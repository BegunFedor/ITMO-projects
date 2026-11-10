-- =====================================================================
-- 04_indexes.sql — индексы под запросы прецедентов П1–П8 (15 индексов).
-- PostgreSQL автоматически индексирует только PRIMARY KEY и UNIQUE;
-- внешние ключи и поля фильтров индексируются явно.
-- =====================================================================

-- Текущий статус обращения: последняя запись журнала (используется во всех запросах
-- со статусом — функция request_status, представление v_request) (П1–П8)
CREATE INDEX idx_history_request_last ON request_status_history (request_id, history_id DESC);

-- Автозакрытие неоценённых обращений: переходы в DONE в окне от 7 до 37 дней назад (бизнес-процесс 3)
CREATE INDEX idx_history_done ON request_status_history (changed_at) WHERE status = 'DONE';

-- Очередь обращений УК / дома с сортировкой по дате (П2, П8)
CREATE INDEX idx_request_house ON request (house_id, created_at DESC);

-- «Мои обращения» жителя (П1, П4, П7)
CREATE INDEX idx_request_author ON request (author_id, created_at DESC);

-- Активные обращения исполнителя: список задач и загрузка при подборе (П2, П3).
-- Исполнитель есть, а результата ещё нет только в статусах ASSIGNED и IN_PROGRESS
-- (результат записывается при переходе в DONE, при снятии исполнитель обнуляется).
CREATE INDEX idx_request_executor_active ON request (executor_id) WHERE result_text IS NULL;

-- Планировщик: ещё не просроченные обращения, срок которых истёк за последние 7 дней (бизнес-процесс 4)
CREATE INDEX idx_request_open_deadline ON request (deadline_at) WHERE NOT is_overdue;

-- Панель инспектора: просроченные обращения по домам района (П8)
CREATE INDEX idx_request_overdue_house ON request (house_id, deadline_at) WHERE is_overdue;

-- Карточка обращения: переписка (П1, П3)
CREATE INDEX idx_comment_request ON request_comment (request_id, created_at);

-- Дома УК (администратор) и дома района (инспектор) (П2, П8)
CREATE INDEX idx_house_company  ON house (company_id);
CREATE INDEX idx_house_district ON house (district_id);

-- Жители квартиры (обратное направление M:N): проверка права жителя,
-- рассылка объявлений (П1, П5, П6)
CREATE INDEX idx_resident_apartment_apartment ON resident_apartment (apartment_id)
    WHERE is_confirmed;

-- Лента объявлений дома (обратное направление M:N) (П6)
CREATE INDEX idx_announcement_house_house ON announcement_house (house_id);

-- Предписания по обращению (обратное направление M:N) (П8)
CREATE INDEX idx_order_request_request ON order_request (request_id);

-- Приборы учёта квартиры (П5)
CREATE INDEX idx_meter_apartment ON meter (apartment_id);

-- Непрочитанные уведомления пользователя
CREATE INDEX idx_notification_unread ON notification (user_id, created_at DESC) WHERE NOT is_read;
