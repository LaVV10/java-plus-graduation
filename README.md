# Explore With Me — микросервисная архитектура (Этап 3)

Сервис поиска и участия в мероприятиях. На втором этапе диплома монолитный `main-service`
разбит на 5 микросервисов, взаимодействующих через **OpenFeign + Eureka** с отказоустойчивостью
на базе **Resilience4j** (CircuitBreaker + Retry). На третьем этапе добавлена **рекомендательная
подсистема**: три новых сервиса (Collector → Aggregator → Analyzer) поверх **Apache Kafka**
и **gRPC**, обменивающихся данными в формате **Avro**. Внешний API остался без изменений —
клиенты ходят через единый **API-шлюз** (порт 8080) и не замечают внутреннего устройства.

## Архитектура

```
                          ┌─────────────────┐
   клиент ──HTTP:8080──►  │  gateway-server │  (Spring Cloud Gateway MVC, маршруты из Config Server)
                          └────────┬────────┘
                                   │  точные Path-маршруты (lb://...)
       ┌──────────┬───────────┬────┴────┬───────────┬──────────┐
       ▼          ▼           ▼         ▼           ▼          ▼
  ┌──────────┐┌─────────┐┌────────┐┌─────────┐┌────────────┐┌──────────┐
  │  event   ││ request ││  user  ││category ││compilation ││  stats   │
  │ -service ││-service ││-service││-service ││ -service   ││ -server  │
  └────┬─────┘└────┬────┘└────┬───┘└────┬────┘└─────┬──────┘└──────────┘
       │ gRPC     │ gRPC      │         │ Feign      │
       │ (V/R/L)  │ (R)       │         │            │
       ▼          ▼           │         │            │
  ┌──────────────────┐  gRPC  │         │            │
  │    collector     │◄───────┘         │            │
  └────────┬─────────┘                  │            │
           │ Kafka (Avro): stats.user-actions.v1
           ▼
  ┌──────────────────┐         ┌──────────────────┐
  │    aggregator    │────────►│     analyzer     │  ◄── gRPC (рекомендации/рейтинг)
  │  (Kafka Streams) │  Kafka  │  (consumers+БД)  │  ──► event-service: rating + /events/recommendations
  └──────────────────┘  (Avro: └────────┬─────────┘
                         stats.events-  │
                         similarity.v1) │
                                        │
   Регистрация/поиск: Eureka (discovery-server:8761)
   Конфигурация:     Config Server (config-server:8888, native-профиль)
   Брокер сообщений:  Apache Kafka (KRaft, без Zookeeper)
```

### Действия пользователей по пути к рекомендациям

`event-service` и `request-service` отправляют действия пользователей по gRPC в `collector`,
тот пишет их в Kafka-топик `stats.user-actions.v1`. Типы действий (Этап 3-1):

| Источник | Действие | ActionTypeProto | Вес (для сходства) |
|---|---|---|---|
| `GET /events/{id}` (event-service) | просмотр | `ACTION_VIEW` | 0.4 |
| `POST /users/{userId}/requests` (request-service) | регистрация | `ACTION_REGISTER` | 0.8 |
| `PUT /events/{eventId}/like` (event-service) | лайк | `ACTION_LIKE` | 1.0 |

`aggregator` потоково пересчитывает косинусное сходство мероприятий (инкрементально, через
частные суммы S_min/S_a/S_b) и пишет в `stats.events-similarity.v1`. `analyzer` аккумулирует
оба потока в БД и отвечает по gRPC рекомендациями и рейтингом мероприятий.

### Сервисы

| Сервис | Модуль | `spring.application.name` | Назначение |
|---|---|---|---|
| event-service | `core/event-service` | `event-service` | Мероприятия + локации, рейтинг через Analyzer gRPC |
| request-service | `core/request-service` | `request-service` | Заявки на участие (шлёт ACTION_REGISTER в Collector) |
| user-service | `core/user-service` | `user-service` | Пользователи |
| category-service | `core/category-service` | `category-service` | Категории событий |
| compilation-service | `core/compilation-service` | `compilation-service` | Подборки событий |
| stats-server | `ewm-stats/stats-server` | `stats-server` | Статистика просмотров (отдельно с Этапа 1) |
| **collector** | `collector/` | `collector` | **Этап 3**: приём действий по gRPC → Kafka (Avro) |
| **aggregator** | `aggregator/` | `aggregator` | **Этап 3**: Kafka Streams, косинусное сходство мероприятий |
| **analyzer** | `analyzer/` | `analyzer` | **Этап 3**: Kafka consumers → БД + gRPC-рекомендации |
| gateway-server | `infra/gateway-server` | `gateway-server` | API-шлюз, порт 8080 |
| discovery-server | `infra/discovery-server` | `discovery-server` | Eureka, порт 8761 |
| config-server | `infra/config-server` | `config-server` | Конфигурация, порт 8888 |
| kafka | (внешний образ `confluentinc/cp-kafka`) | — | Брокер KRaft, порт 9092 |

`main-service` (`core/main-service`) сохранён как **bootstrap-оболочка**: после разделения
в нём не осталось бизнес-кода, он не поднимается в docker-compose.

> **Важно для CI**: автотесты Практикума для ветки `recommendations` запускают сервисы поиском
> JAR по имени артефакта (`category-service-*.jar`, `collector-*.jar`, `analyzer-*.jar`, ...),
> поэтому `artifactId` модулей и `spring.application.name` обязаны точно совпадать с ожидаемыми
> именами. По той же причине категории и подборки разнесены в **отдельные** сервисы.

## Базы данных

Топология БД зависит от окружения:

- **Локально (docker-compose)** — контейнеры Postgres:
  - `ewm-db` с общей базой `ewm-server` для event/user/request/category/compilation (таблицы
    разделены между сервисами по принадлежности, каждый сервис инициализирует только свои);
  - `stats-db` (база `stats`) для stats-server;
  - `analyzer-db` (база `ewm_analyzer`) для analyzer.
- **В CI Практикума** — отдельная база на сервис (`ewm_event`, `ewm_request`, `ewm_user`,
  `ewm_category`, `ewm_compilation`, `ewm_stats_db`, `ewm_analyzer`); datasource.url передаётся
  сервису аргументом `--spring.datasource.url=...` при запуске и перебивает значение из Config Server.

Схема целостности (FK) сохранена частично для данных одного сервиса; связи между сервисами
реализованы на уровне приложения через Feign/gRPC (идентификаторы хранятся как скалярные
`categoryId` / `initiatorId`, без JPA-связей к чужим сущностям).

| Сервис | Таблицы | `schema.sql` |
|---|---|---|
| event-service | `events`, `locations` | `core/event-service/src/main/resources/schema.sql` |
| user-service | `users` | `core/user-service/src/main/resources/schema.sql` |
| request-service | `requests` | `core/request-service/src/main/resources/schema.sql` |
| category-service | `categories` | `core/category-service/src/main/resources/schema.sql` |
| compilation-service | `compilations`, `compilations_events` | `core/compilation-service/src/main/resources/schema.sql` |
| stats-server | `hits` (своя БД `stats` / `ewm_stats_db`) | `ewm-stats/stats-server/src/main/resources/schema.sql` |
| analyzer | `event_similarity`, `user_action` (своя БД `ewm_analyzer`) | `analyzer/src/main/resources/schema.sql` |

Каждый сервис инициализирует свои таблицы (`spring.sql.init.mode: always`, `CREATE TABLE IF NOT EXISTS`).

## Apache Kafka (Этап 3)

Брокер поднимается в docker-compose в режиме **KRaft** (без Zookeeper) — образ
`confluentinc/cp-kafka:7.6.1`. Топики создаются one-shot контейнером `kafka-topics-init`:

| Топик | Назначение | cleanup.policy |
|---|---|---|
| `stats.user-actions.v1` | действия пользователей (UserActionAvro): collector → aggregator/analyzer | compact,delete |
| `stats.events-similarity.v1` | сходство мероприятий (EventSimilarityAvro): aggregator → analyzer | compact,delete |

Сериализация Avro — без Schema Registry (кастомные `AvroSerializer`/`AvroDeserializer` поверх
`SpecificRecord` из `avro-1.11.3`). Все Avro-схемы в модуле `stats-contract/src/main/avro/`,
генерация классов — `avro-maven-plugin`. UI Kafka (опционально): http://localhost:8090.

## Взаимодействие сервисов (внутреннее Feign-API)

Внутренние эндпоинты вынесены под префикс `/internal/**` и **не маршрутизируются** gateway-ом
наружу. Межсервисные вызовы идут через OpenFeign + Eureka (`lb://<service-name>`), защищены
Resilience4j (CircuitBreaker + Retry) с fallback-классами.

| Откуда → Куда | Транспорт | Эндпоинт | Назначение | Fallback |
|---|---|---|---|---|
| event → category | Feign `CategoryClient` | `GET /internal/categories/{id}`, `GET /internal/categories?ids=` | Категория для EventFullDto | null / пустой список |
| event → user | Feign `UserClient` | `GET /internal/users/{id}`, `GET /internal/users?ids=` | Инициатор события | null / пустой список |
| event → request | Feign `RequestStatsClient` | `GET /internal/requests/count?eventId=`, `GET /internal/requests/count-batch?eventIds=` | Подсчёт подтверждённых заявок | `0` / пустая map |
| event → collector | **gRPC** `CollectorClient` | `CollectUserAction` (ACTION_VIEW при `GET /events/{id}`, ACTION_LIKE при `PUT /events/{eventId}/like`) | Фиксация просмотра/лайка для рекомендаций | лог warn, не валит показ |
| event → analyzer | **gRPC** `AnalyzerClient` | `GetInteractionsCount` (rating), `GetRecommendationsForUser` (`/events/recommendations`), `HasInteraction` (валидация лайка) | Рейтинг и рекомендации | пустой список / false |
| request → collector | **gRPC** `CollectorClient` | `CollectUserAction` (ACTION_REGISTER при `POST /users/{userId}/requests`) | Фиксация регистрации для рекомендаций | лог warn, не валит заявку |
| request → event | Feign `EventClient` | `GET /internal/events/{id}/info` | Снимок события для модерации заявки | null → 404 |
| request → user | Feign `UserClient` | `GET /internal/users/{id}` | Валидация существования пользователя | null → 404 |
| category → event | Feign `EventClient` | `GET /internal/events/exists-by-category?categoryId=` | Проверка категории при удалении | false |
| compilation → event | Feign `EventClient` | `GET /internal/events?ids=` | События подборки | пустой список |

Цикл event ↔ request разорван двунаправленным Feign без рекурсии: подсчёт заявок (event→request)
не вызывает обратного создания заявок (request→event), поэтому синхронный RPC безопасен.

### Рекомендательный конвейер (Этап 3)

| Этап | Сервис | Транспорт | Топик/метод | Содержимое |
|---|---|---|---|---|
| 1. Сбор действий | collector | gRPC → Kafka | `stats.user-actions.v1` (Avro `UserActionAvro`) | userId, eventId, actionType, timestamp |
| 2. Сходство | aggregator | Kafka Streams: in → out | in: `stats.user-actions.v1`, out: `stats.events-similarity.v1` (Avro `EventSimilarityAvro`) | eventA, eventB, score, timestamp |
| 3. Рекомендации | analyzer | Kafka → БД + gRPC | in: оба топика в таблицы `event_similarity` / `user_action`; out: `RecommendationsController` | рекомендации по 3 алгоритмам |

Сходство считается инкрементально через частные суммы (Этап 3-1):
`similarity(A,B) = S_min(A,B) / (S_A · S_B)`, где `S_min` — сумма минимальных весов общих
пользователей, `S_A`/`S_B` — суммы весов всех пользователей по мероприятиям A и B.
Веса действий: VIEW=0.4, REGISTER=0.8, LIKE=1.0; для пары (user, event) берётся **максимум**.

## Маршруты API-шлюза

Маршруты заданы в `infra/config-server/src/main/resources/config_repo/gateway-server.yml`.
Используется по одному Path-предикату на маршрут (атомарные маршруты надёжнее, чем несколько
шаблонов через запятую в одном предикате). Порядок важен: специфичные пути (`/events/recommendations`,
`/events/{eventId}/like`, requests) идут раньше общих (`/events/**`).

| Метод | Путь | Целевой сервис |
|---|---|---|
| POST/GET/DELETE | `/admin/users`, `/admin/users/**` | user-service |
| POST/PATCH/DELETE | `/admin/categories`, `/admin/categories/**` | category-service |
| GET | `/categories`, `/categories/**` | category-service |
| POST/PATCH/DELETE | `/admin/compilations`, `/admin/compilations/**` | compilation-service |
| GET | `/compilations`, `/compilations/**` | compilation-service |
| POST/GET/PATCH | `/users/{userId}/events`, `/users/{userId}/events/**` | event-service |
| GET/PATCH | `/admin/events`, `/admin/events/**` | event-service |
| GET | `/events/recommendations` (Этап 3) | event-service |
| PUT | `/events/{eventId}/like` (Этап 3) | event-service |
| GET | `/events`, `/events/**` | event-service |
| POST/GET/PATCH/DELETE | `/admin/locations`, `/admin/locations/**` | event-service |
| POST/GET/PATCH | `/users/{userId}/events/{eventId}/requests` | request-service |
| POST/GET/PATCH | `/users/{userId}/requests` | request-service |

## Где лежат конфигурации

- **Централизованные** (datasource, JPA, resilience4j, маршруты gateway, Kafka, gRPC): `infra/config-server/src/main/resources/config_repo/`
  - `event-service.yml`, `request-service.yml`, `user-service.yml`, `category-service.yml`, `compilation-service.yml`, `gateway-server.yml`, `stats-server.yml`, `collector.yml`, `aggregator.yml`, `analyzer.yml`
- **Bootstrap-минимум** каждого сервиса (имя приложения, import configserver, Eureka): `<модуль>/src/main/resources/application.properties`
- **docker-compose**: `docker-compose.yml` в корне (включая Kafka, analyzer-db, kafka-topics-init)

## Запуск

```bash
# 1. Собрать все модули (checkstyle + spotbugs включены профилем check)
./mvnw -P check clean install -DskipTests

# 2. Поднять инфраструктуру и микросервисы (включая Kafka и collector/aggregator/analyzer)
docker compose up -d --build

# 3. Дождаться регистрации в Eureka (~40-60 сек) и прогреть сервисы.
#    Дашборд Eureka: http://localhost:8761  — должно быть зарегистрировано 11 сервисов
#    (event, request, user, category, compilation, stats-server, gateway,
#     collector, aggregator, analyzer + config-server).
#    UI Kafka (опционально): http://localhost:8090

# 4. Прогнать Postman-тесты (gateway на хост-порту 8081 -> контейнер 8080)
newman run postman/feature.json --env-var "baseUrl=http://localhost:8081"
```

> Порт 8081 на хосте проброшен на 8080 в контейнере gateway, т.к. 8080 на хосте занят
> сторонним процессом. Внутри docker-сети gateway слушает именно 8080 (по ТЗ).

### Ручные проверки рекомендаций (Этап 3)

```bash
# 1. Создать пользователя → категорию → событие → опубликовать → зарегистрироваться на него
#    (все запросы через gateway на :8081). После этого в Kafka появятся действия.

# 2. Получить персональные рекомендации (X-EWM-USER-ID обязателен):
curl -H "X-EWM-USER-ID: 1" "http://localhost:8081/events/recommendations?maxResults=10"

# 3. Поставить лайк посещённому мероприятию:
curl -X PUT -H "X-EWM-USER-ID: 1" "http://localhost:8081/events/1/like"
# (лайк не посещённого события → 400 BAD_REQUEST)

# 4. Поле rating в выдаче событий (например, GET /events/1 с X-EWM-USER-ID) — сумма
#    максимальных весов действий из Analyzer; 0.0 при недоступности Analyzer.
```

## Внешний API (спецификации)

- Основной сервис: [`ewm-main-service-spec.json`](ewm-main-service-spec.json) (OpenAPI 3.0.1)
- Сервис статистики: [`ewm-stats-service-spec.json`](ewm-stats-service-spec.json) (OpenAPI 3.0.1)

## Надёжность (Resilience4j)

Параметры CircuitBreaker/Retry/TimeLimiter вынесены в конфиги сервисов (`config_repo/*-service.yml`):
- CircuitBreaker: sliding-window 10, failure-rate 50%, open 10s
- Retry: 3 попытки, интервал 500 мс (повтор на 5xx / timeout / `RetryableException`)
- TimeLimiter: 3 сек

Fallback-политика (по ТЗ «вернуть фиксированное значение»):
- `confirmedRequests` → `0` при недоступности request-service
- `initiator` / `category` → `null` (поле отсутствует в DTO) при недоступности user/category-service
- `getEventInfo` / `getUserById` в request-service → `null` → 404 (критичная зависимость, нельзя
  создать заявку на неизвестное событие/пользователя)
- `existsByCategoryId` (category → event) → `false` (даёт удалить неиспользуемую категорию
  при недоступности event-service)
- `getEventsByIds` (compilation → event) → пустой список (подборка вернётся без раскрытия событий)

**Рекомендательный конвейер (Этап 3) — fire-and-forget**: gRPC-вызовы в Collector
(отправка действий) и Analyzer (рейтинг/рекомендации) обёрнуты в `*-Safe`-методы с перехватом
ошибок:
- недоступность Collector не ломает показ события или создание заявки (действие просто не зафиксируется);
- недоступность Analyzer: рейтинг события → `0.0`, рекомендации → пустой список, валидация лайка → `false` → `400 BAD_REQUEST`.

## Структура модулей

```
diplom/
├── core/
│   ├── ewm-common/         # общие кросс-сервисные DTO/enums (CategoryDto, UserShortDto, EventFullDto, ...)
│   ├── event-service/      # events + locations (gRPC → collector/analyzer)
│   ├── request-service/    # requests (gRPC → collector)
│   ├── user-service/       # users
│   ├── category-service/   # categories
│   ├── compilation-service/# compilations
│   └── main-service/       # bootstrap-оболочка (бизнес-кода нет)
├── ewm-stats/              # сервис статистики (stats-dto, stats-client (+ gRPC-клиенты), stats-server)
├── stats-contract/         # ОБЩИЙ контракт: .proto (gRPC-стабы) + .avsc (Avro-классы)
├── collector/              # Этап 3: gRPC → Kafka (Avro UserActionAvro)
├── aggregator/             # Этап 3: Kafka Streams, косинусное сходство мероприятий
├── analyzer/               # Этап 3: Kafka consumers → БД + gRPC RecommendationsController
├── infra/
│   ├── discovery-server/   # Eureka
│   ├── config-server/      # Config Server (native, config_repo)
│   └── gateway-server/     # Spring Cloud Gateway MVC
├── postman/feature.json    # Postman-коллекция фичи (locations + moderation)
├── docker-compose.yml      # включая Kafka, analyzer-db, kafka-topics-init
└── pom.xml
```
