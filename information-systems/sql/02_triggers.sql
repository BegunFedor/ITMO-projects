-- =====================================================================
-- 02_triggers.sql — триггеры, обеспечивающие целостность данных,
-- которую нельзя выразить декларативно (CHECK / FK / UNIQUE)
-- =====================================================================

-- ---------------------------------------------------------------------
-- Вспомогательные функции
-- ---------------------------------------------------------------------
-- Текущий пользователь приложения. Приложение (или функции из 03_functions.sql)
-- в начале транзакции выполняет  SELECT set_config('app.user_id', '<id>', true).
-- NULL означает системное действие (планировщик).
CREATE OR REPLACE FUNCTION app_actor_id() RETURNS bigint
LANGUAGE sql STABLE AS $$
    SELECT NULLIF(current_setting('app.user_id', true), '')::bigint
$$;

CREATE OR REPLACE FUNCTION app_comment() RETURNS text
LANGUAGE sql STABLE AS $$
    SELECT NULLIF(current_setting('app.comment', true), '')
$$;

-- ---------------------------------------------------------------------
-- T1. Создание обращения: проверка автора и адреса, расчёт крайнего срока
-- ---------------------------------------------------------------------
CREATE OR REPLACE FUNCTION trg_request_before_insert() RETURNS trigger
LANGUAGE plpgsql AS $$
DECLARE
    v_norm   integer;
    v_active boolean;
BEGIN
    -- квартира должна принадлежать указанному дому
    IF NEW.apartment_id IS NOT NULL AND NOT EXISTS (
        SELECT 1 FROM apartment WHERE apartment_id = NEW.apartment_id AND house_id = NEW.house_id) THEN
        RAISE EXCEPTION 'Квартира % не относится к дому %', NEW.apartment_id, NEW.house_id
            USING ERRCODE = 'foreign_key_violation', HINT = 'APARTMENT_NOT_IN_HOUSE';
    END IF;

    -- автор — житель с подтверждённой связью с этой квартирой (или с квартирой в этом доме)
    IF NOT EXISTS (
        SELECT 1
        FROM resident_apartment ra
        JOIN apartment a USING (apartment_id)
        WHERE ra.user_id = NEW.author_id
          AND ra.is_confirmed
          AND a.house_id = NEW.house_id
          AND (NEW.apartment_id IS NULL OR a.apartment_id = NEW.apartment_id)) THEN
        RAISE EXCEPTION 'Пользователь % не является подтверждённым жителем указанной квартиры/дома',
            NEW.author_id USING ERRCODE = 'insufficient_privilege', HINT = 'NOT_CONFIRMED_RESIDENT';
    END IF;

    -- повторное обращение ссылается только на своё отменённое обращение по тому же дому
    IF NEW.previous_request_id IS NOT NULL AND NOT EXISTS (
        SELECT 1 FROM request p
        WHERE p.request_id = NEW.previous_request_id AND p.author_id = NEW.author_id
          AND p.house_id = NEW.house_id AND request_status(p.request_id) = 'CANCELLED') THEN
        RAISE EXCEPTION 'Повторное обращение может ссылаться только на своё отменённое обращение по этому дому'
            USING ERRCODE = 'check_violation', HINT = 'PREVIOUS_REQUEST_INVALID';
    END IF;

    SELECT norm_hours, is_active INTO v_norm, v_active
    FROM request_category WHERE category_id = NEW.category_id;
    IF NOT v_active THEN
        RAISE EXCEPTION 'Категория % не используется', NEW.category_id USING ERRCODE = 'check_violation', HINT = 'CATEGORY_INACTIVE';
    END IF;

    -- обращение создаётся без исполнителя; статус «Новое» записывает T4
    NEW.executor_id := NULL;
    NEW.result_text := NULL;
    NEW.is_overdue  := false;
    NEW.created_at  := COALESCE(NEW.created_at, now());
    NEW.deadline_at := NEW.created_at + make_interval(hours => v_norm);
    NEW.updated_at  := NEW.created_at;
    NEW.version     := 0;
    RETURN NEW;
END $$;

CREATE TRIGGER request_before_insert BEFORE INSERT ON request
    FOR EACH ROW EXECUTE FUNCTION trg_request_before_insert();

-- ---------------------------------------------------------------------
-- T2. Изменение полей обращения: что и в каком статусе можно менять
-- ---------------------------------------------------------------------
CREATE OR REPLACE FUNCTION trg_request_before_update() RETURNS trigger
LANGUAGE plpgsql AS $$
DECLARE
    v_status request_status := request_status(OLD.request_id);
BEGIN
    -- неизменяемые после создания поля
    IF NEW.author_id <> OLD.author_id OR NEW.house_id <> OLD.house_id
       OR NEW.apartment_id IS DISTINCT FROM OLD.apartment_id
       OR NEW.previous_request_id IS DISTINCT FROM OLD.previous_request_id
       OR NEW.created_at <> OLD.created_at OR NEW.deadline_at <> OLD.deadline_at THEN
        RAISE EXCEPTION 'Автор, адрес, дата создания и крайний срок обращения не изменяются'
            USING ERRCODE = 'check_violation', HINT = 'REQUEST_IMMUTABLE_FIELDS';
    END IF;

    -- закрытые и отменённые обращения не изменяются (кроме служебных version/updated_at)
    IF v_status IN ('CLOSED', 'CANCELLED')
       AND (NEW.executor_id IS DISTINCT FROM OLD.executor_id
            OR NEW.result_text IS DISTINCT FROM OLD.result_text
            OR NEW.description <> OLD.description OR NEW.category_id <> OLD.category_id
            OR NEW.is_overdue <> OLD.is_overdue) THEN
        RAISE EXCEPTION 'Обращение % в статусе % не может изменяться', OLD.request_id, v_status
            USING ERRCODE = 'check_violation', HINT = 'REQUEST_CLOSED';
    END IF;

    -- исполнитель назначается или снимается только в статусе «Принято»,
    -- результат работ записывается только в статусе «В работе»
    IF NEW.executor_id IS DISTINCT FROM OLD.executor_id AND v_status <> 'ACCEPTED' THEN
        RAISE EXCEPTION 'Исполнитель меняется только у принятого обращения'
            USING ERRCODE = 'check_violation', HINT = 'EXECUTOR_CHANGE_FORBIDDEN';
    END IF;
    IF NEW.result_text IS DISTINCT FROM OLD.result_text AND v_status <> 'IN_PROGRESS' THEN
        RAISE EXCEPTION 'Результат работ записывается только у обращения в работе'
            USING ERRCODE = 'check_violation', HINT = 'TRANSITION_FORBIDDEN';
    END IF;

    NEW.updated_at := now();
    NEW.version    := OLD.version + 1;
    RETURN NEW;
END $$;

CREATE TRIGGER request_before_update BEFORE UPDATE ON request
    FOR EACH ROW EXECUTE FUNCTION trg_request_before_update();

-- ---------------------------------------------------------------------
-- T3. Исполнитель: активный сотрудник УК, обслуживающей дом
-- ---------------------------------------------------------------------
CREATE OR REPLACE FUNCTION trg_request_check_executor() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
    IF NEW.executor_id IS NOT NULL AND NOT EXISTS (
        SELECT 1
        FROM app_user u
        JOIN company_staff s USING (user_id)
        JOIN house h ON h.company_id = s.company_id
        WHERE u.user_id = NEW.executor_id
          AND u.role = 'EMPLOYEE' AND u.is_active
          AND h.house_id = NEW.house_id) THEN
        RAISE EXCEPTION 'Пользователь % не может быть исполнителем обращения по дому %',
            NEW.executor_id, NEW.house_id USING ERRCODE = 'check_violation', HINT = 'EXECUTOR_INVALID';
    END IF;
    RETURN NEW;
END $$;

CREATE TRIGGER request_check_executor BEFORE INSERT OR UPDATE OF executor_id ON request
    FOR EACH ROW EXECUTE FUNCTION trg_request_check_executor();

-- ---------------------------------------------------------------------
-- T4. Статус нового обращения: первая запись журнала статусов
-- ---------------------------------------------------------------------
CREATE OR REPLACE FUNCTION trg_request_after_insert() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
    INSERT INTO request_status_history (request_id, status, changed_by)
    VALUES (NEW.request_id, 'NEW', COALESCE(app_actor_id(), NEW.author_id));
    RETURN NULL;
END $$;

CREATE TRIGGER request_after_insert AFTER INSERT ON request
    FOR EACH ROW EXECUTE FUNCTION trg_request_after_insert();

-- ---------------------------------------------------------------------
-- T5. Смена статуса = новая запись журнала: конечный автомат и права актёра
-- ---------------------------------------------------------------------
CREATE OR REPLACE FUNCTION trg_status_before_insert() RETURNS trigger
LANGUAGE plpgsql AS $$
DECLARE
    r            request%ROWTYPE;
    v_old        request_status;
    v_actor      bigint := COALESCE(NEW.changed_by, app_actor_id());
    v_actor_role text;
BEGIN
    -- блокировка обращения: одновременные смены статуса выполняются по очереди
    SELECT * INTO r FROM request WHERE request_id = NEW.request_id FOR UPDATE;
    v_old := request_status(NEW.request_id);

    NEW.changed_by := v_actor;
    NEW.changed_at := now();
    NEW.comment    := COALESCE(NEW.comment, app_comment());

    IF v_old IS NULL THEN
        NEW.changed_at := r.created_at;            -- статус «Новое» — момент создания обращения
        IF NEW.status <> 'NEW' THEN
            RAISE EXCEPTION 'Первый статус обращения — NEW'
                USING ERRCODE = 'check_violation', HINT = 'TRANSITION_FORBIDDEN';
        END IF;
        RETURN NEW;
    END IF;

    IF v_old IN ('CLOSED', 'CANCELLED') THEN
        RAISE EXCEPTION 'Обращение % в статусе % не может изменяться', NEW.request_id, v_old
            USING ERRCODE = 'check_violation', HINT = 'REQUEST_CLOSED';
    END IF;

    IF v_actor IS NULL THEN
        v_actor_role := 'SYSTEM';
    ELSE
        SELECT role::text INTO v_actor_role FROM app_user WHERE user_id = v_actor;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM status_transition
                   WHERE from_status = v_old AND to_status = NEW.status
                     AND actor_role = v_actor_role) THEN
        RAISE EXCEPTION 'Переход % -> % недопустим для роли %', v_old, NEW.status, v_actor_role
            USING ERRCODE = 'check_violation', HINT = 'TRANSITION_FORBIDDEN';
    END IF;

    IF v_actor_role = 'RESIDENT' AND v_actor <> r.author_id THEN
        RAISE EXCEPTION 'Житель может менять статус только своего обращения'
            USING ERRCODE = 'insufficient_privilege', HINT = 'NOT_REQUEST_AUTHOR';
    ELSIF v_actor_role = 'EMPLOYEE' AND v_actor IS DISTINCT FROM r.executor_id THEN
        RAISE EXCEPTION 'Сотрудник может менять статус только назначенного ему обращения'
            USING ERRCODE = 'insufficient_privilege', HINT = 'NOT_REQUEST_EXECUTOR';
    ELSIF v_actor_role = 'COMPANY_ADMIN'
          AND (SELECT company_id FROM company_staff WHERE user_id = v_actor)
              IS DISTINCT FROM (SELECT company_id FROM house WHERE house_id = r.house_id) THEN
        RAISE EXCEPTION 'Администратор может обрабатывать только обращения по домам своей УК'
            USING ERRCODE = 'insufficient_privilege', HINT = 'FOREIGN_COMPANY';
    END IF;

    -- условия на данные обращения в новом статусе
    IF NEW.status = 'ASSIGNED' AND r.executor_id IS NULL THEN
        RAISE EXCEPTION 'Для статуса ASSIGNED нужен исполнитель'
            USING ERRCODE = 'check_violation', HINT = 'EXECUTOR_INVALID';
    END IF;
    IF NEW.status = 'DONE' AND r.result_text IS NULL THEN
        RAISE EXCEPTION 'Для статуса DONE нужен результат работ'
            USING ERRCODE = 'check_violation', HINT = 'RESULT_REQUIRED';
    END IF;
    RETURN NEW;
END $$;

CREATE TRIGGER status_before_insert BEFORE INSERT ON request_status_history
    FOR EACH ROW EXECUTE FUNCTION trg_status_before_insert();

-- ---------------------------------------------------------------------
-- T6. После смены статуса: побочные эффекты и уведомления
-- ---------------------------------------------------------------------
CREATE OR REPLACE FUNCTION trg_status_after_insert() RETURNS trigger
LANGUAGE plpgsql AS $$
DECLARE
    r     request%ROWTYPE;
    v_old request_status;
BEGIN
    SELECT * INTO r FROM request WHERE request_id = NEW.request_id;
    SELECT status INTO v_old FROM request_status_history
    WHERE request_id = NEW.request_id AND history_id < NEW.history_id
    ORDER BY history_id DESC LIMIT 1;

    IF v_old IS NULL THEN
        -- новое обращение — всем администраторам УК, обслуживающей дом
        INSERT INTO notification (user_id, event, entity_type, entity_id, status)
        SELECT s.user_id, 'REQUEST_CREATED', 'REQUEST', r.request_id, NEW.status
        FROM company_staff s
        JOIN app_user u USING (user_id)
        JOIN house h ON h.company_id = s.company_id
        WHERE h.house_id = r.house_id AND u.role = 'COMPANY_ADMIN' AND u.is_active;
        RETURN NULL;
    END IF;

    -- снятие исполнителя (ASSIGNED -> ACCEPTED); любое UPDATE увеличивает версию обращения (T2)
    IF NEW.status = 'ACCEPTED' THEN
        UPDATE request SET executor_id = NULL WHERE request_id = r.request_id;
    ELSE
        UPDATE request SET updated_at = now() WHERE request_id = r.request_id;
    END IF;

    IF r.author_id IS DISTINCT FROM NEW.changed_by THEN
        INSERT INTO notification (user_id, event, entity_type, entity_id, status)
        VALUES (r.author_id, 'STATUS_CHANGED', 'REQUEST', r.request_id, NEW.status);
    END IF;
    IF NEW.status = 'ASSIGNED' OR (NEW.status = 'CANCELLED' AND v_old = 'DONE') THEN
        INSERT INTO notification (user_id, event, entity_type, entity_id, status)
        VALUES (r.executor_id,
                CASE WHEN NEW.status = 'ASSIGNED' THEN 'REQUEST_ASSIGNED' ELSE 'RESULT_REJECTED' END,
                'REQUEST', r.request_id, NEW.status);
    END IF;
    RETURN NULL;
END $$;

CREATE TRIGGER status_after_insert AFTER INSERT ON request_status_history
    FOR EACH ROW EXECUTE FUNCTION trg_status_after_insert();

-- ---------------------------------------------------------------------
-- T7. Журнал статусов только дополняется
-- ---------------------------------------------------------------------
CREATE OR REPLACE FUNCTION trg_forbid_modification() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
    RAISE EXCEPTION 'Таблица % только для добавления записей', TG_TABLE_NAME
        USING ERRCODE = 'insufficient_privilege', HINT = 'APPEND_ONLY';
END $$;

CREATE TRIGGER status_history_append_only BEFORE UPDATE OR DELETE ON request_status_history
    FOR EACH ROW EXECUTE FUNCTION trg_forbid_modification();
-- Удаление журнала каскадом вместе с обращением допускается только через TRUNCATE/DROP схемы.

-- ---------------------------------------------------------------------
-- T8. Оценка: только выполненного обращения; после оценки обращение закрывается
-- ---------------------------------------------------------------------
CREATE OR REPLACE FUNCTION trg_rating_before_insert() RETURNS trigger
LANGUAGE plpgsql AS $$
DECLARE
    r request%ROWTYPE;
BEGIN
    SELECT * INTO r FROM request WHERE request_id = NEW.request_id FOR UPDATE;
    IF request_status(r.request_id) IS DISTINCT FROM 'DONE' THEN
        RAISE EXCEPTION 'Оценить можно только обращение в статусе DONE (текущий: %)', request_status(r.request_id)
            USING ERRCODE = 'check_violation', HINT = 'RATING_REQUIRES_DONE';
    END IF;
    IF app_actor_id() IS DISTINCT FROM r.author_id THEN
        RAISE EXCEPTION 'Оценку ставит только автор обращения' USING ERRCODE = 'insufficient_privilege', HINT = 'NOT_REQUEST_AUTHOR';
    END IF;
    NEW.created_at := now();
    RETURN NEW;
END $$;

CREATE OR REPLACE FUNCTION trg_rating_after_insert() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
    INSERT INTO request_status_history (request_id, status) VALUES (NEW.request_id, 'CLOSED');
    RETURN NULL;
END $$;

CREATE TRIGGER rating_before_insert BEFORE INSERT ON rating
    FOR EACH ROW EXECUTE FUNCTION trg_rating_before_insert();
CREATE TRIGGER rating_after_insert AFTER INSERT ON rating
    FOR EACH ROW EXECUTE FUNCTION trg_rating_after_insert();

-- ---------------------------------------------------------------------
-- T9. Показания приборов учёта: монотонность и право жителя
-- ---------------------------------------------------------------------
CREATE OR REPLACE FUNCTION trg_reading_before_write() RETURNS trigger
LANGUAGE plpgsql AS $$
DECLARE
    v_prev numeric;
    v_next numeric;
BEGIN
    IF NOT (SELECT is_active FROM meter WHERE meter_id = NEW.meter_id) THEN
        RAISE EXCEPTION 'Прибор учёта % выведен из эксплуатации', NEW.meter_id
            USING ERRCODE = 'check_violation', HINT = 'METER_INACTIVE';
    END IF;

    IF NEW.submitted_by IS NOT NULL AND NOT EXISTS (
        SELECT 1 FROM meter m JOIN resident_apartment ra USING (apartment_id)
        WHERE m.meter_id = NEW.meter_id AND ra.user_id = NEW.submitted_by AND ra.is_confirmed) THEN
        RAISE EXCEPTION 'Пользователь % не связан с квартирой прибора %', NEW.submitted_by, NEW.meter_id
            USING ERRCODE = 'insufficient_privilege', HINT = 'METER_NOT_OWNED';
    END IF;

    SELECT value INTO v_prev FROM meter_reading
    WHERE meter_id = NEW.meter_id AND reading_date < NEW.reading_date
    ORDER BY reading_date DESC LIMIT 1;

    SELECT value INTO v_next FROM meter_reading
    WHERE meter_id = NEW.meter_id AND reading_date > NEW.reading_date
    ORDER BY reading_date ASC LIMIT 1;

    IF NEW.value < v_prev THEN
        RAISE EXCEPTION 'Показание % меньше предыдущего (%)', NEW.value, v_prev
            USING ERRCODE = 'check_violation', HINT = 'READING_DECREASES';
    END IF;
    IF NEW.value > v_next THEN
        RAISE EXCEPTION 'Показание % больше последующего (%)', NEW.value, v_next
            USING ERRCODE = 'check_violation', HINT = 'READING_EXCEEDS_NEXT';
    END IF;
    RETURN NEW;
END $$;

CREATE TRIGGER reading_before_write BEFORE INSERT OR UPDATE OF value, reading_date ON meter_reading
    FOR EACH ROW EXECUTE FUNCTION trg_reading_before_write();

-- ---------------------------------------------------------------------
-- T10. Жалоба в ГЖИ: только при нарушении со стороны УК
-- ---------------------------------------------------------------------
CREATE OR REPLACE FUNCTION trg_complaint_before_insert() RETURNS trigger
LANGUAGE plpgsql AS $$
DECLARE
    r request%ROWTYPE;
    v_status request_status;
    v_score smallint;
    v_cancelled_by_company boolean;
    v_result_rejected      boolean;
BEGIN
    SELECT * INTO r FROM request WHERE request_id = NEW.request_id;
    IF r.author_id <> NEW.resident_id THEN
        RAISE EXCEPTION 'Жалобу можно подать только по своему обращению'
            USING ERRCODE = 'insufficient_privilege', HINT = 'NOT_REQUEST_AUTHOR';
    END IF;

    v_status := request_status(r.request_id);
    SELECT score INTO v_score FROM rating WHERE request_id = r.request_id;
    SELECT h.changed_by IS DISTINCT FROM r.author_id,
           (SELECT p.status FROM request_status_history p
            WHERE p.request_id = h.request_id AND p.history_id < h.history_id
            ORDER BY p.history_id DESC LIMIT 1) = 'DONE'
    INTO v_cancelled_by_company, v_result_rejected
    FROM request_status_history h
    WHERE h.request_id = r.request_id AND h.status = 'CANCELLED';

    IF NOT (r.is_overdue
            OR (v_status = 'CANCELLED' AND (v_cancelled_by_company OR v_result_rejected))
            OR (v_status = 'CLOSED' AND v_score <= 2)) THEN
        RAISE EXCEPTION 'Жалоба допустима, если обращение просрочено, отменено УК, результат отклонён жителем или оценён на 1–2'
            USING ERRCODE = 'check_violation', HINT = 'COMPLAINT_NOT_ALLOWED';
    END IF;

    NEW.status := 'NEW';
    NEW.inspector_id := NULL;
    NEW.order_id := NULL;
    NEW.decided_at := NULL;
    RETURN NEW;
END $$;

CREATE TRIGGER complaint_before_insert BEFORE INSERT ON complaint
    FOR EACH ROW EXECUTE FUNCTION trg_complaint_before_insert();
