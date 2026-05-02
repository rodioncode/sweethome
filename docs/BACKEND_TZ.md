# SweetHome — Бэкенд ТЗ

**Версия:** 1.0  
**Дата:** 2026-05-02  
**Аудитория:** Backend-разработчик  
**Стек:** Go microservices + PostgreSQL  
**Текущий контракт API:** `CLIENT_API_GUIDE.md`  

Этот документ описывает **изменения и дополнения** к существующему API. Все эндпоинты из `CLIENT_API_GUIDE.md` остаются в силе.

---

## 1. Изменения в существующих моделях данных

### 1.1. Workspace (таблица `workspace.workspaces`)

Добавить поля:

| Поле | Тип | Описание |
|------|-----|----------|
| `type` | ENUM | Добавить значение `work` к существующим `personal \| group \| family \| mentoring` |
| `work_hours_start` | TIME? | Начало рабочего дня (например, `09:00`) |
| `work_hours_end` | TIME? | Конец рабочего дня (например, `18:00`) |
| `work_days` | TEXT[]? | Рабочие дни, например `["mon","tue","wed","thu","fri"]` |

Поля `work_hours_*` актуальны только для типа `work`.

**Изменения в API:**
- `POST /v1/workspaces` — принимать `type: "work"`
- `PATCH /v1/workspaces/{id}` — принимать `workHoursStart`, `workHoursEnd`, `workDays`
- `GET /v1/workspaces` — возвращать новые поля в ответе
- При регистрации (`POST /v1/auth/register`) — создавать только `personal` workspace. **`work` workspace создаётся вручную** через обычный `POST /v1/workspaces` с `type: "work"`

### 1.2. TodoItem — Shopping детали (таблица `list.shopping_item_fields`)

Добавить поля:

| Поле | Тип | Описание |
|------|-----|----------|
| `brand` | TEXT? | Бренд / марка товара |
| `image_url` | TEXT? | URL фото товара |
| `product_url` | TEXT? | Ссылка на товар в интернет-магазине |

**Изменения в API:**
- `POST /v1/lists/{id}/items` — принимать `shoppingDetails.brand`, `shoppingDetails.imageUrl`, `shoppingDetails.productUrl`
- `PATCH /v1/items/{id}` — принимать те же поля
- `GET /v1/lists/{id}` — возвращать новые поля в элементах

### 1.3. TodoList (таблица `list.lists`)

Добавить поле:

| Поле | Тип | Описание |
|------|-----|----------|
| `room_type` | TEXT? | Тип комнаты: `kitchen \| bedroom \| living_room \| bathroom \| balcony \| kids_room \| custom` |
| `room_name` | TEXT? | Название комнаты (если `room_type = custom`) |

**Изменения в API:**
- `POST /v1/lists` — принимать `roomType`, `roomName`
- `PATCH /v1/lists/{id}` — принимать `roomType`, `roomName`
- `GET /v1/lists` — поддерживать фильтр `?roomType=kitchen`

---

## 2. Новые сущности

### 2.1. HomeCurrency (таблица `gamification.home_currencies`)

```sql
CREATE TABLE gamification.home_currencies (
  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  workspace_id UUID NOT NULL REFERENCES workspace.workspaces(id) ON DELETE CASCADE,
  name        TEXT NOT NULL DEFAULT 'Монеты',
  icon        TEXT NOT NULL DEFAULT 'coin',  -- ключ из набора иконок
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
-- UNIQUE: один currency на workspace
CREATE UNIQUE INDEX ON gamification.home_currencies(workspace_id);
```

**Новые эндпоинты:**

```
GET    /v1/workspaces/{id}/currency
PATCH  /v1/workspaces/{id}/currency    body: { name, icon }
```

Ответ:
```json
{
  "data": {
    "id": "uuid",
    "workspaceId": "uuid",
    "name": "Монеты",
    "icon": "coin"
  }
}
```

Доступен только в workspace типа `family`. Возвращать `403 Forbidden` для других типов.

### 2.2. MemberBalance (таблица `gamification.member_balances`)

```sql
CREATE TABLE gamification.member_balances (
  workspace_id UUID NOT NULL REFERENCES workspace.workspaces(id) ON DELETE CASCADE,
  user_id      UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
  balance      INT NOT NULL DEFAULT 0,
  total_earned INT NOT NULL DEFAULT 0,
  updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (workspace_id, user_id)
);
```

**Логика:** При отметке `isDone = true` на элементе с `reward != null`:
- `balance += reward` для `assignedTo` пользователя
- `total_earned += reward` для `assignedTo`

**Новые эндпоинты:**
```
GET /v1/workspaces/{id}/leaderboard
    Ответ: список участников, отсортированный по balance DESC
    Query: ?period=week|month|all_time
```

### 2.3. RewardShop — Prizes (таблица `gamification.prizes`)

```sql
CREATE TABLE gamification.prizes (
  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  workspace_id UUID NOT NULL REFERENCES workspace.workspaces(id) ON DELETE CASCADE,
  title        TEXT NOT NULL,
  description  TEXT,
  price        INT NOT NULL,  -- в единицах HomeCurrency
  created_by   UUID NOT NULL REFERENCES auth.users(id),
  created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
  archived_at  TIMESTAMPTZ
);
```

**Новые эндпоинты:**
```
GET    /v1/workspaces/{id}/prizes           — все участники
POST   /v1/workspaces/{id}/prizes           — только owner и admin (403 иначе)
    body: { title, description, price }
PATCH  /v1/workspaces/{id}/prizes/{pid}     — только owner и admin (403 иначе)
    body: { title, description, price }
DELETE /v1/workspaces/{id}/prizes/{pid}     — только owner и admin (403 иначе)
    → soft delete (archivedAt)

POST   /v1/workspaces/{id}/prizes/{pid}/redeem  — любой участник
    Списывает price монет у текущего пользователя
    Возвращает 400 если недостаточно баланса
```

---

## 3. Удаление аккаунта

### Новый эндпоинт

```
DELETE /v1/users/me
Header: Authorization: Bearer {accessToken}
Body: { "confirmEmail": "user@email.com" }
```

**Клиент не вызывает этот эндпоинт офлайн** — кнопка на клиенте заблокирована без сети.

**Логика удаления (в транзакции):**
1. Проверить `confirmEmail == user.email`
2. Для каждого workspace, где user = owner:
   - Если есть другие участники: передать ownership следующему по дате вступления
   - Если нет участников: soft delete workspace
3. Удалить из всех других workspace (member)
4. Сбросить `assignedTo` на всех элементах пользователя
5. Удалить все токены (auth tokens, refresh tokens, devices)
6. Удалить профиль, уведомления
7. Удалить/анонимизировать данные пользователя (GDPR)

**Ответы:**
```
200 OK: { "message": "Account deleted" }
400 Bad Request: { "error": "email_mismatch" }
401 Unauthorized
```

---

## 4. Регистрация — Создание двух workspace

Изменить `POST /v1/auth/register` и `POST /v1/auth/guest`:

**После создания пользователя автоматически создавать:**
1. Workspace `{ type: "personal", title: "Личное" }` с owner = new user
2. Workspace `{ type: "work", title: "Работа" }` с owner = new user

Возвращать IDs созданных workspace в ответе:
```json
{
  "data": {
    "accessToken": "...",
    "refreshToken": "...",
    "workspaces": [
      { "id": "uuid-1", "type": "personal", "title": "Личное" },
      { "id": "uuid-2", "type": "work", "title": "Работа" }
    ]
  }
}
```

---

## 5. Фильтрация списков по комнате

Обновить `GET /v1/lists`:

```
GET /v1/lists?workspaceId={id}&roomType=kitchen
GET /v1/lists?workspaceId={id}&roomType=custom&roomName=Гараж
```

Возвращать `roomType` и `roomName` в каждом объекте списка.

---

## 6. Изменения в sync-эндпоинте

`GET /v1/sync?since=X` должен включать в дельту:
- Изменения в `shopping_item_fields` (новые поля brand, imageUrl, productUrl)
- Новые сущности: prizes, member_balances (если workspace = family)

---

## 7. MMP — Цели (Goals)

**Scope: MMP. Описано здесь для планирования схемы БД.**

```sql
CREATE TABLE list.goals (
  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  workspace_id UUID NOT NULL REFERENCES workspace.workspaces(id) ON DELETE CASCADE,
  created_by   UUID NOT NULL REFERENCES auth.users(id),
  title        TEXT NOT NULL,
  description  TEXT,
  deadline     DATE,
  is_done      BOOL NOT NULL DEFAULT false,
  done_at      TIMESTAMPTZ,
  created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
  archived_at  TIMESTAMPTZ
);

CREATE TABLE list.goal_steps (
  id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  goal_id   UUID NOT NULL REFERENCES list.goals(id) ON DELETE CASCADE,
  title     TEXT NOT NULL,
  is_done   BOOL NOT NULL DEFAULT false,
  done_at   TIMESTAMPTZ,
  sort_order INT NOT NULL DEFAULT 0
);
```

**Эндпоинты (MMP):**
```
GET    /v1/workspaces/{id}/goals
POST   /v1/workspaces/{id}/goals
GET    /v1/goals/{id}
PATCH  /v1/goals/{id}
DELETE /v1/goals/{id}
POST   /v1/goals/{id}/steps
PATCH  /v1/goals/{id}/steps/{stepId}
DELETE /v1/goals/{id}/steps/{stepId}
POST   /v1/goals/{id}/steps/{stepId}/complete
```

---

## 8. MMP — Шаринг календаря (Calendar Sharing)

**Scope: MMP. Схема для предстоящей реализации.**

```sql
CREATE TABLE calendar.sharing_settings (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  workspace_id    UUID NOT NULL REFERENCES workspace.workspaces(id) ON DELETE CASCADE,
  user_id         UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
  visibility_level TEXT NOT NULL,  -- "busy_only" | "by_type" | "custom"
  allowed_types   TEXT[],          -- для visibility_level = "by_type"
  allowed_list_ids UUID[],         -- для visibility_level = "custom"
  share_token     TEXT UNIQUE,     -- для внешнего доступа
  PRIMARY KEY (workspace_id, user_id)
);
```

---

## 9. Безопасность и нефункциональные требования

### Rate Limiting (обновление)
- `DELETE /v1/users/me`: 3 req/day на IP (GDPR deletion abuse prevention)
- `PATCH /v1/workspaces/{id}/currency`: 10 req/min

### Валидация
- `work_hours_start` / `work_hours_end`: формат `HH:MM`, start < end
- `work_days`: только валидные значения `mon|tue|wed|thu|fri|sat|sun`
- `HomeCurrency.name`: 1–30 символов, не пустая
- `Prize.price`: > 0, <= 999999
- `brand`: <= 100 символов
- `productUrl`: валидный URL или пустая строка
- `imageUrl`: валидный URL (CDN) или base64 не более 5MB

### GDPR — Удаление аккаунта
При `DELETE /v1/users/me`:
- Физически удалять: auth.users, auth.tokens, auth.devices
- Анонимизировать: сообщения в чате (заменить sender_id на NULL, display_name на «Удалённый пользователь»)
- Soft delete: списки, элементы — сохранить для других участников

---

## 10. Матрица изменений — Приоритеты

| Изменение | Приоритет | Затронутые таблицы |
|-----------|-----------|---------------------|
| Workspace type `work` + авто-создание | 🔴 MVP | `workspace.workspaces` |
| Shopping item детали (brand, imageUrl, productUrl) | 🔴 MVP | `list.shopping_item_fields` |
| TodoList roomType | 🔴 MVP | `list.lists` |
| Workspace work_hours | 🟡 MVP | `workspace.workspaces` |
| DELETE /users/me | 🔴 MVP | `auth.*`, `workspace.*` |
| HomeCurrency + MemberBalance | 🟡 MMP | `gamification.*` |
| Prizes / Reward Shop | 🟡 MMP | `gamification.prizes` |
| Goals + GoalSteps | 🟡 MMP | `list.goals`, `list.goal_steps` |
| Calendar sharing | 🔵 MMP | `calendar.sharing_settings` |
