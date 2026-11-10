-- =====================================================================
-- 08_demo.sql — сценарий демонстрации преподавателю.
-- Запуск после install.sql:  psql -h pg -d studs -f 08_demo.sql
-- Ошибки в разделе 6 ожидаемые: так показывается работа триггеров.
-- =====================================================================
\set ON_ERROR_STOP off
\pset footer off

CREATE OR REPLACE FUNCTION pg_temp.uid(p_login text) RETURNS bigint
LANGUAGE sql AS $$ SELECT user_id FROM app_user WHERE login = p_login $$;

\echo
\echo '=== 1. Таблицы базы данных ==='
\dt

\echo
\echo '=== 2. Связь M:N «житель — квартира» (таблица resident_apartment) ==='
SELECT u.full_name AS житель, h.address AS дом, a.number AS кв, ra.ownership, ra.is_confirmed
FROM resident_apartment ra
JOIN app_user u  ON u.user_id = ra.user_id
JOIN apartment a ON a.apartment_id = ra.apartment_id
JOIN house h     ON h.house_id = a.house_id
WHERE u.login IN ('ivanov', 'petrova')
ORDER BY 1, 2, 3;

\echo
\echo '=== 3. Конечный автомат: допустимые переходы статусов ==='
SELECT from_status, to_status, actor_role FROM status_transition ORDER BY 1, 2;

\echo
\echo '=== 4. Полный цикл обращения через процедуры ==='
\echo '--- 4.1 Житель Иванов создаёт обращение о протечке (create_request)'
SELECT create_request(pg_temp.uid('ivanov'), 1,
       (SELECT apartment_id FROM apartment WHERE house_id = 1 AND number = '5'),
       (SELECT category_id FROM request_category WHERE name = 'Протечка воды'),
       'Демонстрация: течёт кран в ванной') AS new_request_id \gset

SELECT request_id, status, created_at::timestamp(0), deadline_at::timestamp(0) AS крайний_срок
FROM v_request WHERE request_id = :new_request_id;

\echo '--- 4.2 Диспетчер принимает и смотрит подходящих исполнителей'
CALL accept_request(:new_request_id, pg_temp.uid('admin_sev'));
SELECT * FROM suggest_executors(:new_request_id);

\echo '--- 4.3 Назначение, выполнение, оценка'
CALL assign_executor(:new_request_id, pg_temp.uid('admin_sev'), pg_temp.uid('plumb_sev'));
CALL start_work(:new_request_id, pg_temp.uid('plumb_sev'));
CALL complete_request(:new_request_id, pg_temp.uid('plumb_sev'), 'Заменена прокладка крана');
CALL rate_request(:new_request_id, pg_temp.uid('ivanov'), 5::smallint, 'Спасибо!');

\echo '--- 4.4 Статусы обращения — записи журнала, последняя запись = текущий статус'
SELECT h.status, u.login AS кто, h.changed_at::timestamp(0), h.comment
FROM request_status_history h LEFT JOIN app_user u ON u.user_id = h.changed_by
WHERE h.request_id = :new_request_id ORDER BY h.history_id;

\echo '--- 4.5 Уведомления, созданные триггером'
SELECT u.login, n.event FROM notification n JOIN app_user u USING (user_id)
WHERE n.entity_type = 'REQUEST' AND n.entity_id = :new_request_id ORDER BY n.notification_id;

\echo
\echo '=== 5. Повторное обращение взамен отклонённого ==='
SELECT r.request_id, r.status, r.previous_request_id, left(r.description, 45) AS описание
FROM v_request r WHERE r.author_id = pg_temp.uid('orlov') ORDER BY 1;

\echo
\echo '=== 6. Триггеры не дают нарушить правила (ожидаются ОШИБКИ) ==='
\echo '--- 6.1 Диспетчер пытается перескочить из NEW сразу в DONE'
BEGIN;
CALL set_actor(pg_temp.uid('admin_sev'));
INSERT INTO request_status_history (request_id, status)
SELECT min(request_id), 'DONE' FROM v_request WHERE status = 'NEW' AND house_id = 1;
ROLLBACK;

\echo '--- 6.2 Вернуть выполненное обращение в работу (запрещено после замечаний)'
BEGIN;
CALL set_actor(pg_temp.uid('tarasov'));
INSERT INTO request_status_history (request_id, status)
SELECT request_id, 'IN_PROGRESS' FROM v_request WHERE status = 'DONE' AND author_id = pg_temp.uid('tarasov');
ROLLBACK;

\echo '--- 6.3 Изменить закрытое обращение'
BEGIN;
CALL set_actor(pg_temp.uid('ivanov'));
UPDATE request SET description = 'Хочу переписать историю' WHERE request_id = :new_request_id;
ROLLBACK;

\echo '--- 6.4 Подделать журнал статусов'
DELETE FROM request_status_history WHERE request_id = :new_request_id;

\echo '--- 6.5 Передать показание меньше предыдущего'
SELECT submit_meter_reading(
    (SELECT min(m.meter_id) FROM meter m JOIN resident_apartment ra USING (apartment_id)
     WHERE ra.user_id = pg_temp.uid('ivanov')),
    pg_temp.uid('ivanov'), 1);

\echo '--- 6.6 Оценка вне диапазона 1–5 (CHECK)'
BEGIN;
CALL set_actor(pg_temp.uid('tarasov'));
INSERT INTO rating (request_id, score)
SELECT request_id, 7 FROM v_request WHERE status = 'DONE' AND author_id = pg_temp.uid('tarasov');
ROLLBACK;

\echo
\echo '=== 7. Инспектор ГЖИ: просроченные обращения района ==='
SELECT mark_overdue_requests() AS отмечено_новых_просроченных;
SELECT request_id, company, category, status, hours_overdue, has_complaint
FROM inspector_overdue_requests(pg_temp.uid('insp_center'));

\echo
\echo '=== 8. Индексы ==='
SELECT indexname, indexdef FROM pg_indexes
WHERE schemaname = current_schema() AND indexname LIKE 'idx_%' ORDER BY 1;

\echo '--- План запроса «мои обращения» жителя.'
\echo '--- Данных мало, поэтому полный просмотр временно запрещён, чтобы показать, что индекс применим.'
SELECT pg_temp.uid('ivanov') AS ivanov_id, pg_temp.uid('plumb_sev') AS plumb_id \gset
SET jit = off;
SET enable_seqscan = off;
EXPLAIN SELECT request_id, status FROM v_request
WHERE author_id = :ivanov_id ORDER BY created_at DESC LIMIT 20;
EXPLAIN SELECT count(*) FROM v_request
WHERE executor_id = :plumb_id AND status IN ('ASSIGNED', 'IN_PROGRESS');
RESET enable_seqscan;
