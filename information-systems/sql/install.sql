-- Полная установка: создание схемы, триггеры, функции, индексы, тестовые данные
-- psql -h pg -d studs -v ON_ERROR_STOP=1 -f install.sql
\set ON_ERROR_STOP on
\i 99_drop.sql
\i 01_create.sql
\i 02_triggers.sql
\i 03_functions.sql
\i 04_indexes.sql
\i 05_fill.sql
