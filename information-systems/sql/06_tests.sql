-- =====================================================================
-- 06_tests.sql — проверка ограничений целостности и триггеров.
-- Каждый тест пытается нарушить правило; ожидается ошибка.
-- Изменения тестов не сохраняются (блок откатывается).
-- =====================================================================
SET client_min_messages TO notice;

CREATE OR REPLACE FUNCTION pg_temp.uid(p_login text) RETURNS bigint
LANGUAGE sql AS $$ SELECT user_id FROM app_user WHERE login = p_login $$;

CREATE OR REPLACE FUNCTION pg_temp.expect_error(p_name text, p_sql text) RETURNS text
LANGUAGE plpgsql AS $$
BEGIN
    EXECUTE p_sql;
    RETURN 'FAIL  ' || p_name || ' — ошибки не было';
EXCEPTION WHEN OTHERS THEN
    RETURN 'OK    ' || p_name || ' — ' || SQLERRM;
END $$;

SELECT result FROM (VALUES
 (1, pg_temp.expect_error('Номер квартиры уникален в доме',
     $q$INSERT INTO apartment (house_id, number) VALUES (1, '5')$q$)),
 (2, pg_temp.expect_error('Оценка в диапазоне 1–5',
     format($q$CALL set_actor(%s); INSERT INTO rating (request_id, score) SELECT request_id, 6 FROM v_request WHERE status = 'DONE' AND author_id = %s$q$,
            pg_temp.uid('tarasov'), pg_temp.uid('tarasov')))),
 (3, pg_temp.expect_error('Обращение только от подтверждённого жителя',
     format($q$SELECT create_request(%s, 6, NULL, 1, 'Проверка неподтверждённого жителя')$q$, pg_temp.uid('newbie')))),
 (4, pg_temp.expect_error('Обращение только по своему дому',
     format($q$SELECT create_request(%s, 6, NULL, 1, 'Житель чужого дома пишет обращение')$q$, pg_temp.uid('ivanov')))),
 (5, pg_temp.expect_error('Квартира должна относиться к дому',
     format($q$SELECT create_request(%s, 2, (SELECT apartment_id FROM apartment WHERE house_id = 1 AND number = '5'), 1, 'Квартира из другого дома')$q$, pg_temp.uid('ivanov')))),
 (6, pg_temp.expect_error('Недопустимый переход NEW -> DONE',
     format($q$CALL set_actor(%s); INSERT INTO request_status_history (request_id, status) SELECT min(request_id), 'DONE' FROM v_request WHERE status = 'NEW' AND house_id = 1$q$,
            pg_temp.uid('admin_sev')))),
 (7, pg_temp.expect_error('Администратор чужой УК не может принять обращение',
     format($q$CALL accept_request(%s, %s)$q$, (SELECT min(request_id) FROM v_request WHERE status = 'NEW' AND house_id = 1), pg_temp.uid('admin_nev')))),
 (8, pg_temp.expect_error('Исполнитель — сотрудник УК, обслуживающей дом',
     format($q$CALL assign_executor(%s, %s, %s, 'тест')$q$,
            (SELECT min(request_id) FROM v_request WHERE status = 'ACCEPTED' AND house_id = 1), pg_temp.uid('admin_sev'), pg_temp.uid('plumb_zhs')))),
 (9, pg_temp.expect_error('Назначение не по специализации без обоснования',
     format($q$CALL assign_executor(%s, %s, %s)$q$,
            (SELECT min(request_id) FROM v_request WHERE status = 'ACCEPTED' AND house_id = 1), pg_temp.uid('admin_sev'), pg_temp.uid('clean_sev')))),
 (10, pg_temp.expect_error('Сотрудник меняет статус только своего обращения',
     format($q$CALL start_work(%s, %s)$q$, (SELECT min(request_id) FROM v_request WHERE status = 'ASSIGNED' AND house_id = 2), pg_temp.uid('plumb_sev')))),
 (11, pg_temp.expect_error('Закрытое обращение не изменяется',
     format($q$CALL set_actor(%s); UPDATE request SET description = 'Попытка изменить закрытое обращение' WHERE request_id IN (SELECT request_id FROM v_request WHERE status = 'CLOSED')$q$, pg_temp.uid('admin_nev')))),
 (12, pg_temp.expect_error('Оценить можно только выполненное обращение',
     format($q$CALL rate_request(%s, %s, 5::smallint)$q$,
            (SELECT min(request_id) FROM v_request WHERE status = 'IN_PROGRESS' AND author_id = pg_temp.uid('nikitina')), pg_temp.uid('nikitina')))),
 (13, pg_temp.expect_error('Оценку ставит только автор',
     format($q$CALL rate_request(%s, %s, 5::smallint)$q$, (SELECT min(request_id) FROM v_request WHERE status = 'DONE' AND house_id = 4), pg_temp.uid('belov')))),
 (14, pg_temp.expect_error('Журнал статусов не изменяется',
     $q$UPDATE request_status_history SET comment = 'подделка' WHERE history_id = 1$q$)),
 (15, pg_temp.expect_error('Журнал статусов не удаляется',
     $q$DELETE FROM request_status_history WHERE history_id = 1$q$)),
 (16, pg_temp.expect_error('Показание не меньше предыдущего',
     format($q$SELECT submit_meter_reading(%s, %s, 1)$q$, (SELECT min(meter_id) FROM meter JOIN resident_apartment USING (apartment_id) WHERE user_id = pg_temp.uid('ivanov')), pg_temp.uid('ivanov')))),
 (17, pg_temp.expect_error('Показания передаёт житель своей квартиры',
     format($q$SELECT submit_meter_reading(%s, %s, 999999)$q$, (SELECT min(meter_id) FROM meter JOIN resident_apartment USING (apartment_id) WHERE user_id = pg_temp.uid('ivanov')), pg_temp.uid('kozlov')))),
 (18, pg_temp.expect_error('Показание не может быть в будущем',
     $q$INSERT INTO meter_reading (meter_id, reading_date, value) VALUES (1, current_date + 1, 999999)$q$)),
 (19, pg_temp.expect_error('Жалоба без нарушения со стороны УК',
     format($q$SELECT file_complaint(%s, (SELECT min(request_id) FROM v_request WHERE author_id = %s AND status = 'CLOSED'), 'Хочу пожаловаться без причины')$q$,
            pg_temp.uid('komarova'), pg_temp.uid('komarova')))),
 (20, pg_temp.expect_error('Жалоба только по своему обращению',
     format($q$SELECT file_complaint(%s, (SELECT request_id FROM v_request WHERE is_overdue LIMIT 1), 'Жалоба на чужое обращение')$q$, pg_temp.uid('newbie')))),
 (21, pg_temp.expect_error('Одна жалоба на обращение',
     format($q$SELECT file_complaint(resident_id, request_id, 'Повторная жалоба по тому же обращению') FROM complaint LIMIT 1$q$))),
 (22, pg_temp.expect_error('Срок предписания позже даты выдачи',
     format($q$SELECT issue_order(%s, 2, 'нарушение', current_date, '{}')$q$, pg_temp.uid('insp_center')))),
 (23, pg_temp.expect_error('Вложение принадлежит ровно одному объекту',
     format($q$INSERT INTO attachment (file_name, mime_type, size_bytes, storage_path, uploaded_by) VALUES ('a.jpg', 'image/jpeg', 10, 'x/a.jpg', %s)$q$, pg_temp.uid('ivanov')))),
 (24, pg_temp.expect_error('Размер вложения не более 5 МБ',
     format($q$INSERT INTO attachment (request_id, file_name, mime_type, size_bytes, storage_path, uploaded_by) VALUES (1, 'a.jpg', 'image/jpeg', 6000000, 'x/b.jpg', %s)$q$, pg_temp.uid('ivanov')))),
 (25, pg_temp.expect_error('Возврат выполненного обращения в работу запрещён',
     format($q$CALL set_actor(%s); INSERT INTO request_status_history (request_id, status) SELECT request_id, 'IN_PROGRESS' FROM v_request WHERE status = 'DONE' AND author_id = %s$q$,
            pg_temp.uid('tarasov'), pg_temp.uid('tarasov')))),
 (27, pg_temp.expect_error('Исполнитель меняется только у принятого обращения',
     format($q$CALL set_actor(%s); UPDATE request SET executor_id = %s WHERE request_id = (SELECT min(request_id) FROM v_request WHERE status = 'IN_PROGRESS' AND house_id = 2)$q$,
            pg_temp.uid('admin_sev'), pg_temp.uid('lift_sev')))),
 (26, pg_temp.expect_error('Повторное обращение — только взамен своего отменённого',
     format($q$SELECT create_request(%s, 1, NULL, 1, 'Повторное обращение к чужому обращению', (SELECT min(request_id) FROM v_request WHERE status = 'CANCELLED' AND author_id <> %s))$q$,
            pg_temp.uid('ivanov'), pg_temp.uid('ivanov'))))
) AS t(n, result) ORDER BY n;
