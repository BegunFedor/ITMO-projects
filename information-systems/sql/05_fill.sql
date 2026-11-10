-- =====================================================================
-- 05_fill.sql — заполнение базы тестовыми данными.
-- Справочники и объекты вставляются напрямую, а жизненный цикл обращений
-- прогоняется через процедуры из 03_functions.sql, поэтому все триггеры
-- срабатывают так же, как при работе приложения.
-- Пароль всех тестовых пользователей: password
-- =====================================================================

-- Поиск id пользователя по логину (временная функция сессии)
CREATE OR REPLACE FUNCTION pg_temp.uid(p_login text) RETURNS bigint
LANGUAGE sql AS $$ SELECT user_id FROM app_user WHERE login = p_login $$;

-- ---------------------------------------------------------------------
-- Районы, УК, дома, квартиры
-- ---------------------------------------------------------------------
INSERT INTO district (name) VALUES ('Центральный'), ('Петроградский');

INSERT INTO management_company (name, inn, address, dispatch_phone) VALUES
    ('ООО «УК Северная»',       '7801000001', 'Санкт-Петербург, ул. Примерная, 1',   '+7 812 100-00-01'),
    ('ООО «ЖилСервис»',         '7801000002', 'Санкт-Петербург, ул. Тестовая, 15',   '+7 812 100-00-02'),
    ('ООО «УК Невский берег»',  '7801000003', 'Санкт-Петербург, наб. Условная, 7',   '+7 812 100-00-03');

INSERT INTO house (company_id, district_id, address, build_year, floors) VALUES
    (1, 1, 'ул. Садовая, 10',          1965, 5),
    (1, 1, 'ул. Садовая, 12',          1968, 9),
    (2, 1, 'пр. Литейный, 40',         1910, 6),
    (2, 2, 'ул. Ленина, 3',            1985, 12),
    (3, 2, 'Большой пр. П.С., 55',     2005, 16),
    (3, 2, 'ул. Рентгена, 8',          2012, 10);

-- по 12 квартир в каждом доме: 3 подъезда по 4 квартиры
INSERT INTO apartment (house_id, number, entrance, floor, area_m2)
SELECT h.house_id, n::text, (n - 1) / 4 + 1, (n - 1) % 4 + 1, 30 + (n * 7 % 50)
FROM house h CROSS JOIN generate_series(1, 12) AS n;

-- ---------------------------------------------------------------------
-- Пользователи
-- ---------------------------------------------------------------------
INSERT INTO app_user (login, password_hash, full_name, phone, email, role)
SELECT login, 'pbkdf2_sha256$120000$bWtkLXRlc3Qtc2FsdC0xNg==$6rHYAIsd5moRxl0Yf6lRDd19afhHmq8bRtMEpOHZZs4=', fio, phone, email, role::user_role
FROM (VALUES
    -- администраторы УК
    ('admin_sev',  'Орлова Марина Викторовна',    '+79110000001', 'orlova@uk-sev.example',   'COMPANY_ADMIN'),
    ('admin_zhs',  'Кузнецов Игорь Петрович',     '+79110000002', 'kuznetsov@zhs.example',   'COMPANY_ADMIN'),
    ('admin_nev',  'Белова Анна Сергеевна',       '+79110000003', 'belova@nev.example',      'COMPANY_ADMIN'),
    -- сотрудники
    ('plumb_sev',  'Смирнов Олег Иванович',       '+79110000011', NULL, 'EMPLOYEE'),
    ('elec_sev',   'Попов Денис Андреевич',       '+79110000012', NULL, 'EMPLOYEE'),
    ('lift_sev',   'Волков Сергей Николаевич',    '+79110000013', NULL, 'EMPLOYEE'),
    ('clean_sev',  'Зайцева Ольга Петровна',      '+79110000014', NULL, 'EMPLOYEE'),
    ('plumb_zhs',  'Морозов Алексей Юрьевич',     '+79110000021', NULL, 'EMPLOYEE'),
    ('elec_zhs',   'Лебедев Павел Олегович',      '+79110000022', NULL, 'EMPLOYEE'),
    ('clean_zhs',  'Козлова Ирина Алексеевна',    '+79110000023', NULL, 'EMPLOYEE'),
    ('plumb_nev',  'Новиков Артём Сергеевич',     '+79110000031', NULL, 'EMPLOYEE'),
    ('multi_nev',  'Соколов Виктор Ильич',        '+79110000032', NULL, 'EMPLOYEE'),
    ('clean_nev',  'Павлова Елена Дмитриевна',    '+79110000033', NULL, 'EMPLOYEE'),
    -- инспекторы ГЖИ
    ('insp_center', 'Фёдоров Николай Андреевич',  '+79110000041', 'fedorov@gzhi.example', 'INSPECTOR'),
    ('insp_petro',  'Егорова Татьяна Ивановна',   '+79110000042', 'egorova@gzhi.example', 'INSPECTOR'),
    -- жители
    ('ivanov',     'Иванов Пётр Сергеевич',       '+79210000001', 'ivanov@mail.example',   'RESIDENT'),
    ('petrova',    'Петрова Мария Олеговна',      '+79210000002', 'petrova@mail.example',  'RESIDENT'),
    ('sidorov',    'Сидоров Андрей Викторович',   '+79210000003', NULL, 'RESIDENT'),
    ('kozlov',     'Козлов Дмитрий Игоревич',     '+79210000004', NULL, 'RESIDENT'),
    ('nikitina',   'Никитина Светлана Павловна',  '+79210000005', NULL, 'RESIDENT'),
    ('fomin',      'Фомин Глеб Александрович',    '+79210000006', NULL, 'RESIDENT'),
    ('grigorieva', 'Григорьева Алла Борисовна',   '+79210000007', NULL, 'RESIDENT'),
    ('tarasov',    'Тарасов Илья Романович',      '+79210000008', NULL, 'RESIDENT'),
    ('belov',      'Белов Константин Ефимович',   '+79210000009', NULL, 'RESIDENT'),
    ('komarova',   'Комарова Вера Станиславовна', '+79210000010', NULL, 'RESIDENT'),
    ('orlov',      'Орлов Степан Михайлович',     '+79210000011', NULL, 'RESIDENT'),
    ('kiseleva',   'Киселёва Дарья Андреевна',    '+79210000012', NULL, 'RESIDENT'),
    ('newbie',     'Новый Житель Тестович',       '+79210000013', NULL, 'RESIDENT')
) AS t(login, fio, phone, email, role);

INSERT INTO company_staff (user_id, company_id, position) VALUES
    (pg_temp.uid('admin_sev'), 1, 'Главный диспетчер'),
    (pg_temp.uid('admin_zhs'), 2, 'Диспетчер'),
    (pg_temp.uid('admin_nev'), 3, 'Начальник участка'),
    (pg_temp.uid('plumb_sev'), 1, 'Сантехник'),
    (pg_temp.uid('elec_sev'),  1, 'Электрик'),
    (pg_temp.uid('lift_sev'),  1, 'Механик по лифтам'),
    (pg_temp.uid('clean_sev'), 1, 'Дворник'),
    (pg_temp.uid('plumb_zhs'), 2, 'Сантехник'),
    (pg_temp.uid('elec_zhs'),  2, 'Электрик'),
    (pg_temp.uid('clean_zhs'), 2, 'Уборщица'),
    (pg_temp.uid('plumb_nev'), 3, 'Слесарь-сантехник'),
    (pg_temp.uid('multi_nev'), 3, 'Мастер участка'),
    (pg_temp.uid('clean_nev'), 3, 'Дворник');

INSERT INTO specialization (name) VALUES
    ('Сантехника'), ('Электрика'), ('Лифты'), ('Отопление'), ('Уборка'), ('Общестроительные работы');

INSERT INTO staff_specialization (user_id, specialization_id)
SELECT pg_temp.uid(l), s.specialization_id
FROM (VALUES ('plumb_sev', 'Сантехника'), ('plumb_sev', 'Отопление'),
             ('elec_sev',  'Электрика'),
             ('lift_sev',  'Лифты'),      ('lift_sev', 'Электрика'),
             ('clean_sev', 'Уборка'),
             ('plumb_zhs', 'Сантехника'), ('plumb_zhs', 'Отопление'),
             ('elec_zhs',  'Электрика'),  ('elec_zhs',  'Лифты'),
             ('clean_zhs', 'Уборка'),
             ('plumb_nev', 'Сантехника'), ('plumb_nev', 'Отопление'),
             ('multi_nev', 'Электрика'),  ('multi_nev', 'Лифты'), ('multi_nev', 'Общестроительные работы'),
             ('clean_nev', 'Уборка')) AS t(l, sname)
JOIN specialization s ON s.name = t.sname;

INSERT INTO inspector (user_id, district_id, service_number) VALUES
    (pg_temp.uid('insp_center'), 1, 'ГЖИ-0141'),
    (pg_temp.uid('insp_petro'),  2, 'ГЖИ-0277');

-- Связи «житель — квартира» (M:N): у Иванова две квартиры, в кв. 5 дома 1 — двое жителей,
-- у newbie связь ещё не подтверждена
INSERT INTO resident_apartment (user_id, apartment_id, ownership, is_confirmed)
SELECT pg_temp.uid(l), a.apartment_id, o::ownership_type, c
FROM (VALUES ('ivanov',     1, '5',  'OWNER',  true),
             ('ivanov',     2, '7',  'OWNER',  true),
             ('petrova',    1, '5',  'OWNER',  true),
             ('sidorov',    1, '9',  'TENANT', true),
             ('kozlov',     2, '3',  'OWNER',  true),
             ('nikitina',   2, '11', 'OWNER',  true),
             ('fomin',      3, '2',  'OWNER',  true),
             ('grigorieva', 3, '8',  'TENANT', true),
             ('tarasov',    4, '4',  'OWNER',  true),
             ('belov',      4, '10', 'OWNER',  true),
             ('komarova',   5, '1',  'OWNER',  true),
             ('orlov',      5, '6',  'OWNER',  true),
             ('kiseleva',   6, '12', 'TENANT', true),
             ('newbie',     6, '3',  'OWNER',  false)) AS t(l, h, num, o, c)
JOIN apartment a ON a.house_id = t.h AND a.number = t.num;

-- ---------------------------------------------------------------------
-- Категории обращений
-- ---------------------------------------------------------------------
INSERT INTO request_category (name, specialization_id, norm_hours, is_emergency)
SELECT n, s.specialization_id, h, e
FROM (VALUES ('Протечка воды',                 'Сантехника', 24,  true),
             ('Засор канализации',             'Сантехника', 48,  false),
             ('Отсутствие электричества',      'Электрика',  6,   true),
             ('Неисправность освещения в МОП', 'Электрика',  72,  false),
             ('Остановка лифта',               'Лифты',      24,  true),
             ('Нет отопления',                 'Отопление',  24,  true),
             ('Уборка подъезда',               'Уборка',     48,  false),
             ('Уборка придомовой территории',  'Уборка',     72,  false),
             ('Ремонт общего имущества',       'Общестроительные работы', 168, false)) AS t(n, sname, h, e)
JOIN specialization s ON s.name = t.sname;

-- ---------------------------------------------------------------------
-- Приборы учёта и история показаний (3 месяца) для заселённых квартир
-- ---------------------------------------------------------------------
INSERT INTO meter (apartment_id, resource, serial_number, verified_until)
SELECT a.apartment_id, r.res::resource_type,
       upper(left(r.res, 2)) || '-' || lpad(a.apartment_id::text, 5, '0'),
       current_date + 365 * 3
FROM (SELECT DISTINCT apartment_id FROM resident_apartment WHERE is_confirmed) ra
JOIN apartment a USING (apartment_id)
CROSS JOIN (VALUES ('COLD_WATER'), ('HOT_WATER'), ('ELECTRICITY')) AS r(res);

INSERT INTO meter_reading (meter_id, reading_date, value, submitted_by)
SELECT m.meter_id,
       (date_trunc('month', current_date) - make_interval(months => k) + interval '19 days')::date,
       CASE m.resource WHEN 'ELECTRICITY' THEN 5000 + (3 - k) * (150 + m.meter_id % 40)
                       ELSE 100 + (3 - k) * (4 + m.meter_id % 5) END,
       (SELECT ra.user_id FROM resident_apartment ra
        WHERE ra.apartment_id = m.apartment_id AND ra.is_confirmed ORDER BY ra.user_id LIMIT 1)
FROM meter m CROSS JOIN generate_series(1, 3) AS k
ORDER BY 2;

-- ---------------------------------------------------------------------
-- Обращения: создание в прошлом и прохождение жизненного цикла
-- ---------------------------------------------------------------------
CREATE TEMP TABLE t_req (code text PRIMARY KEY, request_id bigint);
CREATE OR REPLACE FUNCTION pg_temp.rq(p_code text) RETURNS bigint
LANGUAGE sql AS $$ SELECT request_id FROM t_req WHERE code = p_code $$;

INSERT INTO request (author_id, house_id, apartment_id, category_id, description, created_at)
SELECT pg_temp.uid(t.l), t.h, a.apartment_id, c.category_id, t.descr, now() - make_interval(hours => t.ago)
FROM (VALUES
    ('r01', 'ivanov',     1, '5',  'Протечка воды',                 'Течёт стояк холодной воды в ванной, капает на пол', 2),
    ('r02', 'petrova',    1, NULL, 'Неисправность освещения в МОП', 'Не горит свет на лестнице между 3 и 4 этажами',     5),
    ('r03', 'kozlov',     2, NULL, 'Остановка лифта',               'Лифт в первом подъезде стоит на 6 этаже, не едет',  30),
    ('r04', 'nikitina',   2, '11', 'Засор канализации',             'Засор в кухонной раковине, вода не уходит',         70),
    ('r05', 'fomin',      3, '2',  'Нет отопления',                 'Холодные батареи во всей квартире третий день',     60),
    ('r06', 'grigorieva', 3, NULL, 'Уборка подъезда',               'Подъезд не убирали больше недели, мусор у лифта',   120),
    ('r07', 'tarasov',    4, '4',  'Отсутствие электричества',      'В квартире нет света, автоматы в щитке включены',   20),
    ('r08', 'komarova',   5, NULL, 'Уборка придомовой территории',  'Не вывезены листья и мусор у детской площадки',     200),
    ('r09', 'orlov',      5, '6',  'Протечка воды',                 'Протекает потолок в коридоре после дождя',          100),
    ('r10', 'kiseleva',   6, NULL, 'Ремонт общего имущества',       'Сломан доводчик входной двери подъезда 1',          300),
    ('r11', 'ivanov',     2, '7',  'Засор канализации',             'Медленно уходит вода в ванной, нужна прочистка',    150),
    ('r12', 'sidorov',    1, '9',  'Протечка воды',                 'Подтекает гибкая подводка к смесителю на кухне',    90),
    ('r13', 'belov',      4, NULL, 'Неисправность освещения в МОП', 'Не работает освещение у входа в подъезд',           250),
    ('r14', 'petrova',    1, NULL, 'Уборка придомовой территории',  'Прошу убрать снег с дорожки к подъезду (тест)',     12),
    ('r15', 'tarasov',    4, NULL, 'Остановка лифта',               'Лифт застревает между этажами, двери не открываются', 50),
    ('r16', 'komarova',   5, '1',  'Засор канализации',             'Засор унитаза, требуется срочная прочистка',        80),
    ('r17', 'fomin',      3, NULL, 'Ремонт общего имущества',       'Разбито стекло в окне на площадке 3 этажа',         40),
    ('r18', 'kozlov',     2, '3',  'Отсутствие электричества',      'Пропадает свет в комнате, искрит розетка',          1)
) AS t(code, l, h, num, cat, descr, ago)
JOIN request_category c ON c.name = t.cat
LEFT JOIN apartment a ON a.house_id = t.h AND a.number = t.num
ORDER BY t.ago DESC;

-- сопоставление кодов тестовых сценариев с id (по началу описания)
INSERT INTO t_req
SELECT v.code, r.request_id
FROM request r JOIN (VALUES
    ('r01','Течёт стояк'), ('r02','Не горит свет на лестнице'), ('r03','Лифт в первом'),
    ('r04','Засор в кухонной'), ('r05','Холодные батареи'), ('r06','Подъезд не убирали'),
    ('r07','В квартире нет света'), ('r08','Не вывезены листья'), ('r09','Протекает потолок'),
    ('r10','Сломан доводчик'), ('r11','Медленно уходит'), ('r12','Подтекает гибкая'),
    ('r13','Не работает освещение'), ('r14','Прошу убрать снег'), ('r15','Лифт застревает'),
    ('r16','Засор унитаза'), ('r17','Разбито стекло'), ('r18','Пропадает свет')
) AS v(code, prefix) ON r.description LIKE v.prefix || '%';

DO $$
DECLARE
    q  bigint;
    q2 bigint;
BEGIN
    -- r01, r18: новые, не рассмотрены
    -- r02: принято диспетчером
    CALL accept_request(pg_temp.rq('r02'), pg_temp.uid('admin_sev'));

    -- r03: назначено механику по лифтам
    q := pg_temp.rq('r03');
    CALL accept_request(q, pg_temp.uid('admin_sev'));
    CALL assign_executor(q, pg_temp.uid('admin_sev'), pg_temp.uid('lift_sev'));

    -- r04: в работе
    q := pg_temp.rq('r04');
    CALL accept_request(q, pg_temp.uid('admin_sev'));
    CALL assign_executor(q, pg_temp.uid('admin_sev'), pg_temp.uid('plumb_sev'));
    CALL start_work(q, pg_temp.uid('plumb_sev'));

    -- r05: в работе (просрочено — норматив 24 ч, создано 60 ч назад)
    q := pg_temp.rq('r05');
    CALL accept_request(q, pg_temp.uid('admin_zhs'));
    CALL assign_executor(q, pg_temp.uid('admin_zhs'), pg_temp.uid('plumb_zhs'));
    CALL start_work(q, pg_temp.uid('plumb_zhs'));
    INSERT INTO request_comment (request_id, author_id, body)
    VALUES (q, pg_temp.uid('plumb_zhs'), 'Ждём поставку запорной арматуры, работы займут ещё 2 дня');

    -- r06: принято, но не назначено (просрочено)
    CALL accept_request(pg_temp.rq('r06'), pg_temp.uid('admin_zhs'));

    -- r07: выполнено, ожидает оценки
    q := pg_temp.rq('r07');
    CALL accept_request(q, pg_temp.uid('admin_zhs'));
    CALL assign_executor(q, pg_temp.uid('admin_zhs'), pg_temp.uid('elec_zhs'));
    CALL start_work(q, pg_temp.uid('elec_zhs'));
    CALL complete_request(q, pg_temp.uid('elec_zhs'), 'Заменён автомат в этажном щитке');

    -- r08: закрыто с оценкой 5
    q := pg_temp.rq('r08');
    CALL accept_request(q, pg_temp.uid('admin_nev'));
    CALL assign_executor(q, pg_temp.uid('admin_nev'), pg_temp.uid('clean_nev'));
    CALL start_work(q, pg_temp.uid('clean_nev'));
    CALL complete_request(q, pg_temp.uid('clean_nev'), 'Территория убрана, мусор вывезен');
    CALL rate_request(q, pg_temp.uid('komarova'), 5::smallint, 'Быстро и аккуратно');

    -- r09: житель отклонил результат (обращение отменено) и создал повторное,
    --      которое выполнено и закрыто с оценкой 3
    q := pg_temp.rq('r09');
    CALL accept_request(q, pg_temp.uid('admin_nev'));
    CALL assign_executor(q, pg_temp.uid('admin_nev'), pg_temp.uid('plumb_nev'));
    CALL start_work(q, pg_temp.uid('plumb_nev'));
    CALL complete_request(q, pg_temp.uid('plumb_nev'), 'Подтянуто соединение трубы');
    CALL reject_result(q, pg_temp.uid('orlov'), 'Потолок продолжает протекать после дождя');
    q2 := create_request(pg_temp.uid('orlov'), 5,
              (SELECT apartment_id FROM apartment WHERE house_id = 5 AND number = '6'),
              (SELECT category_id FROM request_category WHERE name = 'Протечка воды'),
              'Повторно: протекает потолок в коридоре, прошлый ремонт не помог', q);
    CALL accept_request(q2, pg_temp.uid('admin_nev'));
    CALL assign_executor(q2, pg_temp.uid('admin_nev'), pg_temp.uid('plumb_nev'));
    CALL start_work(q2, pg_temp.uid('plumb_nev'));
    CALL complete_request(q2, pg_temp.uid('plumb_nev'), 'Заменён участок водостока');
    CALL rate_request(q2, pg_temp.uid('orlov'), 3::smallint, 'Сделали только со второго раза');

    -- r10: закрыто с оценкой 4
    q := pg_temp.rq('r10');
    CALL accept_request(q, pg_temp.uid('admin_nev'));
    CALL assign_executor(q, pg_temp.uid('admin_nev'), pg_temp.uid('multi_nev'));
    CALL start_work(q, pg_temp.uid('multi_nev'));
    CALL complete_request(q, pg_temp.uid('multi_nev'), 'Установлен новый доводчик');
    CALL rate_request(q, pg_temp.uid('kiseleva'), 4::smallint, NULL);

    -- r11: закрыто с оценкой 3, было переназначение исполнителя
    q := pg_temp.rq('r11');
    CALL accept_request(q, pg_temp.uid('admin_sev'));
    CALL assign_executor(q, pg_temp.uid('admin_sev'), pg_temp.uid('clean_sev'),
                             'Сантехник в отпуске, дворник проверит и вызовет подрядчика');
    CALL set_actor(pg_temp.uid('admin_sev'), 'Сантехник вышел из отпуска');   -- снять исполнителя
    INSERT INTO request_status_history (request_id, status) VALUES (q, 'ACCEPTED');
    CALL assign_executor(q, pg_temp.uid('admin_sev'), pg_temp.uid('plumb_sev'));
    CALL start_work(q, pg_temp.uid('plumb_sev'));
    CALL complete_request(q, pg_temp.uid('plumb_sev'), 'Прочищен сифон и лежак');
    CALL rate_request(q, pg_temp.uid('ivanov'), 3::smallint, 'Нормально, но пришлось ждать');

    -- r12: отменено администратором
    q := pg_temp.rq('r12');
    CALL accept_request(q, pg_temp.uid('admin_sev'));
    CALL cancel_request(q, pg_temp.uid('admin_sev'), 'Внутриквартирное оборудование — не зона ответственности УК');

    -- r13: назначено, просрочено (норматив 72 ч, создано 250 ч назад)
    q := pg_temp.rq('r13');
    CALL accept_request(q, pg_temp.uid('admin_zhs'));
    CALL assign_executor(q, pg_temp.uid('admin_zhs'), pg_temp.uid('elec_zhs'));

    -- r14: отозвано жителем
    CALL cancel_request(pg_temp.rq('r14'), pg_temp.uid('petrova'), 'Создано по ошибке');

    -- r15: новое, просрочено (не рассмотрено 50 ч при нормативе 24 ч)
    -- r16: в работе, просрочено
    q := pg_temp.rq('r16');
    CALL accept_request(q, pg_temp.uid('admin_nev'));
    CALL assign_executor(q, pg_temp.uid('admin_nev'), pg_temp.uid('plumb_nev'));
    CALL start_work(q, pg_temp.uid('plumb_nev'));

    -- r17: выполнено
    q := pg_temp.rq('r17');
    CALL accept_request(q, pg_temp.uid('admin_zhs'));
    CALL assign_executor(q, pg_temp.uid('admin_zhs'), pg_temp.uid('clean_zhs'),
                             'Мелкий ремонт, специалист общестроительных работ отсутствует');
    CALL start_work(q, pg_temp.uid('clean_zhs'));
    CALL complete_request(q, pg_temp.uid('clean_zhs'), 'Окно временно закрыто фанерой, стекло заказано');
END $$;

-- Планировщик: отметить просроченные обращения
SELECT mark_overdue_requests() AS marked_overdue;

-- Комментарии
INSERT INTO request_comment (request_id, author_id, body)
SELECT t.request_id, pg_temp.uid(l), b
FROM t_req t JOIN (VALUES
    ('r03', 'kozlov',   'В лифте никого нет, кнопка вызова не реагирует'),
    ('r03', 'lift_sev', 'Выезжаю, буду в течение часа'),
    ('r04', 'nikitina', 'Буду дома после 18:00'),
    ('r16', 'komarova', 'Прошу ускорить, в квартире маленький ребёнок')
) AS v(code, l, b) ON v.code = t.code;

-- Вложения (метаданные; сами файлы лежат на helios в $HOME/is-app/files)
INSERT INTO attachment (request_id, comment_id, order_id, file_name, mime_type, size_bytes, storage_path, uploaded_by)
SELECT t.request_id, NULL, NULL, f, m, s, 'files/' || t.request_id || '/' || f, r.author_id
FROM t_req t JOIN request r USING (request_id)
JOIN (VALUES ('r01', 'leak.jpg', 'image/jpeg', 812345),
             ('r09', 'ceiling.jpg', 'image/jpeg', 1534000),
             ('r17', 'window.png', 'image/png', 420000)) AS v(code, f, m, s) ON v.code = t.code;

-- ---------------------------------------------------------------------
-- Жалобы и предписания
-- ---------------------------------------------------------------------
DO $$
DECLARE
    c_overdue bigint;
    c_rating  bigint;
    c_cancel  bigint;
    o1 bigint;
    o2 bigint;
BEGIN
    -- жалоба на просроченное обращение (r05, «ЖилСервис», Центральный район)
    c_overdue := file_complaint(pg_temp.uid('fomin'),
        pg_temp.rq('r05'),
        'Третьи сутки нет отопления, УК обещает, но ничего не делает');
    -- жалоба на отклонённый результат (r09, «УК Невский берег», Петроградский район)
    c_rating := file_complaint(pg_temp.uid('orlov'),
        pg_temp.rq('r09'),
        'Протечку не устранили с первого раза, потолок испорчен');
    -- жалоба на отмену обращения УК (r12)
    c_cancel := file_complaint(pg_temp.uid('sidorov'),
        pg_temp.rq('r12'),
        'УК отказалась устранять протечку, считаю отказ необоснованным');

    -- предписание «ЖилСервис» по просроченным обращениям Центрального района; отчёт принят
    o1 := issue_order(pg_temp.uid('insp_center'), 2,
        'Нарушение нормативных сроков устранения неисправностей системы отопления и уборки МОП',
        current_date + 10,
        ARRAY[pg_temp.rq('r05'),
              pg_temp.rq('r06')],
        ARRAY[c_overdue]);
    CALL submit_order_report(o1, pg_temp.uid('admin_zhs'),
        'Отопление восстановлено, график уборки подъездов утверждён и размещён на стендах');
    CALL review_order(o1, pg_temp.uid('insp_center'), true, 'Нарушение устранено, подтверждено выездом');

    -- предписание «УК Невский берег» в Петроградском районе; отчёт пока не поступил
    o2 := issue_order(pg_temp.uid('insp_petro'), 3,
        'Повторное некачественное устранение протечек кровли и засоров',
        current_date + 14,
        ARRAY[pg_temp.rq('r09'),
              pg_temp.rq('r16')],
        ARRAY[c_rating]);

    -- жалобу на отмену инспектор отклоняет: отказ УК обоснован
    CALL reject_complaint(pg_temp.uid('insp_center'), c_cancel,
        'Подводка к смесителю — внутриквартирное оборудование, ответственность собственника');
END $$;

-- ---------------------------------------------------------------------
-- Объявления
-- ---------------------------------------------------------------------
SELECT publish_announcement(pg_temp.uid('admin_sev'), 'Отключение горячей воды',
       'С 10:00 до 18:00 будет отключена горячая вода в связи с плановой опрессовкой.',
       ARRAY[1, 2]::bigint[], now() - interval '1 day', now() + interval '5 days');
SELECT publish_announcement(pg_temp.uid('admin_nev'), 'Техническое обслуживание лифтов',
       'Лифты будут остановлены поочерёдно на 2 часа для планового ТО.',
       ARRAY[5]::bigint[], now(), now() + interval '7 days');
SELECT publish_announcement(pg_temp.uid('admin_zhs'), 'Собрание собственников',
       'Приглашаем на общее собрание собственников во дворе дома.',
       ARRAY[3, 4]::bigint[]);

-- Часть уведомлений прочитана
UPDATE notification SET is_read = true WHERE notification_id % 3 = 0;

DROP TABLE t_req;
