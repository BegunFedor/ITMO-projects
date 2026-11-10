# ИС обработки обращений жителей МКД

Структура:
- `sql/` — скрипты БД (install.sql, 99_drop.sql, тесты)
- `backend/` — Spring Boot 3 (Java 17), уровни хранения и бизнес-логики, REST API
- `frontend/` — React 18 + TypeScript (Vite, MUI)

## Сборка
```
cd frontend && npm ci && npm run build      # кладёт файлы в backend/src/main/resources/static
cd ../backend && mvn package                # target/mkd-requests.jar
```

## Запуск на helios
```
scp -P 2222 -r backend/target/mkd-requests.jar sql s<ИСУ>@helios.cs.ifmo.ru:~/mkd/
ssh -p 2222 s<ИСУ>@helios.cs.ifmo.ru
cd ~/mkd/sql && psql -h pg -d studs -v ON_ERROR_STOP=1 -f install.sql
cd ~/mkd && DB_URL=jdbc:postgresql://pg:5432/studs DB_USER=s<ИСУ> DB_PASSWORD=<пароль> PORT=<порт> \
  nohup java -jar mkd-requests.jar > app.log 2>&1 &
```
На своём компьютере: `ssh -p 2222 -L 8080:localhost:<порт> s<ИСУ>@helios.cs.ifmo.ru`, затем http://localhost:8080

Тестовые логины (пароль `password`): ivanov (житель), admin_sev (администратор УК),
plumb_sev (сотрудник), insp_center (инспектор ГЖИ).
