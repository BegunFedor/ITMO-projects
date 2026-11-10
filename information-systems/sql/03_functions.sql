-- =====================================================================
-- 03_functions.sql — PL/pgSQL-функции и процедуры операций прецедентов.
-- Каждая операция выполняется атомарно и передаёт идентификатор актёра
-- триггерам через set_config('app.user_id'). Ошибки бизнес-правил
-- возвращаются с кодом в поле HINT, текст для пользователя формирует интерфейс.
-- =====================================================================

CREATE OR REPLACE PROCEDURE set_actor(p_user_id bigint, p_comment text DEFAULT NULL)
LANGUAGE plpgsql AS $$
BEGIN
    PERFORM set_config('app.user_id', COALESCE(p_user_id::text, ''), true);
    PERFORM set_config('app.comment', COALESCE(p_comment, ''), true);
END $$;

-- Смена статуса обращения: блокировка, проверка ожидаемого статуса, запись в журнал.
-- Правила перехода и права актёра проверяет триггер status_before_insert.
CREATE OR REPLACE PROCEDURE change_status(
    p_request_id bigint, p_expected request_status[], p_new request_status)
LANGUAGE plpgsql AS $$
BEGIN
    PERFORM 1 FROM request WHERE request_id = p_request_id FOR UPDATE;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Обращение % не найдено', p_request_id
            USING ERRCODE = 'no_data_found', HINT = 'NOT_FOUND';
    END IF;
    IF NOT (request_status(p_request_id) = ANY (p_expected)) THEN
        RAISE EXCEPTION 'Обращение % не в статусе %', p_request_id, p_expected
            USING ERRCODE = 'no_data_found', HINT = 'STATE_CONFLICT';
    END IF;
    INSERT INTO request_status_history (request_id, status) VALUES (p_request_id, p_new);
END $$;

CREATE OR REPLACE FUNCTION require_role(p_user_id bigint, VARIADIC p_roles user_role[])
RETURNS void LANGUAGE plpgsql AS $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM app_user
                   WHERE user_id = p_user_id AND is_active AND role = ANY (p_roles)) THEN
        RAISE EXCEPTION 'Пользователь % не имеет нужной роли (%) или заблокирован', p_user_id, p_roles
            USING ERRCODE = 'insufficient_privilege', HINT = 'ROLE_REQUIRED';
    END IF;
END $$;

CREATE OR REPLACE FUNCTION staff_company(p_user_id bigint)
RETURNS bigint LANGUAGE sql STABLE AS $$
    SELECT company_id FROM company_staff WHERE user_id = p_user_id
$$;

-- =====================================================================
-- Учётные записи жителей
-- =====================================================================
CREATE OR REPLACE FUNCTION register_resident(
    p_login varchar, p_password_hash varchar, p_full_name varchar, p_phone varchar,
    p_email varchar, p_apartment_id bigint, p_ownership ownership_type)
RETURNS bigint LANGUAGE plpgsql AS $$
DECLARE
    v_id bigint;
BEGIN
    IF EXISTS (SELECT 1 FROM app_user WHERE lower(login) = lower(p_login)) THEN
        RAISE EXCEPTION 'Логин % уже занят', p_login
            USING ERRCODE = 'unique_violation', HINT = 'LOGIN_TAKEN';
    END IF;
    INSERT INTO app_user (login, password_hash, full_name, phone, email, role)
    VALUES (p_login, p_password_hash, p_full_name, p_phone, p_email, 'RESIDENT')
    RETURNING user_id INTO v_id;
    INSERT INTO resident_apartment (user_id, apartment_id, ownership, is_confirmed)
    VALUES (v_id, p_apartment_id, p_ownership, false);
    RETURN v_id;
END $$;

CREATE OR REPLACE PROCEDURE confirm_resident(p_admin_id bigint, p_user_id bigint, p_apartment_id bigint)
LANGUAGE plpgsql AS $$
BEGIN
    PERFORM require_role(p_admin_id, 'COMPANY_ADMIN');
    UPDATE resident_apartment ra SET is_confirmed = true
    FROM apartment a JOIN house h USING (house_id)
    WHERE ra.apartment_id = a.apartment_id
      AND ra.user_id = p_user_id AND ra.apartment_id = p_apartment_id
      AND NOT ra.is_confirmed
      AND h.company_id = staff_company(p_admin_id);
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Заявка жителя % на квартиру % не найдена или уже подтверждена', p_user_id, p_apartment_id
            USING ERRCODE = 'no_data_found', HINT = 'STATE_CONFLICT';
    END IF;
    INSERT INTO notification (user_id, event, entity_type, entity_id)
    VALUES (p_user_id, 'RESIDENT_CONFIRMED', 'APARTMENT', p_apartment_id);
END $$;

-- =====================================================================
-- П1. Создать обращение, отменить новое обращение
-- =====================================================================
CREATE OR REPLACE FUNCTION create_request(
    p_author_id    bigint,
    p_house_id     bigint,
    p_apartment_id bigint,
    p_category_id  bigint,
    p_description  text,
    p_previous_request_id bigint DEFAULT NULL)
RETURNS bigint LANGUAGE plpgsql AS $$
DECLARE
    v_id bigint;
BEGIN
    PERFORM require_role(p_author_id, 'RESIDENT');
    CALL set_actor(p_author_id);
    INSERT INTO request (author_id, house_id, apartment_id, category_id, description, previous_request_id)
    VALUES (p_author_id, p_house_id, p_apartment_id, p_category_id, p_description, p_previous_request_id)
    RETURNING request_id INTO v_id;
    RETURN v_id;
END $$;

CREATE OR REPLACE PROCEDURE cancel_request(p_request_id bigint, p_user_id bigint, p_reason text)
LANGUAGE plpgsql AS $$
BEGIN
    PERFORM require_role(p_user_id, 'RESIDENT', 'COMPANY_ADMIN');
    IF p_reason IS NULL OR length(trim(p_reason)) = 0 THEN
        RAISE EXCEPTION 'Причина отмены обязательна'
            USING ERRCODE = 'check_violation', HINT = 'REASON_REQUIRED';
    END IF;
    CALL set_actor(p_user_id, p_reason);
    CALL change_status(p_request_id, '{NEW,ACCEPTED}', 'CANCELLED');
END $$;

CREATE OR REPLACE FUNCTION add_comment(p_request_id bigint, p_author_id bigint, p_body text)
RETURNS bigint LANGUAGE plpgsql AS $$
DECLARE
    r      request%ROWTYPE;
    v_role user_role;
    v_id   bigint;
BEGIN
    SELECT * INTO r FROM request WHERE request_id = p_request_id;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Обращение % не найдено', p_request_id
            USING ERRCODE = 'no_data_found', HINT = 'NOT_FOUND';
    END IF;
    IF request_status(p_request_id) IN ('CLOSED', 'CANCELLED') THEN
        RAISE EXCEPTION 'Обращение % закрыто для комментариев', p_request_id
            USING ERRCODE = 'check_violation', HINT = 'REQUEST_CLOSED';
    END IF;
    SELECT role INTO v_role FROM app_user WHERE user_id = p_author_id AND is_active;
    IF NOT ((v_role = 'RESIDENT' AND r.author_id = p_author_id)
         OR (v_role IN ('EMPLOYEE', 'COMPANY_ADMIN')
             AND staff_company(p_author_id) = (SELECT company_id FROM house WHERE house_id = r.house_id))) THEN
        RAISE EXCEPTION 'Пользователь % не участвует в обработке обращения %', p_author_id, p_request_id
            USING ERRCODE = 'insufficient_privilege', HINT = 'NOT_PARTICIPANT';
    END IF;
    INSERT INTO request_comment (request_id, author_id, body) VALUES (p_request_id, p_author_id, p_body)
    RETURNING comment_id INTO v_id;
    RETURN v_id;
END $$;

-- =====================================================================
-- П2. Рассмотреть обращение и назначить исполнителя
-- =====================================================================
CREATE OR REPLACE PROCEDURE accept_request(p_request_id bigint, p_admin_id bigint)
LANGUAGE plpgsql AS $$
BEGIN
    PERFORM require_role(p_admin_id, 'COMPANY_ADMIN');
    CALL set_actor(p_admin_id);
    CALL change_status(p_request_id, '{NEW}', 'ACCEPTED');
END $$;

CREATE OR REPLACE FUNCTION suggest_executors(p_request_id bigint)
RETURNS TABLE (executor_id bigint, full_name varchar, matches_specialization boolean, active_requests bigint)
LANGUAGE sql STABLE AS $$
    SELECT u.user_id,
           u.full_name,
           EXISTS (SELECT 1 FROM staff_specialization ss
                   WHERE ss.user_id = u.user_id AND ss.specialization_id = c.specialization_id),
           (SELECT count(*) FROM request x
            WHERE x.executor_id = u.user_id AND x.result_text IS NULL
              AND request_status(x.request_id) IN ('ASSIGNED', 'IN_PROGRESS'))
    FROM request r
    JOIN request_category c ON c.category_id = r.category_id
    JOIN house h            ON h.house_id = r.house_id
    JOIN company_staff s    ON s.company_id = h.company_id
    JOIN app_user u         ON u.user_id = s.user_id
    WHERE r.request_id = p_request_id
      AND u.role = 'EMPLOYEE' AND u.is_active
    ORDER BY 3 DESC, 4 ASC, 2
$$;

CREATE OR REPLACE PROCEDURE assign_executor(
    p_request_id bigint, p_admin_id bigint, p_executor_id bigint, p_comment text DEFAULT NULL)
LANGUAGE plpgsql AS $$
DECLARE
    v_match boolean;
BEGIN
    PERFORM require_role(p_admin_id, 'COMPANY_ADMIN');

    SELECT EXISTS (SELECT 1 FROM staff_specialization ss
                   WHERE ss.user_id = p_executor_id AND ss.specialization_id = c.specialization_id)
    INTO v_match
    FROM request r JOIN request_category c USING (category_id)
    WHERE r.request_id = p_request_id;

    IF NOT v_match AND (p_comment IS NULL OR length(trim(p_comment)) = 0) THEN
        RAISE EXCEPTION 'Специализация сотрудника % не соответствует категории; укажите обоснование', p_executor_id
            USING ERRCODE = 'check_violation', HINT = 'SPECIALIZATION_MISMATCH';
    END IF;

    CALL set_actor(p_admin_id, p_comment);
    PERFORM 1 FROM request WHERE request_id = p_request_id FOR UPDATE;
    IF request_status(p_request_id) IS DISTINCT FROM 'ACCEPTED' THEN
        RAISE EXCEPTION 'Обращение % не найдено или не в статусе ACCEPTED', p_request_id
            USING ERRCODE = 'no_data_found', HINT = 'STATE_CONFLICT';
    END IF;
    UPDATE request SET executor_id = p_executor_id WHERE request_id = p_request_id;
    CALL change_status(p_request_id, '{ACCEPTED}', 'ASSIGNED');
END $$;

-- =====================================================================
-- П3. Выполнить обращение
-- =====================================================================
CREATE OR REPLACE PROCEDURE start_work(p_request_id bigint, p_executor_id bigint)
LANGUAGE plpgsql AS $$
BEGIN
    PERFORM require_role(p_executor_id, 'EMPLOYEE');
    CALL set_actor(p_executor_id);
    CALL change_status(p_request_id, '{ASSIGNED}', 'IN_PROGRESS');
END $$;

CREATE OR REPLACE PROCEDURE complete_request(p_request_id bigint, p_executor_id bigint, p_result text)
LANGUAGE plpgsql AS $$
BEGIN
    PERFORM require_role(p_executor_id, 'EMPLOYEE');
    IF p_result IS NULL OR length(trim(p_result)) = 0 THEN
        RAISE EXCEPTION 'Описание результата обязательно'
            USING ERRCODE = 'check_violation', HINT = 'RESULT_REQUIRED';
    END IF;
    CALL set_actor(p_executor_id);
    PERFORM 1 FROM request WHERE request_id = p_request_id FOR UPDATE;
    IF request_status(p_request_id) IS DISTINCT FROM 'IN_PROGRESS' THEN
        RAISE EXCEPTION 'Обращение % не в статусе IN_PROGRESS', p_request_id
            USING ERRCODE = 'no_data_found', HINT = 'STATE_CONFLICT';
    END IF;
    UPDATE request SET result_text = p_result
    WHERE request_id = p_request_id AND executor_id = p_executor_id;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Сотрудник % не назначен на обращение %', p_executor_id, p_request_id
            USING ERRCODE = 'insufficient_privilege', HINT = 'NOT_REQUEST_EXECUTOR';
    END IF;
    CALL change_status(p_request_id, '{IN_PROGRESS}', 'DONE');
END $$;

-- =====================================================================
-- П4. Принять работу с оценкой или отклонить результат
-- =====================================================================
CREATE OR REPLACE PROCEDURE rate_request(
    p_request_id bigint, p_resident_id bigint, p_score smallint, p_review text DEFAULT NULL)
LANGUAGE plpgsql AS $$
BEGIN
    PERFORM require_role(p_resident_id, 'RESIDENT');
    CALL set_actor(p_resident_id);
    INSERT INTO rating (request_id, score, review) VALUES (p_request_id, p_score, p_review);
END $$;

CREATE OR REPLACE PROCEDURE reject_result(p_request_id bigint, p_resident_id bigint, p_reason text)
LANGUAGE plpgsql AS $$
BEGIN
    PERFORM require_role(p_resident_id, 'RESIDENT');
    IF p_reason IS NULL OR length(trim(p_reason)) = 0 THEN
        RAISE EXCEPTION 'Причина отклонения результата обязательна'
            USING ERRCODE = 'check_violation', HINT = 'REASON_REQUIRED';
    END IF;
    CALL set_actor(p_resident_id, p_reason);
    CALL change_status(p_request_id, '{DONE}', 'CANCELLED');
END $$;

-- =====================================================================
-- Планировщик (бизнес-процессы 3 и 4)
-- =====================================================================
CREATE OR REPLACE FUNCTION mark_overdue_requests(p_now timestamptz DEFAULT now())
RETURNS integer LANGUAGE plpgsql AS $$
DECLARE
    v_cnt integer;
BEGIN
    CALL set_actor(NULL);
    -- Обращение проверяется, когда его срок истёк в течение последних 7 дней: обращение,
    -- которое было открыто позже срока, было открыто и в момент его наступления
    -- (возврата из DONE/CLOSED/CANCELLED нет), поэтому окна достаточно даже при простое планировщика.
    UPDATE request SET is_overdue = true
    WHERE NOT is_overdue
      AND deadline_at BETWEEN p_now - interval '7 days' AND p_now
      AND request_status(request_id) NOT IN ('DONE', 'CLOSED', 'CANCELLED');
    GET DIAGNOSTICS v_cnt = ROW_COUNT;
    RETURN v_cnt;
END $$;

CREATE OR REPLACE FUNCTION auto_close_unrated(p_days integer DEFAULT 7)
RETURNS integer LANGUAGE plpgsql AS $$
DECLARE
    v_cnt integer;
BEGIN
    CALL set_actor(NULL);
    INSERT INTO request_status_history (request_id, status)
    SELECT h.request_id, 'CLOSED'
    FROM request_status_history h
    WHERE h.status = 'DONE'
      -- окно в 30 дней: планировщик запускается ежедневно, более старые уже закрыты
      AND h.changed_at BETWEEN now() - make_interval(days => p_days + 30) AND now() - make_interval(days => p_days)
      AND NOT EXISTS (SELECT 1 FROM request_status_history n
                      WHERE n.request_id = h.request_id AND n.history_id > h.history_id);
    GET DIAGNOSTICS v_cnt = ROW_COUNT;
    RETURN v_cnt;
END $$;

CREATE OR REPLACE FUNCTION mark_overdue_orders(p_today date DEFAULT current_date)
RETURNS integer LANGUAGE plpgsql AS $$
DECLARE
    v_cnt integer;
BEGIN
    UPDATE inspection_order SET status = 'OVERDUE'
    WHERE status = 'ISSUED' AND due_date < p_today;
    GET DIAGNOSTICS v_cnt = ROW_COUNT;
    RETURN v_cnt;
END $$;

-- =====================================================================
-- П5. Передать показания (повторная передача за тот же день заменяет значение)
-- =====================================================================
CREATE OR REPLACE FUNCTION submit_meter_reading(p_meter_id bigint, p_resident_id bigint, p_value numeric)
RETURNS bigint LANGUAGE plpgsql AS $$
DECLARE
    v_id bigint;
BEGIN
    PERFORM require_role(p_resident_id, 'RESIDENT');
    INSERT INTO meter_reading (meter_id, reading_date, value, submitted_by)
    VALUES (p_meter_id, current_date, p_value, p_resident_id)
    ON CONFLICT (meter_id, reading_date)
    DO UPDATE SET value = EXCLUDED.value, submitted_by = EXCLUDED.submitted_by, created_at = now()
    RETURNING reading_id INTO v_id;
    RETURN v_id;
END $$;

-- =====================================================================
-- П6. Опубликовать объявление для домов своей УК
-- =====================================================================
CREATE OR REPLACE FUNCTION publish_announcement(
    p_admin_id bigint, p_title varchar, p_body text, p_house_ids bigint[],
    p_visible_from timestamptz DEFAULT now(), p_visible_to timestamptz DEFAULT NULL)
RETURNS bigint LANGUAGE plpgsql AS $$
DECLARE
    v_company bigint := staff_company(p_admin_id);
    v_id      bigint;
BEGIN
    PERFORM require_role(p_admin_id, 'COMPANY_ADMIN');
    IF p_house_ids IS NULL OR cardinality(p_house_ids) = 0 THEN
        RAISE EXCEPTION 'Не выбран ни один дом'
            USING ERRCODE = 'check_violation', HINT = 'HOUSES_REQUIRED';
    END IF;
    IF EXISTS (SELECT 1 FROM unnest(p_house_ids) AS x(house_id)
               LEFT JOIN house h USING (house_id)
               WHERE h.company_id IS DISTINCT FROM v_company) THEN
        RAISE EXCEPTION 'Объявление можно адресовать только домам своей УК'
            USING ERRCODE = 'insufficient_privilege', HINT = 'FOREIGN_COMPANY';
    END IF;

    INSERT INTO announcement (company_id, author_id, title, body, visible_from, visible_to)
    VALUES (v_company, p_admin_id, p_title, p_body, COALESCE(p_visible_from, now()), p_visible_to)
    RETURNING announcement_id INTO v_id;

    INSERT INTO announcement_house (announcement_id, house_id)
    SELECT DISTINCT v_id, unnest(p_house_ids);

    INSERT INTO notification (user_id, event, entity_type, entity_id)
    SELECT DISTINCT ra.user_id, 'ANNOUNCEMENT_PUBLISHED', 'ANNOUNCEMENT', v_id
    FROM resident_apartment ra JOIN apartment a USING (apartment_id)
    WHERE a.house_id = ANY (p_house_ids) AND ra.is_confirmed;
    RETURN v_id;
END $$;

-- =====================================================================
-- П7. Подать жалобу в ГЖИ
-- =====================================================================
CREATE OR REPLACE FUNCTION file_complaint(p_resident_id bigint, p_request_id bigint, p_body text)
RETURNS bigint LANGUAGE plpgsql AS $$
DECLARE
    v_id bigint;
BEGIN
    PERFORM require_role(p_resident_id, 'RESIDENT');
    INSERT INTO complaint (resident_id, request_id, body)
    VALUES (p_resident_id, p_request_id, p_body)
    RETURNING complaint_id INTO v_id;

    INSERT INTO notification (user_id, event, entity_type, entity_id)
    SELECT i.user_id, 'COMPLAINT_FILED', 'COMPLAINT', v_id
    FROM request r JOIN house h USING (house_id) JOIN inspector i ON i.district_id = h.district_id
    WHERE r.request_id = p_request_id;
    RETURN v_id;
END $$;

-- =====================================================================
-- П8. Контроль обращений инспектором и предписания
-- =====================================================================
CREATE OR REPLACE FUNCTION inspector_overdue_requests(p_inspector_id bigint)
RETURNS TABLE (request_id bigint, company_id bigint, company varchar, address varchar, category varchar,
               status request_status, created_at timestamptz, deadline_at timestamptz,
               hours_overdue numeric, has_complaint boolean)
LANGUAGE sql STABLE AS $$
    SELECT r.request_id, mc.company_id, mc.name, h.address, c.name, r.status, r.created_at, r.deadline_at,
           round(extract(epoch FROM now() - r.deadline_at) / 3600, 1),
           EXISTS (SELECT 1 FROM complaint cp WHERE cp.request_id = r.request_id)
    FROM inspector i
    JOIN house h              ON h.district_id = i.district_id
    JOIN v_request r          ON r.house_id = h.house_id
    JOIN management_company mc ON mc.company_id = h.company_id
    JOIN request_category c   ON c.category_id = r.category_id
    WHERE i.user_id = p_inspector_id
      AND r.is_overdue
      AND r.status NOT IN ('DONE', 'CLOSED', 'CANCELLED')
    ORDER BY r.deadline_at
$$;

CREATE OR REPLACE FUNCTION issue_order(
    p_inspector_id bigint, p_company_id bigint, p_violation text, p_due_date date,
    p_request_ids bigint[], p_complaint_ids bigint[] DEFAULT '{}')
RETURNS bigint LANGUAGE plpgsql AS $$
DECLARE
    v_district bigint;
    v_id       bigint;
    v_number   varchar;
BEGIN
    PERFORM require_role(p_inspector_id, 'INSPECTOR');
    SELECT district_id INTO v_district FROM inspector WHERE user_id = p_inspector_id;

    IF NOT EXISTS (SELECT 1 FROM house WHERE company_id = p_company_id AND district_id = v_district) THEN
        RAISE EXCEPTION 'УК % не обслуживает дома района инспектора', p_company_id
            USING ERRCODE = 'check_violation', HINT = 'FOREIGN_DISTRICT';
    END IF;
    IF EXISTS (SELECT 1 FROM unnest(COALESCE(p_request_ids, '{}')) AS x(request_id)
               LEFT JOIN request r USING (request_id)
               LEFT JOIN house h ON h.house_id = r.house_id
               WHERE h.company_id IS DISTINCT FROM p_company_id
                  OR h.district_id IS DISTINCT FROM v_district) THEN
        RAISE EXCEPTION 'Обращения предписания должны относиться к УК и району инспектора'
            USING ERRCODE = 'check_violation', HINT = 'FOREIGN_DISTRICT';
    END IF;

    v_number := format('ГЖИ-%s-%s', to_char(now(), 'YYYY'),
                       lpad((SELECT count(*) + 1 FROM inspection_order
                             WHERE issued_at >= date_trunc('year', now()))::text, 4, '0'));

    INSERT INTO inspection_order (number, inspector_id, company_id, violation, due_date)
    VALUES (v_number, p_inspector_id, p_company_id, p_violation, p_due_date)
    RETURNING order_id INTO v_id;

    INSERT INTO order_request (order_id, request_id)
    SELECT DISTINCT v_id, unnest(COALESCE(p_request_ids, '{}'));

    UPDATE complaint SET status = 'ORDER_ISSUED', order_id = v_id, inspector_id = p_inspector_id,
                         decided_at = now()
    WHERE complaint_id = ANY (COALESCE(p_complaint_ids, '{}')) AND status IN ('NEW', 'UNDER_REVIEW');

    INSERT INTO notification (user_id, event, entity_type, entity_id)
    SELECT s.user_id, 'ORDER_ISSUED', 'ORDER', v_id
    FROM company_staff s JOIN app_user u USING (user_id)
    WHERE s.company_id = p_company_id AND u.role = 'COMPANY_ADMIN';
    RETURN v_id;
END $$;

CREATE OR REPLACE PROCEDURE reject_complaint(p_inspector_id bigint, p_complaint_id bigint, p_reason text)
LANGUAGE plpgsql AS $$
BEGIN
    PERFORM require_role(p_inspector_id, 'INSPECTOR');
    IF p_reason IS NULL OR length(trim(p_reason)) = 0 THEN
        RAISE EXCEPTION 'Причина отказа обязательна'
            USING ERRCODE = 'check_violation', HINT = 'REASON_REQUIRED';
    END IF;
    UPDATE complaint c SET status = 'REJECTED', inspector_id = p_inspector_id,
                           decision_comment = p_reason, decided_at = now()
    FROM request r JOIN house h USING (house_id) JOIN inspector i ON i.district_id = h.district_id
    WHERE c.complaint_id = p_complaint_id AND r.request_id = c.request_id
      AND i.user_id = p_inspector_id AND c.status IN ('NEW', 'UNDER_REVIEW');
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Жалоба % не найдена, уже рассмотрена или относится к другому району', p_complaint_id
            USING ERRCODE = 'no_data_found', HINT = 'STATE_CONFLICT';
    END IF;
END $$;

CREATE OR REPLACE PROCEDURE submit_order_report(p_order_id bigint, p_admin_id bigint, p_report text)
LANGUAGE plpgsql AS $$
DECLARE
    v_inspector bigint;
BEGIN
    PERFORM require_role(p_admin_id, 'COMPANY_ADMIN');
    IF p_report IS NULL OR length(trim(p_report)) = 0 THEN
        RAISE EXCEPTION 'Текст отчёта обязателен'
            USING ERRCODE = 'check_violation', HINT = 'REPORT_REQUIRED';
    END IF;
    UPDATE inspection_order SET status = 'REPORT_SUBMITTED', report_text = p_report, report_at = now()
    WHERE order_id = p_order_id AND company_id = staff_company(p_admin_id)
      AND status IN ('ISSUED', 'OVERDUE')
    RETURNING inspector_id INTO v_inspector;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Предписание % не найдено, выдано другой УК или уже с отчётом', p_order_id
            USING ERRCODE = 'no_data_found', HINT = 'STATE_CONFLICT';
    END IF;
    INSERT INTO notification (user_id, event, entity_type, entity_id)
    VALUES (v_inspector, 'ORDER_REPORTED', 'ORDER', p_order_id);
END $$;

CREATE OR REPLACE PROCEDURE review_order(
    p_order_id bigint, p_inspector_id bigint, p_accepted boolean, p_comment text DEFAULT NULL)
LANGUAGE plpgsql AS $$
DECLARE
    v_company bigint;
BEGIN
    PERFORM require_role(p_inspector_id, 'INSPECTOR');
    UPDATE inspection_order
    SET status = CASE WHEN p_accepted THEN 'FULFILLED' ELSE 'NOT_FULFILLED' END::order_status,
        reviewed_at = now(), review_comment = p_comment
    WHERE order_id = p_order_id AND inspector_id = p_inspector_id AND status = 'REPORT_SUBMITTED'
    RETURNING company_id INTO v_company;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Предписание % не найдено, выдано другим инспектором или без отчёта', p_order_id
            USING ERRCODE = 'no_data_found', HINT = 'STATE_CONFLICT';
    END IF;
    INSERT INTO notification (user_id, event, entity_type, entity_id)
    SELECT s.user_id, 'ORDER_REVIEWED', 'ORDER', p_order_id
    FROM company_staff s JOIN app_user u USING (user_id)
    WHERE s.company_id = v_company AND u.role = 'COMPANY_ADMIN';
END $$;
