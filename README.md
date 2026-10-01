# TicTac

REST API для игры в крестики-нолики с JWT-авторизацией. Можно играть против компьютера (бот на алгоритме minimax) или против другого игрока.

## Возможности

- Регистрация и вход по логину и паролю (пароли хранятся в виде BCrypt-хеша)
- JWT: короткоживущий access-токен и одноразовый refresh-токен
- Игра против компьютера: бот ходит сразу после хода игрока
- Игра против человека: создать игру, дождаться соперника, ходить по очереди
- Сервер проверяет ходы: чья очередь, ровно одна новая клетка, правильная метка, поле 3×3
- История завершённых игр пользователя
- Таблица лидеров по соотношению побед

## Стек

- Java 25+
- Spring Boot 4.1 (Web, Security, Data JPA)
- JJWT 0.12
- PostgreSQL
- Gradle 9 (wrapper в репозитории)
- JUnit 5 + Mockito

## Архитектура

Проект разделён на три слоя, у каждого слоя свои модели. Между слоями модели переводятся мапперами.

```
web/          контроллеры, DTO (WebGame, WebUser), AuthFilter, обработка ошибок
  ↓
domain/       бизнес-логика: GameServiceImpl, AuthService, UserService, JwtProvider
  ↓
datasource/   JPA-сущности (DataGame, DataUser) и репозитории
di/           конфигурация бинов и Spring Security
```

## Запуск

1. Поднять PostgreSQL, например в Docker:

   ```bash
   docker run -d --name tictac-db -p 5432:5432 -e POSTGRES_PASSWORD=root postgres:17
   ```

2. При необходимости поправить `src/main/resources/application.properties`: подключение к БД и JWT-секреты. Каждый секрет должен быть не короче 32 байт. Таблицы создаются автоматически (`ddl-auto=update`).

3. Запустить приложение:

   ```bash
   ./gradlew bootRun
   ```

   Сервер будет доступен на `http://localhost:8080`.

## Авторизация

1. `POST /auth/login` возвращает пару токенов:
   ```json
   { "type": "Bearer", "accessToken": "...", "refreshToken": "..." }
   ```
2. Access-токен передаётся в каждом запросе в заголовке `Authorization: Bearer <accessToken>`. Он живёт 15 минут, внутри хранятся UUID пользователя и его роли.
3. Когда access-токен истёк, `POST /auth/token` с refresh-токеном выдаёт новый access-токен.
4. `POST /auth/refresh` выдаёт новую пару токенов, а старый refresh-токен после этого перестаёт работать. Refresh-токен живёт 30 дней.

Если токена нет или он неверный, защищённые эндпоинты отвечают `401`.

## API

| Метод | Путь | Авторизация | Описание |
|-------|------|:-----------:|----------|
| POST | `/auth/sign_up` | — | Регистрация, тело `{login, password}` |
| POST | `/auth/login` | — | Вход, тело `{login, password}`, ответ — пара токенов |
| POST | `/auth/token` | — | Новый access-токен, тело `{refreshToken}` |
| POST | `/auth/refresh` | ✔ | Новая пара токенов, тело `{refreshToken}` |
| GET | `/user/me` | ✔ | Текущий пользователь |
| GET | `/user/{uuid}` | ✔ | Пользователь по UUID |
| POST | `/game` | ✔ | Создать игру, тело `{vsComputer: true/false}` |
| GET | `/game/available` | ✔ | Игры, которые ждут второго игрока |
| POST | `/game/{uuid}/join` | ✔ | Присоединиться к игре |
| GET | `/game/{uuid}` | ✔ | Состояние игры |
| POST | `/game/{uuid}` | ✔ | Сделать ход: передаётся поле с одной новой меткой |
| GET | `/game/history` | ✔ | Завершённые игры текущего пользователя (победы и ничьи), новые сверху |
| GET | `/leaderboard?n=10` | ✔ | Топ-N игроков: `uuid`, `login`, `winRatio` |

Поле игры — матрица 3×3, где `1` означает X (первый игрок), `-1` — O (второй игрок или компьютер), `0` — пустую клетку. Статусы игры: `WAITING_FOR_PLAYERS`, `PLAYER_TURN`, `WIN`, `DRAW`. У каждой игры есть дата создания `createdAt`. Если победил компьютер, `winnerUuid` равен `00000000-0000-0000-0000-000000000000`.

`winRatio` — это победы, делённые на сумму поражений и ничьих. Если поражений и ничьих нет, делитель считается равным 1.

Ошибки возвращаются в виде `{ "message": "..." }`. Неверный ход даёт `400`, неверные логин, пароль или токен — `401`.

## Пример

```bash
# регистрация и вход
curl -X POST localhost:8080/auth/sign_up -H 'Content-Type: application/json' \
     -d '{"login":"alice","password":"secret"}'

TOKEN=$(curl -s -X POST localhost:8080/auth/login -H 'Content-Type: application/json' \
     -d '{"login":"alice","password":"secret"}' | jq -r .accessToken)

# новая игра против компьютера
GAME=$(curl -s -X POST localhost:8080/game -H "Authorization: Bearer $TOKEN" \
     -H 'Content-Type: application/json' -d '{"vsComputer":true}' | jq -r .uuid)

# ход в центр, в ответе будет уже и ход бота
curl -X POST localhost:8080/game/$GAME -H "Authorization: Bearer $TOKEN" \
     -H 'Content-Type: application/json' \
     -d '{"gameField":{"matrix":[[0,0,0],[0,1,0],[0,0,0]]}}'
```

## Тесты

```bash
./gradlew test
```

## Ограничения

- Refresh-токены хранятся в памяти приложения, по одному на пользователя. После перезапуска сервера нужно заново войти, а вход с другого устройства делает недействительным предыдущий refresh-токен.
- Секреты в `application.properties` указаны только для локальной разработки.
