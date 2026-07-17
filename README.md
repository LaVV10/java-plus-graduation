# Explore With Me — микросервисная архитектура (Этап 2)

Сервис поиска и участия в мероприятиях. На втором этапе диплома монолитный `main-service`
разбит на 4 микросервиса, взаимодействующих через **OpenFeign + Eureka** с отказоустойчивостью
на базе **Resilience4j** (CircuitBreaker + Retry). Внешний API остался без изменений —
клиенты ходят через единый **API-шлюз** (порт 8080) и не замечают внутреннего устройства.

## Архитектура

```
                          ┌─────────────────┐
   клиент ──HTTP:8080──►  │  gateway-server │  (Spring Cloud Gateway MVC, маршруты из Config Server)
                          └────────┬────────┘
                                   │  точные Path-маршруты (lb://...)
            ┌──────────┬───────────┼───────────┬──────────┐
            ▼          ▼           ▼           ▼          ▼
      ┌──────────┐┌─────────┐┌──────────┐┌──────────┐┌────────────┐
      │  event   ││ request ││   user   ││ feature  ││   stats    │
      │ -service ││-service ││ -service ││ -service ││  -server   │
      └────┬─────┘└────┬────┘└────┬─────┘└────┬─────┘└────────────┘
           │ Feign     │ Feign    │           │ Feign
           └───────────┴──────────┘           │
                  ▲                           │
                  └───────────────────────────┘
   Регистрация/поиск: Eureka (discovery-server:8761)
   Конфигурация:     Config Server (config-server:8888, native-профиль)
```

### Сервисы

| Сервис | Модуль | `spring.application.name` | Назначение |
|---|---|---|---|
| event-service | `core/event-service` | `event-service` | Мероприятия + локации, подсчёт просмотров через stats |
| request-service | `core/request-service` | `request-service` | Заявки на участие |
| user-service | `core/user-service` | `user-service` | Пользователи |
| feature-service | `core/feature-service` | `feature-service` | Категории + подборки (доп. функциональность) |
| stats-server | `ewm-stats/stats-server` | `stats-server` | Статистика просмотров (отдельно с Этапа 1) |
| gateway-server | `infra/gateway-server` | `gateway-server` | API-шлюз, порт 8080 |
| discovery-server | `infra/discovery-server` | `discovery-server` | Eureka, порт 8761 |
| config-server | `infra/config-server` | `config-server` | Конфигурация, порт 8888 |

`main-service` (`core/main-service`) сохранён как **bootstrap-оболочка**: после разделения
в нём не осталось бизнес-кода, он не поднимается в docker-compose.

## Общая база данных

Используется один контейнер Postgres `ewm-db` (база `ewm-server`), таблицы разделены между
сервисами по принадлежности. Схема целостности (FK) сохранена частично для данных одного сервиса;
связи между сервисами реализованы на уровне приложения через Feign (идентификаторы хранятся
как скалярные `categoryId` / `initiatorId`, без JPA-связей к чужим сущностям).

| Сервис | Таблицы | `schema.sql` |
|---|---|---|
| event-service | `events`, `locations` | `core/event-service/src/main/resources/schema.sql` |
| user-service | `users` | `core/user-service/src/main/resources/schema.sql` |
| request-service | `requests` | `core/request-service/src/main/resources/schema.sql` |
| feature-service | `categories`, `compilations`, `compilations_events` | `core/feature-service/src/main/resources/schema.sql` |
| stats-server | `hits` (своя БД `stats`) | `ewm-stats/stats-server/src/main/resources/schema.sql` |

Каждый сервис инициализирует свои таблицы (`spring.sql.init.mode: always`, `CREATE TABLE IF NOT EXISTS`).

## Взаимодействие сервисов (внутреннее Feign-API)

Внутренние эндпоинты вынесены под префикс `/internal/**` и **не маршрутизируются** gateway-ом
наружу. Межсервисные вызовы идут через OpenFeign + Eureka (`lb://<service-name>`), защищены
Resilience4j (CircuitBreaker + Retry) с fallback-классами.

| Откуда → Куда | Feign-клиент | Эндпоинт | Назначение | Fallback |
|---|---|---|---|---|
| event → feature | `CategoryClient` | `GET /internal/categories/{id}`, `GET /internal/categories?ids=` | Категория для EventFullDto | null / пустой список |
| event → user | `UserClient` | `GET /internal/users/{id}`, `GET /internal/users?ids=` | Инициатор события | null / пустой список |
| event → request | `RequestStatsClient` | `GET /internal/requests/count?eventId=`, `GET /internal/requests/count-batch?eventIds=` | Подсчёт подтверждённых заявок | `0` / пустая map |
| event → stats | `StatsClient` (RestTemplate) | `POST /hit`, `GET /stats` | Просмотры | — |
| request → event | `EventClient` | `GET /internal/events/{id}/info` | Снимок события для модерации заявки | null → 404 |
| request → user | `UserClient` | `GET /internal/users/{id}` | Валидация существования пользователя | null → 404 |
| feature → event | `EventClient` | `GET /internal/events/exists-by-category?categoryId=`, `GET /internal/events?ids=` | Проверка категории при удалении; события подборки | false / пустой список |

Цикл event ↔ request разорван двунаправленным Feign без рекурсии: подсчёт заявок (event→request)
не вызывает обратного создания заявок (request→event), поэтому синхронный RPC безопасен.

## Маршруты API-шлюза

Маршруты заданы в `infra/config-server/src/main/resources/config_repo/gateway-server.yml`.
Используется по одному Path-предикату на маршрут (атомарные маршруты надёжнее, чем несколько
шаблонов через запятую в одном предикате). Порядок важен: специфичные пути (requests) раньше общих.

| Метод | Путь | Целевой сервис |
|---|---|---|
| POST/GET/DELETE | `/admin/users`, `/admin/users/**` | user-service |
| POST/PATCH/DELETE | `/admin/categories`, `/admin/categories/**` | feature-service |
| GET | `/categories`, `/categories/**` | feature-service |
| POST/PATCH/DELETE | `/admin/compilations`, `/admin/compilations/**` | feature-service |
| GET | `/compilations`, `/compilations/**` | feature-service |
| POST/GET/PATCH | `/users/{userId}/events`, `/users/{userId}/events/**` | event-service |
| GET/PATCH | `/admin/events`, `/admin/events/**` | event-service |
| GET | `/events`, `/events/**` | event-service |
| POST/GET/PATCH/DELETE | `/admin/locations`, `/admin/locations/**` | event-service |
| POST/GET/PATCH | `/users/{userId}/events/{eventId}/requests` | request-service |
| POST/GET/PATCH | `/users/{userId}/requests` | request-service |

## Где лежат конфигурации

- **Централизованные** (datasource, JPA, resilience4j, маршруты gateway): `infra/config-server/src/main/resources/config_repo/`
  - `event-service.yml`, `request-service.yml`, `user-service.yml`, `feature-service.yml`, `gateway-server.yml`, `stats-server.yml`
- **Bootstrap-минимум** каждого сервиса (имя приложения, import configserver, Eureka): `<модуль>/src/main/resources/application.properties`
- **docker-compose**: `docker-compose.yml` в корне

## Запуск

```bash
# 1. Собрать все модули (checkstyle + spotbugs включены профилем check)
./mvnw -P check clean install -DskipTests

# 2. Поднять инфраструктуру и микросервисы
docker compose up -d --build

# 3. Дождаться регистрации в Eureka (~30-40 сек) и прогреть сервисы
#    Дашборд Eureka: http://localhost:8761

# 4. Прогнать Postman-тесты (gateway на хост-порту 8081 -> контейнер 8080)
newman run postman/feature.json --env-var "baseUrl=http://localhost:8081"
```

> Порт 8081 на хосте проброшен на 8080 в контейнере gateway, т.к. 8080 на хосте занят
> сторонним процессом. Внутри docker-сети gateway слушает именно 8080 (по ТЗ).

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
- `initiator` / `category` → `null` (поле отсутствует в DTO) при недоступности user/feature-service
- `getEventInfo` / `getUserById` в request-service → `null` → 404 (критичная зависимость, нельзя
  создать заявку на неизвестное событие/пользователя)
- `existsByCategoryId` → `false` (даёт удалить неиспользуемую категорию при недоступности event-service)

Проверено: при остановке request + user + feature сервисов (остался только event-service) публичный
поиск событий `/events` продолжает возвращать `200` с дефолтными значениями вместо обогащённых полей.

## Структура модулей

```
diplom/
├── core/
│   ├── ewm-common/         # общие кросс-сервисные DTO/enums (CategoryDto, UserShortDto, EventFullDto, ...)
│   ├── event-service/      # events + locations
│   ├── request-service/    # requests
│   ├── user-service/       # users
│   ├── feature-service/    # categories + compilations
│   └── main-service/       # bootstrap-оболочка (бизнес-кода нет)
├── ewm-stats/              # сервис статистики (stats-dto, stats-client, stats-server)
├── infra/
│   ├── discovery-server/   # Eureka
│   ├── config-server/      # Config Server (native, config_repo)
│   └── gateway-server/     # Spring Cloud Gateway MVC
├── postman/feature.json    # Postman-коллекция фичи (locations + moderation)
├── docker-compose.yml
└── pom.xml
```
