-- =====================================================================
-- ИС обработки обращений жителей МКД. Этап 2.
-- 01_create.sql — создание типов, таблиц и декларативных ограничений
-- Объекты создаются в текущей схеме пользователя (на helios — схема с именем логина)
-- Запуск:  psql -h pg -d studs -f install.sql   (или по отдельности)
-- =====================================================================


-- ---------------------------------------------------------------------
-- Перечислимые типы
-- ---------------------------------------------------------------------
CREATE TYPE user_role       AS ENUM ('RESIDENT', 'EMPLOYEE', 'COMPANY_ADMIN', 'INSPECTOR');
CREATE TYPE ownership_type  AS ENUM ('OWNER', 'TENANT');
CREATE TYPE request_status  AS ENUM ('NEW', 'ACCEPTED', 'ASSIGNED', 'IN_PROGRESS',
                                     'DONE', 'CLOSED', 'CANCELLED');
CREATE TYPE resource_type   AS ENUM ('COLD_WATER', 'HOT_WATER', 'ELECTRICITY', 'HEAT', 'GAS');
CREATE TYPE complaint_status AS ENUM ('NEW', 'UNDER_REVIEW', 'REJECTED', 'ORDER_ISSUED');
CREATE TYPE order_status    AS ENUM ('ISSUED', 'REPORT_SUBMITTED', 'FULFILLED',
                                     'NOT_FULFILLED', 'OVERDUE');

-- ---------------------------------------------------------------------
-- Жилой фонд и надзор
-- ---------------------------------------------------------------------
CREATE TABLE management_company (
    company_id      bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name            varchar(200) NOT NULL,
    inn             char(10)     NOT NULL UNIQUE CHECK (inn ~ '^[0-9]{10}$'),
    address         varchar(300) NOT NULL,
    dispatch_phone  varchar(20)  NOT NULL CHECK (dispatch_phone ~ '^\+?[0-9() -]{5,20}$')
);
COMMENT ON TABLE management_company IS 'Управляющая организация (УК)';

CREATE TABLE district (
    district_id     bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name            varchar(100) NOT NULL UNIQUE
);
COMMENT ON TABLE district IS 'Район государственного жилищного надзора';

CREATE TABLE house (
    house_id        bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    company_id      bigint NOT NULL REFERENCES management_company ON DELETE RESTRICT,
    district_id     bigint NOT NULL REFERENCES district ON DELETE RESTRICT,
    address         varchar(300) NOT NULL UNIQUE,
    build_year      smallint CHECK (build_year BETWEEN 1800 AND 2100),
    floors          smallint NOT NULL CHECK (floors BETWEEN 1 AND 200)
);
COMMENT ON TABLE house IS 'Многоквартирный дом';

CREATE TABLE apartment (
    apartment_id    bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    house_id        bigint NOT NULL REFERENCES house ON DELETE CASCADE,
    number          varchar(10) NOT NULL,
    entrance        smallint CHECK (entrance > 0),
    floor           smallint,
    area_m2         numeric(7,2) CHECK (area_m2 > 0),
    CONSTRAINT apartment_number_unique UNIQUE (house_id, number)
);
COMMENT ON TABLE apartment IS 'Квартира; номер уникален в пределах дома';

-- ---------------------------------------------------------------------
-- Пользователи и роли
-- ---------------------------------------------------------------------
CREATE TABLE app_user (
    user_id         bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    login           varchar(50)  NOT NULL UNIQUE CHECK (login ~ '^[A-Za-z0-9_.-]{3,50}$'),
    password_hash   varchar(200) NOT NULL,
    full_name       varchar(150) NOT NULL CHECK (length(trim(full_name)) > 0),
    phone           varchar(20),
    email           varchar(100) CHECK (email ~ '^[^@\s]+@[^@\s]+\.[^@\s]+$'),
    role            user_role    NOT NULL,
    is_active       boolean      NOT NULL DEFAULT true,
    created_at      timestamptz  NOT NULL DEFAULT now()
);
COMMENT ON TABLE app_user IS 'Учётная запись пользователя любой роли';

-- Житель <-> квартира (многие-ко-многим)
CREATE TABLE resident_apartment (
    user_id         bigint NOT NULL REFERENCES app_user ON DELETE CASCADE,
    apartment_id    bigint NOT NULL REFERENCES apartment ON DELETE CASCADE,
    ownership       ownership_type NOT NULL,
    is_confirmed    boolean NOT NULL DEFAULT false,
    linked_at       timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, apartment_id)
);
COMMENT ON TABLE resident_apartment IS 'Связь жителя с квартирой (M:N); подтверждается администратором УК';

-- Сотрудники и администраторы УК
CREATE TABLE company_staff (
    user_id         bigint PRIMARY KEY REFERENCES app_user ON DELETE CASCADE,
    company_id      bigint NOT NULL REFERENCES management_company ON DELETE RESTRICT,
    position        varchar(100) NOT NULL
);
COMMENT ON TABLE company_staff IS 'Работник УК (роль EMPLOYEE или COMPANY_ADMIN)';

CREATE TABLE specialization (
    specialization_id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name            varchar(100) NOT NULL UNIQUE
);

-- Сотрудник <-> специализация (многие-ко-многим)
CREATE TABLE staff_specialization (
    user_id           bigint NOT NULL REFERENCES company_staff ON DELETE CASCADE,
    specialization_id bigint NOT NULL REFERENCES specialization ON DELETE RESTRICT,
    PRIMARY KEY (user_id, specialization_id)
);

CREATE TABLE inspector (
    user_id         bigint PRIMARY KEY REFERENCES app_user ON DELETE CASCADE,
    district_id     bigint NOT NULL REFERENCES district ON DELETE RESTRICT,
    service_number  varchar(20) NOT NULL UNIQUE
);
COMMENT ON TABLE inspector IS 'Инспектор государственной жилищной инспекции';

-- ---------------------------------------------------------------------
-- Обращения
-- ---------------------------------------------------------------------
CREATE TABLE request_category (
    category_id       bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name              varchar(100) NOT NULL UNIQUE,
    specialization_id bigint NOT NULL REFERENCES specialization ON DELETE RESTRICT,
    norm_hours        integer NOT NULL CHECK (norm_hours > 0),
    is_emergency      boolean NOT NULL DEFAULT false,
    is_active         boolean NOT NULL DEFAULT true
);
COMMENT ON TABLE request_category IS 'Категория обращения: требуемая специализация и нормативный срок';

-- Справочник допустимых переходов статуса (конечный автомат)
CREATE TABLE status_transition (
    from_status     request_status NOT NULL,
    to_status       request_status NOT NULL,
    actor_role      varchar(20)    NOT NULL
                    CHECK (actor_role IN ('RESIDENT','EMPLOYEE','COMPANY_ADMIN','SYSTEM')),
    PRIMARY KEY (from_status, to_status, actor_role),
    CHECK (from_status <> to_status)
);
COMMENT ON TABLE status_transition IS 'Допустимые переходы статуса обращения и роль, которая может их выполнить';

CREATE TABLE request (
    request_id      bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    author_id       bigint NOT NULL REFERENCES app_user ON DELETE RESTRICT,
    house_id        bigint NOT NULL REFERENCES house ON DELETE RESTRICT,
    apartment_id    bigint REFERENCES apartment ON DELETE RESTRICT,   -- NULL = общее имущество дома
    previous_request_id bigint REFERENCES request ON DELETE SET NULL, -- повторное обращение взамен отменённого
    category_id     bigint NOT NULL REFERENCES request_category ON DELETE RESTRICT,
    description     text   NOT NULL CHECK (length(trim(description)) >= 10),
    executor_id     bigint REFERENCES app_user ON DELETE RESTRICT,
    result_text     text,
    created_at      timestamptz NOT NULL DEFAULT now(),
    deadline_at     timestamptz NOT NULL,
    is_overdue      boolean NOT NULL DEFAULT false,
    updated_at      timestamptz NOT NULL DEFAULT now(),
    version         integer NOT NULL DEFAULT 0,
    CONSTRAINT request_deadline_after_create CHECK (deadline_at > created_at)
);
COMMENT ON TABLE request IS 'Обращение жителя; текущий статус — последняя запись request_status_history';

-- Статус обращения хранится только здесь: каждая запись — переход в статус status в момент changed_at.
-- Текущий статус — запись с наибольшим history_id.
CREATE TABLE request_status_history (
    history_id      bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    request_id      bigint NOT NULL REFERENCES request ON DELETE CASCADE,
    status          request_status NOT NULL,
    changed_at      timestamptz NOT NULL DEFAULT now(),
    changed_by      bigint REFERENCES app_user ON DELETE SET NULL,  -- NULL = планировщик
    comment         text
);
COMMENT ON TABLE request_status_history IS 'Статусы обращения: неизменяемый журнал, последняя запись — текущий статус';

CREATE TABLE request_comment (
    comment_id      bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    request_id      bigint NOT NULL REFERENCES request ON DELETE CASCADE,
    author_id       bigint NOT NULL REFERENCES app_user ON DELETE RESTRICT,
    body            text   NOT NULL CHECK (length(trim(body)) > 0),
    created_at      timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE rating (
    request_id      bigint PRIMARY KEY REFERENCES request ON DELETE CASCADE,  -- не более одной оценки
    score           smallint NOT NULL CHECK (score BETWEEN 1 AND 5),
    review          text,
    created_at      timestamptz NOT NULL DEFAULT now()
);

-- ---------------------------------------------------------------------
-- Надзор: жалобы и предписания
-- ---------------------------------------------------------------------
CREATE TABLE inspection_order (
    order_id        bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    number          varchar(30) NOT NULL UNIQUE,
    inspector_id    bigint NOT NULL REFERENCES inspector ON DELETE RESTRICT,
    company_id      bigint NOT NULL REFERENCES management_company ON DELETE RESTRICT,
    violation       text   NOT NULL CHECK (length(trim(violation)) > 0),
    issued_at       timestamptz NOT NULL DEFAULT now(),
    due_date        date   NOT NULL,
    status          order_status NOT NULL DEFAULT 'ISSUED',
    report_text     text,
    report_at       timestamptz,
    reviewed_at     timestamptz,
    review_comment  text,
    CONSTRAINT order_due_after_issue CHECK (due_date > issued_at::date),
    CONSTRAINT order_report_consistent
        CHECK ((report_text IS NULL) = (report_at IS NULL)),
    CONSTRAINT order_report_required
        CHECK (status IN ('ISSUED','OVERDUE') OR report_text IS NOT NULL),
    CONSTRAINT order_reviewed_consistent
        CHECK ((status IN ('FULFILLED','NOT_FULFILLED')) = (reviewed_at IS NOT NULL))
);
COMMENT ON TABLE inspection_order IS 'Предписание ГЖИ управляющей организации';

-- Предписание <-> обращение (многие-ко-многим)
CREATE TABLE order_request (
    order_id        bigint NOT NULL REFERENCES inspection_order ON DELETE CASCADE,
    request_id      bigint NOT NULL REFERENCES request ON DELETE RESTRICT,
    PRIMARY KEY (order_id, request_id)
);

CREATE TABLE complaint (
    complaint_id    bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    resident_id     bigint NOT NULL REFERENCES app_user ON DELETE RESTRICT,
    request_id      bigint NOT NULL UNIQUE REFERENCES request ON DELETE RESTRICT, -- одна жалоба на обращение
    body            text   NOT NULL CHECK (length(trim(body)) >= 10),
    status          complaint_status NOT NULL DEFAULT 'NEW',
    inspector_id    bigint REFERENCES inspector ON DELETE SET NULL,
    order_id        bigint REFERENCES inspection_order ON DELETE SET NULL,
    decision_comment text,
    created_at      timestamptz NOT NULL DEFAULT now(),
    decided_at      timestamptz,
    CONSTRAINT complaint_order_consistent
        CHECK ((status = 'ORDER_ISSUED') = (order_id IS NOT NULL)),
    CONSTRAINT complaint_decision_consistent
        CHECK ((status IN ('REJECTED','ORDER_ISSUED')) = (decided_at IS NOT NULL)),
    CONSTRAINT complaint_rejection_reason
        CHECK (status <> 'REJECTED' OR decision_comment IS NOT NULL)
);
COMMENT ON TABLE complaint IS 'Жалоба жителя в ГЖИ на действия УК';

-- ---------------------------------------------------------------------
-- Вложения (к обращению, комментарию или отчёту по предписанию)
-- ---------------------------------------------------------------------
CREATE TABLE attachment (
    attachment_id   bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    request_id      bigint REFERENCES request ON DELETE CASCADE,
    comment_id      bigint REFERENCES request_comment ON DELETE CASCADE,
    order_id        bigint REFERENCES inspection_order ON DELETE CASCADE,
    file_name       varchar(255) NOT NULL,
    mime_type       varchar(50)  NOT NULL
                    CHECK (mime_type IN ('image/jpeg','image/png','application/pdf')),
    size_bytes      integer NOT NULL CHECK (size_bytes > 0 AND size_bytes <= 5 * 1024 * 1024),
    storage_path    varchar(500) NOT NULL UNIQUE,
    uploaded_by     bigint NOT NULL REFERENCES app_user ON DELETE RESTRICT,
    uploaded_at     timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT attachment_single_owner CHECK (num_nonnulls(request_id, comment_id, order_id) = 1)
);

-- ---------------------------------------------------------------------
-- Объявления
-- ---------------------------------------------------------------------
CREATE TABLE announcement (
    announcement_id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    company_id      bigint NOT NULL REFERENCES management_company ON DELETE CASCADE,
    author_id       bigint NOT NULL REFERENCES app_user ON DELETE RESTRICT,
    title           varchar(200) NOT NULL CHECK (length(trim(title)) > 0),
    body            text NOT NULL,
    visible_from    timestamptz NOT NULL DEFAULT now(),
    visible_to      timestamptz,
    created_at      timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT announcement_period CHECK (visible_to IS NULL OR visible_to > visible_from)
);

-- Объявление <-> дом (многие-ко-многим)
CREATE TABLE announcement_house (
    announcement_id bigint NOT NULL REFERENCES announcement ON DELETE CASCADE,
    house_id        bigint NOT NULL REFERENCES house ON DELETE CASCADE,
    PRIMARY KEY (announcement_id, house_id)
);

-- ---------------------------------------------------------------------
-- Приборы учёта
-- ---------------------------------------------------------------------
CREATE TABLE meter (
    meter_id        bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    apartment_id    bigint NOT NULL REFERENCES apartment ON DELETE CASCADE,
    resource        resource_type NOT NULL,
    serial_number   varchar(30) NOT NULL,
    verified_until  date,
    is_active       boolean NOT NULL DEFAULT true,
    CONSTRAINT meter_serial_unique UNIQUE (resource, serial_number)
);

CREATE TABLE meter_reading (
    reading_id      bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    meter_id        bigint NOT NULL REFERENCES meter ON DELETE CASCADE,
    reading_date    date   NOT NULL DEFAULT current_date,
    value           numeric(12,3) NOT NULL CHECK (value >= 0),
    submitted_by    bigint REFERENCES app_user ON DELETE SET NULL,
    created_at      timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT reading_one_per_day UNIQUE (meter_id, reading_date),
    CONSTRAINT reading_not_future  CHECK (reading_date <= created_at::date)
);

-- ---------------------------------------------------------------------
-- Уведомления
-- ---------------------------------------------------------------------
CREATE TABLE notification (
    notification_id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id         bigint NOT NULL REFERENCES app_user ON DELETE CASCADE,
    event           varchar(30) NOT NULL
                    CHECK (event IN ('REQUEST_CREATED','STATUS_CHANGED','REQUEST_ASSIGNED','RESULT_REJECTED',
                                     'COMPLAINT_FILED','ORDER_ISSUED','ORDER_REPORTED','ORDER_REVIEWED',
                                     'ANNOUNCEMENT_PUBLISHED','RESIDENT_CONFIRMED')),
    entity_type     varchar(20) NOT NULL
                    CHECK (entity_type IN ('REQUEST','ANNOUNCEMENT','COMPLAINT','ORDER','APARTMENT')),
    entity_id       bigint NOT NULL,
    status          request_status,
    is_read         boolean NOT NULL DEFAULT false,
    created_at      timestamptz NOT NULL DEFAULT now()
);
COMMENT ON TABLE notification IS 'Уведомление: код события и объект; текст формирует интерфейс';

-- ---------------------------------------------------------------------
-- Текущий статус обращения (последняя запись журнала статусов)
-- ---------------------------------------------------------------------
CREATE OR REPLACE FUNCTION request_status(p_request_id bigint) RETURNS request_status
LANGUAGE sql STABLE AS $$
    SELECT status FROM request_status_history
    WHERE request_id = p_request_id
    ORDER BY history_id DESC LIMIT 1
$$;

CREATE VIEW v_request AS
SELECT r.*, s.status, s.changed_at AS status_changed_at
FROM request r
JOIN LATERAL (SELECT h.status, h.changed_at
              FROM request_status_history h
              WHERE h.request_id = r.request_id
              ORDER BY h.history_id DESC LIMIT 1) s ON true;
COMMENT ON VIEW v_request IS 'Обращение с текущим статусом и временем его установки';

-- ---------------------------------------------------------------------
-- Справочные данные конечного автомата (часть схемы, а не тестовых данных)
-- ---------------------------------------------------------------------
INSERT INTO status_transition (from_status, to_status, actor_role) VALUES
    ('NEW',         'ACCEPTED',    'COMPANY_ADMIN'),
    ('NEW',         'CANCELLED',   'RESIDENT'),
    ('NEW',         'CANCELLED',   'COMPANY_ADMIN'),
    ('ACCEPTED',    'ASSIGNED',    'COMPANY_ADMIN'),
    ('ACCEPTED',    'CANCELLED',   'COMPANY_ADMIN'),
    ('ASSIGNED',    'ACCEPTED',    'COMPANY_ADMIN'),   -- снятие исполнителя для переназначения
    ('ASSIGNED',    'IN_PROGRESS', 'EMPLOYEE'),
    ('IN_PROGRESS', 'DONE',        'EMPLOYEE'),
    ('DONE',        'CANCELLED',   'RESIDENT'),        -- житель отклонил результат
    ('DONE',        'CLOSED',      'RESIDENT'),
    ('DONE',        'CLOSED',      'SYSTEM');          -- автозакрытие без оценки
