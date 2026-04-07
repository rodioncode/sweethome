# Family Todo — Руководство по разработке клиентского приложения

## Общие сведения

**Бэкенд:** Go microservices за API Gateway
**Клиент:** Kotlin Multiplatform (Android + iOS)
**Базовый URL:** `http://<gateway>:8080`
**Аутентификация:** JWT (HMAC-SHA256), передаётся в `Authorization: Bearer <accessToken>`
**Формат ответов:** JSON envelope `{ "data": ..., "error": ... }`

---

## 1. Формат ответов (Response Envelope)

Все эндпоинты возвращают единый формат:

```json
{
  "data": <объект | массив | null>,
  "error": <null | { "code": "string", "message": "string", "details": null }>
}
```

**Правило:** если `error != null` — запрос не удался, `data` будет `null`. Если `error == null` — данные в `data`.

### Стандартные коды ошибок

| Код | HTTP | Описание |
|-----|------|----------|
| `bad_request` | 400 | Невалидный JSON |
| `validation_error` | 400 | Не заполнены обязательные поля или некорректный формат |
| `unauthorized` | 401 | Нет или невалидный JWT-токен |
| `invalid_credentials` | 401 | Неверный email/пароль |
| `invalid_token` | 401 | Refresh-токен невалиден, отозван или истёк |
| `forbidden` | 403 | Нет доступа к ресурсу |
| `email_required` | 403 | Гостевой аккаунт — нужно привязать email |
| `owner_cannot_leave` | 403 | Владелец должен передать роль перед выходом |
| `not_found` | 404 | Ресурс не найден |
| `rate_limit` | 429 | Слишком много запросов |
| `internal_error` | 500 | Внутренняя ошибка сервера |

---

## 2. Аутентификация и управление токенами

### Жизненный цикл токенов

- **Access token:** 15 минут, JWT с `sub` = userId
- **Refresh token:** 30 дней, непрозрачная строка, ротируется при каждом обновлении
- Refresh token одноразовый — после использования старый отзывается, выдаётся новый

### Рекомендуемая логика на клиенте

1. Хранить `accessToken`, `refreshToken`, `userId` в защищённом хранилище (Keychain / EncryptedSharedPrefs)
2. При получении 401 — вызвать `POST /auth/refresh`
3. Если refresh тоже 401 — разлогинить пользователя
4. Добавлять `Authorization: Bearer <accessToken>` ко всем запросам кроме auth-эндпоинтов

---

## 3. API-контракты

### 3.1. Регистрация и вход

#### POST /auth/register
Регистрация по email. Rate limit: 10 req/min per IP.

```
← Request:
{
  "email": "user@example.com",    // обязательно, приводится к lowercase
  "password": "secret123",         // обязательно
  "displayName": "Иван"            // обязательно
}

→ Response 200:
{
  "data": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "accessToken": "eyJhbGciOi...",
    "refreshToken": "dGhpcyBpcyBh..."
  }
}
```

#### POST /auth/login
Вход по email/паролю. Rate limit: 10 req/min per IP.

```
← Request:
{
  "email": "user@example.com",
  "password": "secret123"
}

→ Response 200:  // аналогичен register
```

**Ошибки:** `invalid_credentials` (401) при неверном email/пароле.

#### POST /auth/guest
Гостевой вход без регистрации. Rate limit: 10 req/min per IP.

```
← Request: {} (пустое тело)

→ Response 200:  // аналогичен register, displayName = "Guest"
```

**Важно:** гостевой аккаунт не может вступать в группы. Показывать пользователю подсказку о привязке email.

#### POST /auth/refresh
Обновление токенов. Ротация: старый refresh отзывается.

```
← Request:
{
  "refreshToken": "dGhpcyBpcyBh..."
}

→ Response 200:
{
  "data": {
    "userId": "...",
    "accessToken": "новый...",
    "refreshToken": "новый..."   // ВАЖНО: сохранить новый!
  }
}
```

**Ошибки:** `invalid_token` (401) — нужно разлогинить пользователя.

#### POST /auth/logout
Отзыв refresh-токена.

```
← Request:
{
  "refreshToken": "dGhpcyBpcyBh..."
}

→ Response 200: { "data": {}, "error": null }
```

#### POST /auth/link/email 🔒
Привязка email к гостевому аккаунту. Требует JWT.

```
← Request:
{
  "email": "user@example.com",
  "password": "secret123",
  "displayName": "Иван"
}

→ Response 200: { "data": {}, "error": null }
```

**UX:** после успешной привязки обновить отображение пользователя (displayName), разблокировать функции групп.

---

### 3.2. Устройства и уведомления

#### POST /auth/devices 🔒
Регистрация push-токена устройства.

```
← Request:
{
  "platform": "android",        // "android" | "ios"
  "pushToken": "firebase_token..."
}

→ Response 200: { "data": {}, "error": null }
```

**Когда вызывать:** при запуске приложения и при обновлении push-токена.

#### GET /auth/notification-preferences 🔒
Получить настройки каналов уведомлений.

```
→ Response 200:
{
  "data": {
    "preferences": [
      { "channel": "groups", "enabled": true },
      { "channel": "tasks", "enabled": true },
      { "channel": "assignments", "enabled": false },
      { "channel": "reminders", "enabled": true }
    ]
  }
}
```

**Примечание:** если пользователь ещё не настраивал каналы — массив будет пустым. По умолчанию все каналы считаются включёнными.

**Каналы уведомлений:**

| Канал | Триггеры |
|-------|----------|
| `groups` | Приглашение в группу, принятие, выход участника |
| `tasks` | Задача выполнена/изменена в групповом списке |
| `assignments` | Задача назначена на пользователя |
| `reminders` | Дедлайны (general_todos), расписание (home_chores) |

#### PUT /auth/notification-preferences 🔒
Обновить настройку канала.

```
← Request:
{
  "channel": "assignments",     // обязательно
  "enabled": false
}

→ Response 200: { "data": {}, "error": null }
```

---

### 3.3. Группы

#### GET /groups 🔒
Список групп пользователя с его ролью.

```
→ Response 200:
{
  "data": {
    "groups": [
      {
        "id": "uuid",
        "name": "Семья",
        "createdBy": "uuid",
        "createdAt": "2026-04-01T12:00:00Z",
        "role": "owner"            // "owner" | "member"
      }
    ]
  }
}
```

#### POST /groups 🔒
Создать группу. Создатель автоматически получает роль `owner`.

```
← Request:
{
  "name": "Семья"               // обязательно
}

→ Response 200:
{
  "data": {
    "id": "uuid",
    "name": "Семья",
    "createdBy": "uuid",
    "createdAt": "2026-04-01T12:00:00Z",
    "role": "owner"
  }
}
```

#### POST /groups/{id}/invites 🔒
Создать приглашение. Любой участник группы может создать инвайт (срок — 7 дней).

```
→ Response 200:
{
  "data": {
    "token": "abc123def456...",
    "expiresAt": "2026-04-08T12:00:00Z"
  }
}
```

**Клиентская логика:**
- Сформировать deep link: `familytodo://invite/{token}`
- Показать кнопку «Поделиться ссылкой»
- При перехвате deep link → вызвать `POST /invites/{token}/accept`

#### POST /invites/{token}/accept 🔒
Принять приглашение. Гостевые аккаунты (без email) — получат `email_required` (403).

```
→ Response 200:
{
  "data": {
    "groupId": "uuid"
  }
}
```

**Ошибки:**
- `email_required` (403) — показать экран привязки email
- `invalid_invite` (400) — «Приглашение недействительно или истекло»

#### POST /groups/{id}/transfer-ownership 🔒
Передать роль владельца. Только текущий owner.

```
← Request:
{
  "userId": "uuid"              // UUID нового владельца
}

→ Response 200: { "data": {}, "error": null }
```

**UX:** показывать список участников группы для выбора нового владельца.

#### DELETE /groups/{id} 🔒
Удалить группу. Только owner. Каскадно удаляются все списки и элементы.

```
→ Response 200: { "data": {}, "error": null }
```

**UX:** обязательно подтверждение «Все списки группы будут удалены».

#### DELETE /groups/{id}/members/{userId} 🔒
Удалить участника или выйти из группы.

**Сценарии:**
- `userId == текущий пользователь` → выход из группы (self-leave)
- `userId != текущий пользователь` → удаление участника (только owner)
- Owner не может выйти без передачи роли → `owner_cannot_leave`

```
→ Response 200: { "data": {}, "error": null }
```

**Побочный эффект:** все задачи, назначенные на удалённого участника, становятся «свободными» (`assignedTo = null`).

**Права:**

| Действие | Owner | Member |
|----------|-------|--------|
| Выход из группы | ❌ (сначала передать роль) | ✅ |
| Удаление другого участника | ✅ | ❌ |
| Передача владения | ✅ | ❌ |
| Удаление группы | ✅ | ❌ |
| Создание инвайтов | ✅ | ✅ |
| CRUD списков/задач | ✅ | ✅ |

---

### 3.4. Списки

#### GET /lists 🔒
Получить списки.

**Query-параметры:**
- `scope` — `personal` (по умолчанию) или `group`
- `groupId` — UUID группы (обязателен при `scope=group`)

```
→ Response 200:
{
  "data": {
    "lists": [
      {
        "id": "uuid",
        "type": "shopping",          // "shopping" | "general_todos" | "home_chores"
        "title": "Продукты",
        "icon": "🛒",                // null если не задана
        "scope": "personal",         // "personal" | "group"
        "ownerUserId": "uuid",       // null для scope=group
        "ownerGroupId": null,        // uuid для scope=group
        "createdBy": "uuid",
        "createdAt": "2026-04-01T12:00:00Z",
        "archivedAt": null            // null если активен
      }
    ]
  }
}
```

**Логика на клиенте:**
- Показать отдельные вкладки/секции: «Мои списки» + по каждой группе
- Фильтровать `archivedAt != null` для архивных (или показать с пометкой)

#### POST /lists 🔒
Создать список.

```
← Request:
{
  "type": "shopping",             // обязательно
  "title": "Продукты на неделю",  // обязательно
  "icon": "🛒",                   // опционально
  "scope": "personal",            // обязательно: "personal" | "group"
  "groupId": null                 // обязательно при scope=group
}
```

#### GET /lists/{id} 🔒
Получить список с элементами.

```
→ Response 200:
{
  "data": {
    "list": { /* listDTO */ },
    "items": [
      {
        "id": "uuid",
        "listId": "uuid",
        "title": "Молоко",
        "note": "2.5%, Простоквашино",
        "sortOrder": 1.0,
        "isDone": false,
        "doneAt": null,
        "createdBy": "uuid",
        "createdAt": "2026-04-01T12:00:00Z",
        "updatedAt": "2026-04-01T12:00:00Z",
        "assignedTo": "uuid",        // null = никому не назначено
        "dueAt": "2026-04-05T18:00:00Z",  // null = без дедлайна
        "isFavorite": false,
        "version": 1,
        "shopping": {                 // null если не shopping-тип
          "quantity": 2,
          "unit": "л",
          "category": "Молочное"
        },
        "choreSchedule": null        // null если не chore-тип
      }
    ]
  }
}
```

**Автоархивация (shopping):** В списках покупок элементы с `isDone=true` и `doneAt` старше 24 часов автоматически скрываются сервером. Клиент не получает их в обычном запросе, но они доступны через `/sync`.

#### PATCH /lists/{id} 🔒
Обновить список. Передавать только изменённые поля.

```
← Request:
{
  "title": "Новое название",   // null = не менять
  "icon": "🏠",                // null = не менять
  "archived": true              // null = не менять; true = архивировать
}
```

#### DELETE /lists/{id} 🔒
Архивировать список (soft archive через `archivedAt`).

---

### 3.5. Элементы списков (Items)

#### Полная структура itemDTO

```typescript
interface ItemDTO {
  id: string                     // UUID
  listId: string                 // UUID
  title: string
  note: string | null
  sortOrder: number | null
  isDone: boolean
  doneAt: string | null          // RFC3339
  createdBy: string              // UUID
  createdAt: string              // RFC3339
  updatedAt: string              // RFC3339
  assignedTo: string | null      // UUID участника группы
  dueAt: string | null           // RFC3339
  isFavorite: boolean
  version: number                // для оффлайн-синхронизации

  // Только для type=shopping
  shopping: {
    quantity: number | null
    unit: string | null          // "кг", "л", "шт" и т.д.
    category: string | null      // "Молочное", "Овощи" и т.д.
  } | null

  // Только для type=home_chores
  choreSchedule: {
    intervalDays: number | null  // повтор каждые N дней
    daysOfWeek: string[]         // ["monday", "friday"]
    startDate: string            // "2026-04-01" (YYYY-MM-DD)
    endDate: string | null       // "2026-12-31" (YYYY-MM-DD)
    lastDoneAt: string | null    // RFC3339 — когда последний раз выполняли
    category: string | null      // "Кухня", "Ванная" и т.д.
  } | null
}
```

#### POST /lists/{listId}/items 🔒
Создать элемент.

```
← Request:
{
  "title": "Молоко",                          // обязательно
  "note": "2.5%, Простоквашино",              // опционально
  "sortOrder": 1.0,                           // опционально
  "assignedTo": "uuid",                       // опционально
  "dueAt": "2026-04-05T18:00:00Z",           // опционально, RFC3339
  "shopping": {                               // только для shopping-списков
    "quantity": 2,
    "unit": "л",
    "category": "Молочное"
  },
  "choreSchedule": {                          // только для home_chores-списков
    "intervalDays": 7,
    "daysOfWeek": ["monday"],
    "startDate": "2026-04-01",                // обязательно внутри блока
    "endDate": null,
    "category": "Кухня"
  }
}

→ Response 200: itemDTO
```

#### PATCH /items/{id} 🔒
Обновить элемент. Передавать только изменённые поля.

**Специальное значение `""` (пустая строка) — сброс в NULL:**
- `"assignedTo": ""` → убрать назначение
- `"dueAt": ""` → убрать дедлайн
- `"assignedTo": null` или отсутствие поля → не менять

```
← Request:
{
  "title": "Кефир",                   // null = не менять
  "note": "1%",                       // null = не менять
  "sortOrder": 2.0,                   // null = не менять
  "isDone": true,                     // null = не менять
  "assignedTo": "uuid" | "" | null,   // uuid=назначить, ""=сбросить, null=не менять
  "dueAt": "..." | "" | null,         // аналогично
  "isFavorite": true,                 // null = не менять
  "shopping": { ... },                // null = не менять
  "choreSchedule": { ... }            // null = не менять
}

→ Response 200: обновлённый itemDTO
```

**Побочные эффекты при `isDone: true`:**
- Устанавливается `doneAt = now()`
- Для home_chores: обновляется `choreSchedule.lastDoneAt = now()`
- Инкрементируется `version`

**Побочные эффекты при `isDone: false`:**
- `doneAt` сбрасывается в `null`

#### DELETE /items/{id} 🔒
Soft delete: элемент помечается `deletedAt`, version инкрементируется. Клиент узнаёт об удалении через `/sync`.

---

### 3.6. Категории

#### GET /categories?scope=shopping|chore 🔒
Получить категории: системные (для всех) + пользовательские (только свои).

```
→ Response 200:
{
  "data": {
    "categories": [
      { "id": "uuid", "scope": "shopping", "name": "Молочное", "isSystem": true, "userId": null },
      { "id": "uuid", "scope": "shopping", "name": "Мой раздел", "isSystem": false, "userId": "uuid" }
    ]
  }
}
```

**Системные категории (shopping):** Молочное, Мясо и рыба, Овощи и фрукты, Хлеб и выпечка, Напитки, Бытовая химия, Крупы и макароны, Замороженные продукты, Сладости, Другое.

**Системные категории (chore):** Кухня, Ванная, Спальня, Гостиная, Общее, Балкон, Прихожая.

#### POST /categories 🔒
Создать пользовательскую категорию.

```
← Request:
{
  "scope": "shopping",            // "shopping" | "chore"
  "name": "Бакалея"               // обязательно
}

→ Response 200: categoryDTO
```

---

### 3.7. Синхронизация (оффлайн-режим)

#### GET /sync?since={timestamp} 🔒
Получить все изменения после указанного момента. Возвращает **все** элементы, включая soft-deleted.

**Query-параметры:**
- `since` — RFC3339 timestamp (обязательно), например `2026-04-01T00:00:00Z`

```
→ Response 200:
{
  "data": {
    "items": [
      {
        "id": "uuid",
        "listId": "uuid",
        "title": "...",
        "...": "все поля itemDTO",
        "deleted": true              // true = элемент удалён (soft delete)
      }
    ],
    "timestamp": "2026-04-01T12:30:00Z"  // использовать как `since` при следующем вызове
  }
}
```

**Рекомендуемая логика синхронизации:**

1. При первом запуске: `GET /sync?since=1970-01-01T00:00:00Z`
2. Сохранить `timestamp` из ответа
3. При последующих запусках: `GET /sync?since={сохранённый timestamp}`
4. Для каждого элемента в ответе:
   - Если `deleted == true` → удалить локально
   - Если элемент существует локально → сравнить `version`, обновить если серверный выше
   - Если элемент не существует локально → создать
5. **Стратегия конфликтов:** last-write-wins по `updatedAt`

**Когда вызывать:**
- При возврате приложения из фона
- При восстановлении сетевого соединения
- Периодически в фоне (рекомендуемый интервал — 30 секунд при активном использовании)

---

### 3.8. Подсказки и рекомендации

#### GET /suggestions/chore-templates?locale=ru 🔒
Шаблоны домашних дел с рекомендуемой периодичностью.

```
→ Response 200:
{
  "data": {
    "templates": [
      {
        "id": "uuid",
        "category": "Общее",
        "intervalDays": 7,
        "title": "Пропылесосить квартиру",
        "description": "Пройтись по всем комнатам пылесосом"
      }
    ]
  }
}
```

**UX:** показывать при создании нового chore-элемента. Пользователь может:
- Взять шаблон как есть → `POST /lists/{id}/items` с `choreSchedule.intervalDays` из шаблона
- Изменить периодичность / категорию
- Создать свой

#### GET /suggestions/frequent-items?listId={uuid} 🔒
Часто добавляемые элементы в конкретном списке (до 20 штук).

```
→ Response 200:
{
  "data": {
    "items": [ /* массив itemDTO */ ]
  }
}
```

**UX:** показать как «Вы часто добавляли» при добавлении нового элемента в список.

#### GET /suggestions/favorites 🔒
Элементы, помеченные как избранные (из всех доступных списков).

```
→ Response 200:
{
  "data": {
    "items": [ /* массив itemDTO */ ]
  }
}
```

**UX:** показать как быстрый доступ или в строке поиска при добавлении.

---

## 4. Логика по типам списков

### 4.1. Список покупок (`shopping`)

**Создание элемента:** title + опционально `shopping.quantity`, `shopping.unit`, `shopping.category`

**Отметка «куплено»:** `PATCH /items/{id}` с `isDone: true`

**Автоархивация:** сервер автоматически скрывает элементы через 24 часа после `doneAt`. На клиенте можно показать обратный таймер «исчезнет через X часов» или просто скрыть отмеченные спустя время.

**Категории:** получить через `GET /categories?scope=shopping`, показать при создании/редактировании элемента.

**Избранное:** пометить через `PATCH /items/{id}` с `isFavorite: true`. Предлагать через `GET /suggestions/favorites`.

### 4.2. Список дел (`general_todos`)

**Создание элемента:** title + опционально `dueAt` (дедлайн)

**Дедлайны:** отображать как дату/время, подсвечивать просроченные (dueAt < now). Возможность экспорта в системный календарь (клиентская логика через Intent/Calendar API).

**Назначение:** в групповом списке — выбрать участника через `assignedTo`. Показать аватар/имя исполнителя.

### 4.3. Домашние дела (`home_chores`)

**Создание элемента:** title + `choreSchedule` с расписанием

**Расписание:**
- `intervalDays` — повтор каждые N дней (например, 7 = раз в неделю)
- `daysOfWeek` — по конкретным дням `["monday", "wednesday", "friday"]`
- `startDate` / `endDate` — период действия

**Дата последнего выполнения (`lastDoneAt`):** обновляется автоматически при `isDone: true`. Клиент рассчитывает:
- «Осталось X дней» = `lastDoneAt + intervalDays - now`
- «Просрочено на X дней» = `now - (lastDoneAt + intervalDays)`
- Цветовое кодирование: зелёный → жёлтый → красный

**Категории:** получить через `GET /categories?scope=chore`.

**Шаблоны:** при создании → предложить `GET /suggestions/chore-templates`.

---

## 5. Оффлайн-режим

### Архитектура

```
┌─────────────┐     ┌──────────────┐     ┌─────────────┐
│  UI Layer   │ ←→  │  Local DB    │ ←→  │  Sync Engine │ ←→ API
│             │     │  (Room/SQLDelight) │  │             │
└─────────────┘     └──────────────┘     └─────────────┘
```

### Принципы

1. **Все операции — сначала в локальную БД**, затем синхронизация
2. **Чтение — всегда из локальной БД** (мгновенный отклик)
3. **Запись — в локальную БД + в очередь отправки**
4. **Sync engine** периодически отправляет очередь и получает изменения

### Поля для синхронизации

| Поле | Назначение |
|------|-----------|
| `version` | Оптимистичная блокировка. При конфликте — побеждает больший version |
| `updatedAt` | Дополнительный критерий для LWW |
| `deletedAt` / `deleted` | Soft delete — клиент узнаёт об удалениях через sync |
| `timestamp` (из /sync) | Курсор для инкрементальной синхронизации |

### Алгоритм

```
1. При каждом действии пользователя:
   a. Применить изменение в локальной БД
   b. Добавить в outgoing queue (с optimistic version++)
   c. Показать результат пользователю немедленно

2. Sync worker (фоновый):
   a. Отправить pending changes из очереди (POST/PATCH/DELETE)
   b. GET /sync?since=<lastSync>
   c. Для каждого item из ответа:
      - deleted=true → удалить из локальной БД
      - version > local.version → перезаписать локальную копию
      - version <= local.version → пропустить (локальная копия новее)
   d. Сохранить timestamp для следующего sync
```

---

## 6. Экраны приложения (рекомендуемая навигация)

### Стартовый экран
- Проверить наличие токенов → если есть и refresh валиден → главный экран
- Если нет → экран входа (Login / Register / Guest)

### Главный экран (Tab Bar)
1. **Мои списки** — `GET /lists?scope=personal`
2. **Группы** — `GET /groups` → по каждой `GET /lists?scope=group&groupId=X`
3. **Настройки** — профиль, уведомления, привязка email

### Экран списка
- `GET /lists/{id}` → list + items
- Чекбокс → `PATCH /items/{id}` с `isDone`
- Добавить → форма с типо-зависимыми полями
- Долгое нажатие → редактировать / удалить / назначить

### Экран создания элемента
- По типу списка показывать разные поля:
  - **shopping:** title, quantity, unit, category
  - **general_todos:** title, note, dueAt, assignedTo (в групповых)
  - **home_chores:** title, intervalDays/daysOfWeek, startDate, category, assignedTo
- Секция «Подсказки»: frequent-items, favorites, chore-templates

### Экран группы
- Список участников (из ответа GET /groups — role)
- Создать инвайт → поделиться ссылкой
- Owner: удалить участника, передать роль, удалить группу

### Экран настроек уведомлений
- `GET /auth/notification-preferences`
- Тоглы по каналам
- `PUT /auth/notification-preferences` при переключении

---

## 7. Форматы данных

| Тип | Формат | Пример |
|-----|--------|--------|
| UUID | RFC 4122 | `550e8400-e29b-41d4-a716-446655440000` |
| Timestamp | RFC 3339 | `2026-04-01T12:00:00Z` |
| Date (расписание) | ISO 8601 | `2026-04-01` |
| Days of week | lowercase English | `monday`, `tuesday`, ... `sunday` |

---

## 8. Нереализованные фичи (📋 TODO)

Следующие функции описаны в ТЗ, но **ещё не реализованы на бэкенде**:

| Фича | Описание | Статус |
|------|----------|--------|
| Ротация исполнителей | Автоматическая смена assigned_to по расписанию | ❌ |
| Вложения (фото) | Таблица item_attachments, S3 storage, upload/download API | ❌ |
| Notification worker | Outbox polling → FCM/APNs push-уведомления | ❌ |
| Лимиты подписки | 1 группа / 2 человека бесплатно, подписка для расширения | ❌ |
| OAuth (Google, Apple) | Авторизация через OAuth-провайдеров | 📋 |
| Геймификация | Счётчики, стрики, достижения | 📋 |
| Внутренний календарь | Отображение задач с дедлайнами в виде календаря | 📋 |
| Мультиязычность | i18n для контента и шаблонов | 📋 |

**Рекомендация:** на клиенте можно заложить UI для ротации и вложений, но оставить неактивным до реализации бэкенда.
