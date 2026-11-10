-- =====================================================================
-- 07_load_test.sql — генерация большого объёма данных для проверки индексов.
-- Запускается ПОСЛЕ 05_fill.sql и только на тестовой копии базы.
-- Пользовательские триггеры на время массовой загрузки отключаются
-- (это может сделать владелец таблиц без прав суперпользователя).
-- =====================================================================

ALTER TABLE request DISABLE TRIGGER USER;
ALTER TABLE request_status_history DISABLE TRIGGER USER;

-- 500 дополнительных домов
INSERT INTO house (company_id, district_id, address, build_year, floors)
SELECT 1 + g % 3, 1 + g % 2, 'Тестовый пр., ' || g, 1960 + g % 60, 5 + g % 20
FROM generate_series(1, 500) AS g;

-- 300 000 обращений за 5 лет: ~95 % закрыты или отменены, остальные открыты
CREATE TEMP TABLE gen AS
WITH params AS (
    SELECT g,
           (SELECT array_agg(house_id) FROM house) AS houses,
           (SELECT array_agg(user_id) FROM app_user WHERE role = 'RESIDENT') AS residents,
           (SELECT array_agg(user_id) FROM app_user WHERE role = 'EMPLOYEE') AS employees,
           now() - make_interval(secs => random() * 5 * 365 * 86400) AS created
    FROM generate_series(1, 300000) AS g
)
SELECT g, created,
       houses[1 + (random() * (cardinality(houses) - 1))::int] AS house_id,
       residents[1 + (random() * (cardinality(residents) - 1))::int] AS author_id,
       employees[1 + (random() * (cardinality(employees) - 1))::int] AS executor_id,
       1 + (random() * 8)::int AS category_id,
       CASE WHEN created < now() - interval '30 days' THEN
                 CASE WHEN random() < 0.97 THEN 'CLOSED' ELSE 'CANCELLED' END
            ELSE (ARRAY['NEW','ACCEPTED','ASSIGNED','IN_PROGRESS','DONE','CLOSED','CLOSED','CLOSED'])
                 [1 + (random() * 7)::int] END::request_status AS status
FROM params;

INSERT INTO request (author_id, house_id, apartment_id, category_id, description, executor_id,
                     result_text, created_at, deadline_at, is_overdue, updated_at)
SELECT s.author_id, s.house_id, NULL, s.category_id, 'Сгенерированное обращение №' || s.g,
       CASE WHEN s.status IN ('NEW','ACCEPTED','CANCELLED') THEN NULL ELSE s.executor_id END,
       CASE WHEN s.status IN ('DONE','CLOSED') THEN 'Работы выполнены' END,
       s.created,
       s.created + make_interval(hours => c.norm_hours),
       s.status NOT IN ('DONE','CLOSED','CANCELLED') AND s.created + make_interval(hours => c.norm_hours) < now()
           AND random() < 0.5,
       s.created
FROM gen s JOIN request_category c USING (category_id)
ORDER BY s.g;

-- журнал статусов: путь от NEW до итогового статуса, по часу на переход
INSERT INTO request_status_history (request_id, status, changed_by, changed_at)
SELECT r.request_id, p.st, NULL, r.created_at + (p.k - 1) * interval '1 hour'
FROM request r
JOIN gen s ON r.description = 'Сгенерированное обращение №' || s.g
CROSS JOIN LATERAL unnest(CASE s.status
        WHEN 'NEW'         THEN '{NEW}'
        WHEN 'ACCEPTED'    THEN '{NEW,ACCEPTED}'
        WHEN 'ASSIGNED'    THEN '{NEW,ACCEPTED,ASSIGNED}'
        WHEN 'IN_PROGRESS' THEN '{NEW,ACCEPTED,ASSIGNED,IN_PROGRESS}'
        WHEN 'DONE'        THEN '{NEW,ACCEPTED,ASSIGNED,IN_PROGRESS,DONE}'
        WHEN 'CLOSED'      THEN '{NEW,ACCEPTED,ASSIGNED,IN_PROGRESS,DONE,CLOSED}'
        ELSE                    '{NEW,CANCELLED}' END::request_status[]) WITH ORDINALITY AS p(st, k)
ORDER BY r.request_id, p.k;

-- 500 000 уведомлений, 90 % прочитаны
INSERT INTO notification (user_id, event, entity_type, entity_id, is_read, created_at)
SELECT 1 + (random() * 27)::int, 'STATUS_CHANGED', 'REQUEST', g, random() < 0.9,
       now() - make_interval(secs => random() * 365 * 86400)
FROM generate_series(1, 500000) AS g;

ALTER TABLE request ENABLE TRIGGER USER;
ALTER TABLE request_status_history ENABLE TRIGGER USER;

ANALYZE house, request, request_status_history, notification;
