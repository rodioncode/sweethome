# SweetHome — Клиентское ТЗ (KMP)

**Версия:** 1.0  
**Дата:** 2026-05-02  
**Аудитория:** KMP-разработчик (Kotlin Multiplatform + Compose Multiplatform)  
**Текущая база:** `composeApp/src/commonMain/kotlin/com/jetbrains/kmpapp/`  

---

## 1. Архитектурные принципы (не меняются)

- **MVVM**: ViewModel + StateFlow + Compose State
- **Offline-first**: Room DB, чтение всегда из локальной БД
- **LWW sync**: `version` field, delta-sync через `GET /sync?since=X`
- **DI**: Koin (модули: data, screen, platform)
- **Navigation**: Compose Navigation Multiplatform

---

## 2. Изменения в моделях данных

### 2.1. GroupType (WorkspaceType)

Файл: `data/groups/GroupsModels.kt`

```kotlin
// Добавить значение work к существующему enum
enum class WorkspaceType {
    personal, group, family, mentoring, work  // добавить work
}
```

### 2.2. Group (Workspace) модель

Файл: `data/groups/GroupsModels.kt`

```kotlin
data class Group(
    val id: String,
    val title: String,
    val icon: String?,
    val ownerId: String,
    val createdAt: String,
    val updatedAt: String,
    val archivedAt: String?,
    val role: String,
    val type: String,
    // Новые поля:
    val workHoursStart: String?,    // "09:00" — локальное время устройства, не UTC
    val workHoursEnd: String?,      // "18:00" — локальное время устройства, не UTC
    val workDays: List<String>?,    // ["mon","tue","wed","thu","fri"]
)
```

### 2.3. TodoItem — Shopping детали

Файл: `data/lists/ListsModels.kt`

```kotlin
@Serializable
data class ShoppingItemFields(
    val quantity: Float? = null,
    val unit: String? = null,
    val category: String? = null,
    // Новые поля MVP:
    val brand: String? = null,
    val productUrl: String? = null,
    // MMP (требует CDN на бэкенде):
    val imageUrl: String? = null,
)
```

### 2.4. TodoList — Room type

Файл: `data/lists/ListsModels.kt`

```kotlin
@Serializable
data class TodoList(
    // ...существующие поля...
    val roomType: String? = null,   // "kitchen" | "bedroom" | etc.
    val roomName: String? = null,   // для custom комнат
)
```

### 2.5. Новые модели — HomeCurrency

Файл: `data/gamification/GamificationModels.kt` (новый файл)

```kotlin
@Serializable
data class HomeCurrency(
    val id: String,
    val workspaceId: String,
    val name: String,
    val icon: String,
)

@Serializable
data class MemberBalance(
    val userId: String,
    val displayName: String,
    val avatarUrl: String?,
    val balance: Int,
    val totalEarned: Int,
)

@Serializable
data class Prize(
    val id: String,
    val workspaceId: String,
    val title: String,
    val description: String?,
    val price: Int,
    val archivedAt: String?,
)
```

---

## 3. Новые API-клиенты

### 3.1. GamificationApi

Файл: `data/gamification/GamificationApi.kt` (новый)

```kotlin
interface GamificationApi {
    suspend fun getCurrency(workspaceId: String): HomeCurrency
    suspend fun updateCurrency(workspaceId: String, name: String, icon: String): HomeCurrency
    suspend fun getLeaderboard(workspaceId: String, period: String): List<MemberBalance>
    suspend fun getPrizes(workspaceId: String): List<Prize>
    suspend fun createPrize(workspaceId: String, title: String, description: String?, price: Int): Prize
    suspend fun updatePrize(workspaceId: String, prizeId: String, title: String, description: String?, price: Int): Prize
    suspend fun deletePrize(workspaceId: String, prizeId: String)
    suspend fun redeemPrize(workspaceId: String, prizeId: String)
}
```

### 3.2. ProfileApi — deleteAccount

Файл: `data/profile/ProfileApi.kt` (дополнить)

```kotlin
interface ProfileApi {
    // ...существующие методы...
    suspend fun deleteAccount(confirmEmail: String)
}
```

---

## 4. Обновление существующих слоёв

### 4.1. GroupsRepository — work hours

Файл: `data/groups/GroupsRepository.kt`

```kotlin
interface GroupsRepository {
    // ...существующие методы...
    suspend fun updateWorkHours(
        workspaceId: String,
        start: String?,
        end: String?,
        days: List<String>?
    ): Group
}
```

### 4.2. ListsRepository — room filter

Файл: `data/lists/ListsRepository.kt`

```kotlin
interface ListsRepository {
    // ...существующие методы...
    suspend fun getListsByRoom(workspaceId: String, roomType: String?): List<TodoList>
    suspend fun updateListRoom(listId: String, roomType: String?, roomName: String?): TodoList
}
```

---

## 5. Навигация — обновлённая схема

Файл: `App.kt`

### Текущая навигация (5 табов)
Dashboard → Home → Lists → Templates → Groups

### Обновлённая навигация — новые destinations

```kotlin
// Добавить к существующим:
@Serializable object HomeCurrencySettingsDestination
@Serializable data class HomeRoomsDestination(val workspaceId: String)
@Serializable object DeleteAccountDestination
@Serializable data class CreateTaskDestination(val listId: String, val workspaceId: String)
@Serializable data class WorkHoursSettingsDestination(val workspaceId: String)
```

---

## 6. Главный экран (Dashboard) — переработка

### HomeScreen.kt + HomeViewModel.kt

**Что изменить:**

```kotlin
// HomeViewModel — новое состояние
data class HomeUiState(
    val hasWorkContext: Boolean = false,           // work workspace существует
    val currentContext: WorkContext = WorkContext.PERSONAL,
    val isContextManuallyOverridden: Boolean = false,  // пользователь переключил вручную
    val todayItems: List<TodoItemWithList> = emptyList(),   // первые 3 по приоритету
    val todayItemsTotal: Int = 0,                           // всего задач на сегодня
    val overdueItems: List<TodoItemWithList> = emptyList(),
    val isLoading: Boolean = false,
)

enum class WorkContext { WORK, PERSONAL }

const val TODAY_ITEMS_PREVIEW_COUNT = 3

class HomeViewModel : ViewModel() {
    // Определить текущий контекст по рабочим часам (локальное время устройства)
    // Если work workspace не существует — всегда PERSONAL
    private fun resolveContext(workHoursStart: String?, workHoursEnd: String?): WorkContext
    
    // Агрегировать задачи: dueAt = today OR (dueAt < today AND !isDone)
    // Сортировка: overdue → high priority → medium → low
    // В state: первые 3, всего N
    fun loadTodayItems(context: WorkContext)
    
    // Ручное переключение — сбрасывается при следующем автопереключении
    fun toggleContext()
    
    // Вызывать при каждом tick часов (WorkManager / Timer) для авто-сброса overridden
    fun onTimeCheck()
}
```

**Изменения в HomeScreen.kt:**

1. `ContextPill` — скрыт если `!hasWorkContext`
2. `TodaySection` — первые 3 задачи + кнопка «Ещё N →» если `todayItemsTotal > 3`
3. `OverdueSection` — просроченные (если есть), с красным бейджем
4. `GoalsPlaceholder` — заглушка виджета целей (MVP)
5. `QuickActionsFab` — раскрывающийся FAB с 3 действиями

---

## 7. Экран создания задачи — динамические поля

### Новый файл: `screens/todo/CreateTaskScreen.kt`

```kotlin
// Параметры
data class CreateTaskArgs(
    val listId: String,
    val workspaceId: String,
    val listType: String,       // определяет показываемые секции
    val workspaceType: String,  // family/mentoring → показывать reward
    val templateId: String? = null,  // pre-fill из шаблона
)

// ViewModel
class CreateTaskViewModel : ViewModel() {
    val uiState: StateFlow<CreateTaskUiState>
    
    fun onTemplateSelected(template: TodoItem)  // pre-fill формы
    fun onSave(): Unit  // POST /lists/{id}/items
    fun onUpdate(itemId: String): Unit  // PATCH /items/{id}
}

data class CreateTaskUiState(
    val title: String = "",
    val note: String = "",
    val priority: Priority? = null,
    val dueAt: String? = null,
    val assignedTo: String? = null,
    
    // Shopping fields (только если listType = shopping)
    val shoppingDetails: ShoppingItemFields? = null,
    
    // Chore fields (только если listType = home_chores)
    // Режим расписания — radio, только один активен
    val choreMode: ChoreMode = ChoreMode.ONE_TIME,
    val intervalDays: Int? = null,       // активно при ChoreMode.INTERVAL
    val daysOfWeek: List<String> = emptyList(), // активно при ChoreMode.DAYS_OF_WEEK
    val startDate: String? = null,
    val endDate: String? = null,
    
    // Reward (только если workspaceType = family/mentoring)
    val reward: String? = null,
    
    // Media fields (только если listType = media)
    val mediaFields: MediaItemFields? = null,
    
    // Visible sections
    val showChoreSection: Boolean,
    val showShoppingDetailsSection: Boolean,
    val showRewardSection: Boolean,
    val showMediaSection: Boolean,
)
```

**ChoreMode enum:**
```kotlin
enum class ChoreMode { ONE_TIME, INTERVAL, DAYS_OF_WEEK }
// При сохранении: ONE_TIME → не заполнять intervalDays/daysOfWeek
//                 INTERVAL → заполнить intervalDays, daysOfWeek = null
//                 DAYS_OF_WEEK → заполнить daysOfWeek, intervalDays = null
```

**Логика видимости секций:**
```kotlin
val showChoreSection = listType == "home_chores"
val showShoppingDetailsSection = listType == "shopping"
val showRewardSection = workspaceType in listOf("family", "mentoring")
val showMediaSection = listType == "media"
```

**Шаблоны inline:**
- `TemplatePickerBottomSheet` — показывает **только шаблоны совместимого типа** (`listType`)
- После выбора: `viewModel.onTemplateSelected(template)` → pre-fill всех полей формы (title, note, priority, reward, shopping/chore/media поля)
- Источники данных (Templates v2 backend, см. `docs/GAP_ANALYSIS.md` G-03):
  - **Шаблоны** = `GET /v1/task-templates/public?scope=<listType>` ∪ `GET /v1/task-templates/mine?scope=<listType>`
  - **Часто добавляете** = `GET /v1/suggestions/frequent-items?listId=<id>`
  - **Избранное** = `GET /v1/suggestions/favorites`
- Старые `SuggestionsRepository.choreTemplates` / `allTemplates` (на legacy-эндпоинтах `/suggestions/chore-templates` и `/templates`) — **deprecated**, мигрировать на новые методы.

---

## 8. AssigneeChip — компонент быстрого назначения

Файл: `ui/components/AssigneeChip.kt` (новый или обновление `Chips.kt`)

```kotlin
enum class AssigneeState { UNASSIGNED, ASSIGNED_ME, ASSIGNED_OTHER }

@Composable
fun AssigneeChip(
    state: AssigneeState,
    displayName: String?,
    avatarUrl: String?,
    onClaimSelf: () -> Unit,
    onOpenPicker: () -> Unit,
)
```

**Логика:**
- `UNASSIGNED` + тап → `onClaimSelf()` → немедленно `PATCH /items/{id}` с `assignedTo = currentUserId` + показать Snackbar «Назначено вам · Отменить» (3 сек). При тапе «Отменить» → `PATCH /items/{id}` с `assignedTo = null`
- `ASSIGNED_ME` + тап → `onOpenPicker()` → bottom sheet (снять/переназначить)
- `ASSIGNED_OTHER` + тап → `onOpenPicker()` → bottom sheet

**Где использовать:**
- `TodoListDetailScreen` — строка каждой задачи в групповых/семейных списках
- `CreateTaskScreen` — поле «Исполнитель»
- `HomeScreen` — `TaskRowCompact` на главном экране

---

## 9. Home Space — Комнаты

### Новый экран: `screens/home/HomeRoomsScreen.kt`

```kotlin
class HomeRoomsViewModel(
    private val listsRepository: ListsRepository,
    private val workspaceId: String,
) : ViewModel() {
    
    val rooms: StateFlow<List<RoomTab>>
    val activeRoom: StateFlow<String>
    val activeFilters: StateFlow<RoomFilters>
    val filteredLists: StateFlow<List<TodoList>>
    
    fun selectRoom(roomType: String)
    fun applyFilter(filter: RoomFilters)
    fun clearFilters()
    fun addRoom(name: String, icon: String)
}

data class RoomTab(
    val type: String?,  // null = "Общее" (списки без комнаты), иначе "kitchen" | "bedroom" | etc.
    val name: String,   // "Общее" для null type
    val icon: String,
    val listCount: Int,
)
// Первая вкладка всегда: RoomTab(type = null, name = "Общее", ...)

data class RoomFilters(
    val priorities: Set<Priority> = emptySet(),
    val assigneeId: String? = null,
    val status: ItemStatus? = null,  // ACTIVE | DONE | OVERDUE
)
```

**В существующем `TodoListsScreen.kt`** для home/personal workspace:
- Добавить TabRow по комнатам
- Добавить FilterRow под TabRow
- Фильтрация применяется к спискам текущей вкладки

---

## 10. Shopping List — Детали товара

### Изменения в `TodoListDetailScreen.kt`

1. **ItemDetailsIndicator** — chevron `›` справа в строке, если у элемента есть `brand` или `productUrl`. Тап на строку → открывает bottom sheet (не лонг-пресс — для discoverability)
2. **ItemDetailsBottomSheet** — открывается тапом на строку с деталями:

```kotlin
@Composable
fun ItemDetailsBottomSheet(
    item: TodoItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
)
```

Содержимое: бренд (если есть), ссылка на товар (кликабельная, открывает браузер), quantity, кнопки редактировать/удалить.
> **MVP**: imageUrl не поддерживается. Thumbnail в bottom sheet появится в MMP.

### Изменения в CreateTaskScreen.kt

Секция «Детали товара» (только для shopping, MVP):
```kotlin
// Два поля текстового ввода:
OutlinedTextField(label = "Бренд / марка")   // brand
OutlinedTextField(label = "Ссылка на товар") // productUrl, keyboard = Uri
// ImagePicker — MMP, не реализовывать в MVP
```

---

## 11. Удаление аккаунта

### Изменения в ProfileScreen/SettingsScreen

**Добавить в Settings:**

```kotlin
// DangerZoneSection
@Composable
fun DangerZoneSection(onDeleteAccount: () -> Unit) {
    // Секция с красным заголовком
    // Кнопка "Удалить аккаунт" (outlined, Error color)
}
```

### Новый экран: `screens/settings/DeleteAccountScreen.kt`

```kotlin
class DeleteAccountViewModel(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {
    
    val confirmEmail: StateFlow<String>
    val isOnline: StateFlow<Boolean>               // наблюдать за сетью
    val canDelete: StateFlow<Boolean>              // confirmEmail == userEmail AND isOnline
    val state: StateFlow<DeleteState>
    
    fun onEmailInput(email: String)
    fun deleteAccount()  // DELETE /v1/users/me → очистка БД → навигация на Auth
}

sealed class DeleteState {
    object Idle : DeleteState()
    object Loading : DeleteState()
    object Success : DeleteState()
    data class Error(val message: String) : DeleteState()
}
```

**Кнопка «Удалить навсегда»:** `enabled = canDelete` (т.е. email совпадает И сеть есть). При `!isOnline` — Tooltip «Нет подключения к сети».

**После успешного удаления:**
1. Очистить Room DB (`listsDatabase.clearAllTables()`)
2. Очистить токены (TokenStorage)
3. Навигация на AuthDestination с clear backstack

---

## 12. Настройки рабочих часов

### В ProfileScreen или SettingsScreen

```kotlin
@Composable
fun WorkHoursSettings(
    workHoursStart: String?,
    workHoursEnd: String?,
    workDays: List<String>?,
    onSave: (start: String?, end: String?, days: List<String>?) -> Unit,
)
```

Использует `GroupsRepository.updateWorkHours()`.

---

## 13. Исправления из WORK_PLAN (критично для MVP)

### 13.1. Имена исполнителей вместо UUID (WORK_PLAN 4.11)

Файл: `TodoListDetailViewModel.kt`

```kotlin
// Загружать участников workspace при открытии экрана
// Хранить Map<UUID, String> для резолвинга имён
// Передавать displayName в AssigneeChip
```

### 13.2. Подсветка просроченных задач (WORK_PLAN 4.12)

Файл: `TodoListDetailScreen.kt`

```kotlin
// В строке элемента: если dueAt < now() && !isDone
// → текст dueAt красным цветом
// → возможно, красная точка/метка
```

### 13.3. Статус chores: N дней + цвет (WORK_PLAN 4.13/4.14)

Файл: `TodoListDetailScreen.kt`

```kotlin
fun choreStatusColor(lastDoneAt: String?, intervalDays: Int?): Color {
    // Вычислить дней с последнего выполнения
    // < 80% интервала → Green
    // 80–100% интервала → Amber
    // > 100% интервала → Red (просрочено)
}

fun choreStatusText(lastDoneAt: String?, intervalDays: Int?): String {
    // "Осталось N дней" / "Сегодня" / "Просрочено N дней назад"
}
```

### 13.4. Иконки списков (WORK_PLAN 3.7)

Файл: `TodoListsScreen.kt` + `GroupDetailScreen.kt`

- `TodoList.icon` поле уже есть в модели
- Отображать в карточке списка (emoji или иконка из набора)
- В `CreateListScreen` добавить выбор иконки

### 13.5. Баннер гостю о привязке email (WORK_PLAN 1.9)

Файл: `HomeScreen.kt` или `MainScreen.kt`

```kotlin
// Если currentUser.email == null → показывать Banner
@Composable
fun GuestEmailBanner(onLinkEmail: () -> Unit) {
    // Material3 Banner или Card
    // "Привяжите email, чтобы не потерять данные"
    // [Привязать email] кнопка → навигация на LinkEmailScreen
}
```

---

## 14. Порядок реализации (приоритеты)

### Фаза 1 — Критично для UX (MVP)

1. `WorkspaceType.work` в enum + обновление модели Group
2. Авто-создание work workspace при регистрации (изменение AuthRepository)
3. Имена исполнителей (`AssigneeChip` v1 — только отображение)
4. Подсветка просроченных задач
5. Статус chores (цвет + N дней)
6. Иконки списков в UI
7. Баннер гостю
8. `DeleteAccountScreen` + `ProfileApi.deleteAccount()`

### Фаза 2 — Новая функциональность (MVP)

9. `HomeScreen` переработка (ContextPill + TodaySection)
10. `HomeViewModel` — умный контекст по рабочим часам
11. `CreateTaskScreen` — динамические поля
12. `TemplatePickerBottomSheet` — выбор шаблона inline
13. `AssigneeChip` — quick-assign логика
14. Shopping item details (indicator + bottom sheet)
15. Home rooms (TabRow + фильтры)

### Фаза 3 — MMP

16. `HomeCurrency` модели, API, экран настроек
17. `Leaderboard` + `RewardShop`
18. `GoalsScreen` (после добавления Goals API на бэкенде)
19. Фоновый sync worker + очередь операций
20. Список из меню/рецепта

---

## 15. Нефункциональные требования

- **Анимации:** переключение ContextPill — 200ms slide+fade. Появление динамических секций — 200ms slide down.
- **Offline:** `CreateTaskScreen` сохраняет задачу локально в Room при отсутствии сети (optimistic insert).
- **Изображения:** `AsyncImage` (Coil/Kamel) для shopping item thumbnails. Lazy loading, placeholder.
- **Тема:** все новые экраны — light + dark theme. Использовать только существующие токены из `ui/Color.kt`.
- **Локализация:** все строки через `Res.string.*` в `composeResources/`.
