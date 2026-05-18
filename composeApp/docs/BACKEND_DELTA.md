# SweetHome Backend Delta — для дизайна Cozy

> Документ для команды бэкенда. Описывает изменения в REST API, необходимые для нового UI (Cozy direction A), внедрённого в Compose Multiplatform клиент.
>
> Базовый контракт описан в `uploads/CLIENT_GUIDE.md` (1023 строки). Этот файл — только дельта.

---

## TL;DR

Cozy-редизайн добавляет 4 новые функциональные области:
1. **Pet/Mascot** — личный + общий «guardian» питомец, аксессуары, дневник
2. **Menu / Meal planning** — недельное меню с конверсией в шоппинг-лист
3. **Kid mode** — отдельный детский профиль с приключенческим путём и призами
4. **Onboarding + Permissions** — 5-step setup + персистентное состояние разрешений

Плюс точечные расширения существующих сущностей (gamification stars, list item typedFields, WorkHours, family guardian pet).

---

## 1. Pet / Mascot

### Сущности

```kotlin
data class Pet(
    val id: String,
    val workspaceId: String,           // на каком workspace висит
    val ownerId: String?,              // null если это guardian-питомец семьи/группы
    val scope: PetScope,               // PERSONAL | GUARDIAN
    val species: PetSpecies,           // RACCOON|FOX|CAT|HEDGIE|PANDA|BUNNY
    val name: String,
    val stage: PetStage,               // BABY|CHILD|TEEN|SCHOOLKID|YOUNG|ADULT
    val level: Int,                    // 1..12
    val exp: Int,                      // current XP (used to compute level)
    val mood: PetMood,                 // HAPPY|SLEEPY|EXCITED|SAD (computed server-side weekly)
    val accessories: List<String>,     // ids of equipped accessories
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class PetAccessory(
    val id: String,
    val emoji: String,
    val name: String,
    val slot: AccessorySlot,           // HEAD|NECK|FACE|BACK
    val price: Int,                    // в звёздах (gamification stars)
    val unlocked: Boolean,             // user has bought/earned
    val equipped: Boolean,             // currently worn
    val reason: String?,               // как получить ("За 30 закрытых задач")
)

data class PetDiaryEntry(
    val id: String,
    val petId: String,
    val emoji: String,
    val text: String,                  // текст «письма» от питомца
    val createdAt: Instant,
)

data class PetMilestone(
    val stage: PetStage,
    val level: Int,
    val displayName: String,
    val emoji: String,
    val unlockedAt: Instant?,          // null if locked
    val current: Boolean,
)
```

### Endpoints

| Метод | Путь | Описание |
|---|---|---|
| GET    | `/api/v1/workspaces/{wsId}/pets/me` | Личный питомец пользователя в workspace |
| GET    | `/api/v1/workspaces/{wsId}/pets/guardian` | Общий «berloga» питомец (только family/group) |
| POST   | `/api/v1/workspaces/{wsId}/pets` | Создать питомца. Body: `{ species, name, scope: 'personal'|'guardian' }` |
| PATCH  | `/api/v1/pets/{petId}` | Переименовать. Body: `{ name }` |
| POST   | `/api/v1/pets/{petId}/actions/feed` | Покормить (использовать `Idempotency-Key` header) |
| POST   | `/api/v1/pets/{petId}/actions/pet` | Погладить |
| POST   | `/api/v1/pets/{petId}/actions/dress` | Обновить аксессуары. Body: `{ accessoryIds: [...] }` |
| POST   | `/api/v1/pets/{petId}/actions/photo` | Сделать фото-snapshot. Body: `{ companionPetIds: [...] }` |
| GET    | `/api/v1/pets/{petId}/accessories` | Все доступные + статусы (owned/locked/equipped) |
| POST   | `/api/v1/pets/{petId}/accessories/{accId}/buy` | Купить за звёзды. 402 если не хватает звёзд |
| POST   | `/api/v1/pets/{petId}/accessories/{accId}/equip` | Надеть. Auto-снимает другие из того же слота |
| POST   | `/api/v1/pets/{petId}/accessories/{accId}/unequip` | Снять |
| GET    | `/api/v1/pets/{petId}/diary?cursor=&limit=20` | Дневник (paginated) |
| GET    | `/api/v1/pets/{petId}/milestones` | 6 стадий со статусом |
| GET    | `/api/v1/pets/{petId}/friendships` | С кем «дружит» этот питомец (для friendship-hint в карточке) |
| WS     | `/api/v1/pets/{petId}/stream` | Optional: real-time мood/level updates |

### Бизнес-логика

- **EXP начисляется автоматически** при `task.complete` через сервис gamification. Маппинг: `task.priority` × `task.list_type.exp_multiplier` + bonuses (streak/early/perfectly_on_time). Уровни: `level = floor(sqrt(exp / 50)) + 1`, `level <= 12`.
- **Stage** определяется по level: BABY(1)/CHILD(3)/TEEN(5)/SCHOOLKID(7)/YOUNG(10)/ADULT(12).
- **Mood** считается сервером раз в день: HAPPY если выполнено >80% задач за неделю; SLEEPY если 50-80%; SAD если <50% + есть просрочки; EXCITED если streak >=7 дней.
- **Guardian pet** в family/group workspace — общий для всех участников. EXP суммируется от всех завершённых задач workspace (с лимитом 5 EXP/задача).
- **Personal pet** — один на user на personal workspace, плюс по одному на каждый family/group workspace (опционально, по желанию пользователя).

---

## 2. Menu / Meal planning

### Сущности

```kotlin
data class MenuWeek(
    val workspaceId: String,
    val weekStart: LocalDate,          // понедельник
    val days: List<MenuDay>,
    val chefOfTheWeekUserId: String?,  // null если не назначен
)

data class MenuDay(
    val date: LocalDate,
    val slots: Map<MealSlot, MenuCell?>,
)

enum class MealSlot { BREAKFAST, SNACK_1, LUNCH, SNACK_2, DINNER }

sealed interface MenuCell {
    val name: String
    val emoji: String

    data class Recipe(
        val recipeId: String,
        override val name: String,
        override val emoji: String,
        val author: String,             // имя того, кто положил
        val servings: Int,
    ) : MenuCell

    data class Free(override val name: String, override val emoji: String) : MenuCell

    data class Out(
        override val name: String,
        override val emoji: String,
        val note: String?,              // место/повод
    ) : MenuCell

    data class Delivery(
        override val name: String,
        override val emoji: String,
        val price: Int?,                // в рублях
        val service: String?,           // «Самокат», «Delivery Club»
    ) : MenuCell
}

data class ShoppingListPreview(
    val items: List<ShoppingItem>,
    val totalIngredients: Int,
)
data class ShoppingItem(
    val ingredient: String,
    val quantity: Double?,
    val unit: String?,
    val sourceRecipeIds: List<String>,
    val category: String?,             // «Молочка», «Овощи» — для группировки
)
```

### Endpoints

| Метод | Путь | Описание |
|---|---|---|
| GET    | `/api/v1/workspaces/{wsId}/menu?weekStart=YYYY-MM-DD` | Меню недели |
| PUT    | `/api/v1/workspaces/{wsId}/menu/days/{date}/slots/{slot}` | Заполнить слот. Body — `MenuCell` JSON с дискриминатором `kind`. |
| DELETE | `/api/v1/workspaces/{wsId}/menu/days/{date}/slots/{slot}` | Очистить слот |
| POST   | `/api/v1/workspaces/{wsId}/menu/to-shopping` | Создать/обновить shopping-list из меню. Body: `{ weekStart, targetListId?: string }`. Returns `{ listId, items }`. Дедуп ингредиентов по `ingredient+unit`. |
| GET    | `/api/v1/workspaces/{wsId}/menu/to-shopping/preview?weekStart=` | Превью без создания списка |
| GET    | `/api/v1/recipes?search=&cuisine=` | Поиск рецептов. Recipe DTO: `{ id, name, emoji, cuisine, ingredients: [...], steps: [...], servings, author }` |
| POST   | `/api/v1/workspaces/{wsId}/menu/chef` | Назначить шефа недели. Body: `{ userId, weekStart }` |
| GET    | `/api/v1/workspaces/{wsId}/menu/chef?weekStart=` | Кто шеф недели |

### Бизнес-логика

- **Слот может быть null** — пустой, отображается как «+»
- **dedupe**: при `to-shopping` ингредиенты с одинаковыми `ingredient+unit` суммируются по `quantity`. Если unit разные — разные строки
- **Recipes** — пока shared (общая база) + workspace-private (созданные внутри family). См. существующий `templates`-эндпоинт, можно расширить через `templateType: 'recipe' | 'list_template' | 'goal_template'`

---

## 3. Kid mode

### Сущности

```kotlin
data class KidProfile(
    val userId: String,                // тот же user id, но с флагом
    val parentUserId: String,
    val isKidModeEnabled: Boolean,
    val dailyStarLimit: Int?,          // максимум звёзд в день, для anti-grinding
    val allowShopRedeem: Boolean,
    val petId: String,                 // дефолт-питомец для kid mode
)

data class AdventurePath(
    val userId: String,
    val date: LocalDate,
    val stops: List<AdventureStop>,
    val prize: KidPrize?,              // финальный приз
)

data class AdventureStop(
    val id: String,
    val taskId: String,                // связан с обычной задачей
    val emoji: String,
    val title: String,
    val reward: Int,                   // звёзды
    val done: Boolean,
    val current: Boolean,              // первый незакрытый
    val order: Int,
)

data class KidPrize(
    val id: String,
    val name: String,
    val emoji: String,
    val price: Int,                    // полная цена в звёздах
    val remaining: Int,                // сколько ещё нужно
    val status: PrizeStatus,           // LOCKED | AVAILABLE | OWNED
    val approvedByParent: Boolean,     // нужно ли родительское разрешение
)

data class KidLetter(
    val id: String,
    val from: String,                  // «Мама», «Питомец», «SweetHome»
    val title: String,
    val body: String,
    val emoji: String,
    val readAt: Instant?,              // null = unread
    val sentAt: Instant,
)

data class KidStreak(
    val current: Int,                  // дней подряд
    val best: Int,
    val last7: List<Boolean>,          // последние 7 дней
)
```

### Endpoints

| Метод | Путь | Описание |
|---|---|---|
| GET    | `/api/v1/users/{id}/kid-profile` | Профиль (404 если не включён) |
| POST   | `/api/v1/users/{id}/kid-profile/enable` | Родитель включает. Body: `{ parentPin, dailyStarLimit?, petId }` |
| POST   | `/api/v1/users/{id}/kid-profile/disable` | Body: `{ parentPin }` |
| GET    | `/api/v1/users/{id}/adventure-path?date=YYYY-MM-DD` | Путь на день |
| POST   | `/api/v1/users/{id}/adventure-stops/{stopId}/complete` | Завершить stop (фактически = `tasks/{taskId}/complete`) |
| GET    | `/api/v1/users/{id}/prizes` | Список призов |
| POST   | `/api/v1/users/{id}/prizes` | Родитель создаёт приз. Body: `{ name, emoji, price, approvedByParent: true }` |
| POST   | `/api/v1/users/{id}/prizes/{prizeId}/redeem` | Забрать приз. 402 если не хватает звёзд |
| GET    | `/api/v1/users/{id}/streak` | Текущая серия + 7 дней |
| GET    | `/api/v1/users/{id}/letters?unreadOnly=` | Письма |
| POST   | `/api/v1/users/{id}/letters/{letterId}/read` | Отметить прочитанным |

### Бизнес-логика

- **Daily star limit** опционален. Когда лимит достигнут — клиент показывает «Завтра ещё больше!» и блокирует начисление до полуночи (server-side enforcement).
- **Adventure stops** генерируются автоматически из задач, назначенных на этого user-а + помеченных `kid_friendly=true`. Порядок — сначала текущая, потом по due_date.
- **Letters** — система отправляет автоматически при streak-milestones (7/30/100 дней) и при крупных достижениях. Родитель может писать через отдельный endpoint `POST /api/v1/users/{kidId}/letters`.

---

## 4. Onboarding state

```kotlin
data class OnboardingState(
    val userId: String,
    val step: Int,                     // 1..5
    val path: OnboardingPath?,         // SOLO | FAMILY | GROUP
    val familyName: String?,
    val permissions: OnboardingPermissions,
    val completedAt: Instant?,
)

data class OnboardingPermissions(
    val push: Boolean = false,
    val calendar: Boolean = false,
    val location: Boolean = false,
    val contacts: Boolean = false,
    val askedAt: Map<String, Instant> = emptyMap(),
)
```

| Метод | Путь | Описание |
|---|---|---|
| GET  | `/api/v1/users/me/onboarding` | Текущий step |
| PUT  | `/api/v1/users/me/onboarding` | Body: частичный update |
| POST | `/api/v1/users/me/onboarding/complete` | Финальный коммит |

Опционально — состояние можно хранить только клиентски. Но для multi-device консистентности рекомендую сервер.

---

## 5. Permissions persistence

Для permission ask screens (PUSH/CALENDAR/LOCATION/CONTACTS) нужно знать, **когда** мы спрашивали — чтобы не показывать второй раз сразу:

| Метод | Путь | Описание |
|---|---|---|
| GET  | `/api/v1/users/me/permissions` | `{ permissions: { push: { granted, askedAt }, ... } }` |
| POST | `/api/v1/users/me/permissions` | Body: `{ type: 'push'|'calendar'|'location'|'contacts', granted: bool }`. Server-side stamp `askedAt = now()` |

---

## 6. Расширение существующих сущностей

### 6.1 `Item.typedFields`

8 типов списков (shopping/home_chores/general_todos/study/travel/media/wishlist/custom) имеют разные специфичные поля. Предложение: добавить **`typedFields: JsonObject`** в существующий `Item` API. Server-side валидация по схеме типа списка (или хранить как opaque JSONB и валидировать на клиенте).

```kotlin
data class Item(
    // существующие: id, title, isDone, priority, assignedTo, dueDate, subtasks, tags, notes
    val typedFields: Map<String, JsonElement>,
)
```

Per-type field schemas:
- **shopping**: `quantity, unit, category, brand, store, price, photo, link`
- **home_chores**: `zone (Кухня|Гостиная|Спальня|Ванная|Балкон), schedule (daily|weekly|monthly), difficulty (1..5), rotationOrder: [userId...], lastDoneBy, lastDoneAt`
- **general_todos**: ничего дополнительного (используют общие поля)
- **study**: `subject, taskType (homework|exam|project), studentId, teacher, deadline, attachments`
- **travel**: `category (Документы|Одежда|...), quantity, checklistStatus (packed|unpacked), luggage`
- **media**: `mediaType (book|movie|series|article), status (want|in_progress|done), rating (1..5), link`
- **wishlist**: `photo, priceRange, link, occasion, priority, claimedBy, claimedAt`

### 6.2 Workspace.guardianPetId

```kotlin
data class Workspace(
    // существующие
    val guardianPetId: String?,        // для family/group типа
)
```

### 6.3 WorkHours / Quiet hours

```kotlin
data class WorkHours(
    val days: List<DayOfWeek>,         // MON..SUN
    val startTime: LocalTime,
    val endTime: LocalTime,
)

GET /api/v1/users/me/work-hours
PUT /api/v1/users/me/work-hours
```

В quiet hours сервер должен не слать push-уведомления (кроме `urgent` приоритета).

### 6.4 Gamification stars

Существующий `gamification` API расширить:

```kotlin
GET /api/v1/users/me/stars → { total: Int, lifetime: Int, sources: List<StarSource> }
GET /api/v1/workspaces/{wsId}/leaderboard?period=week|month|all
```

Изменения звёзд — **только server-side** в ответ на `task.complete` или `prize.redeem`. Клиент не может начислять напрямую.

### 6.5 Empty-state suggestions

```kotlin
GET /api/v1/workspaces/{wsId}/list-suggestions → List<ListSuggestion>
data class ListSuggestion(val emoji: String, val title: String, val templateId: String?)
```

Используется в `ListsEmptyScreen` — top-3 suggestion-chips на основе истории пользователя или дефолтных шаблонов.

---

## 7. Контракт ошибок

Все новые endpoints следуют существующему `ErrorResponse`:
```json
{ "error": { "code": "INSUFFICIENT_STARS", "message": "...", "details": {} } }
```

Новые коды:
- `INSUFFICIENT_STARS` (402) — не хватает звёзд для покупки prize / accessory
- `PET_ALREADY_EXISTS` (409) — попытка создать второго personal/guardian
- `KID_MODE_LIMIT_REACHED` (429) — daily star limit
- `PARENT_PIN_INVALID` (401) — для kid mode enable/disable
- `MENU_SLOT_INVALID` (400) — неверная комбинация date/slot

---

## 8. Sync / Offline

Все новые сущности следуют существующей offline-first стратегии (`sync` endpoint, optimistic updates с localStorage backup, refresh-token rotation на 401). Pet actions и menu edits должны быть **idempotent** (через `Idempotency-Key` header) — клиент может повторно отправлять offline-копию.

WebSocket стримы (опциональные):
- `/api/v1/pets/{petId}/stream` — pet mood/level updates
- `/api/v1/workspaces/{wsId}/menu/stream` — multi-user menu editing

---

## 9. Приоритет имплементации

Сверху вниз — то что блокирует UI:

1. **High** (блокирует MVP Cozy):
   - Pet create/get/feed/pet (без accessories)
   - WorkHours GET/PUT
   - `Item.typedFields` хранение (можно opaque JSON, валидация на клиенте)
   - Onboarding state (опционально, можно отложить — клиент хранит локально)
2. **Medium** (нужно для полной демо):
   - Menu CRUD + to-shopping
   - Accessories + buy/equip
   - Pet diary
   - Gamification leaderboard
3. **Low** (можно после релиза):
   - Kid mode (целый блок)
   - Pet streaming
   - Empty-state suggestions
   - Permission persistence

---

## 10. Что НЕ меняется

- Auth flow (JWT, refresh, OAuth)
- Workspaces CRUD
- Lists/items базовые CRUD (только добавляется поле `typedFields`)
- Notifications (только добавляются новые типы: `pet_milestone_reached`, `prize_available`, `kid_streak_milestone`, `menu_chef_assigned`)
- Calendar / Chat / Achievements / Goals / Profile / Templates / Devices
