package com.jetbrains.kmpapp.screens.family

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetbrains.kmpapp.data.categories.CategoriesApi
import com.jetbrains.kmpapp.data.categories.Category
import com.jetbrains.kmpapp.data.categories.CreateCategoryRequest
import com.jetbrains.kmpapp.data.groups.Group
import com.jetbrains.kmpapp.data.groups.GroupMember
import com.jetbrains.kmpapp.data.groups.GroupsRepository
import com.jetbrains.kmpapp.data.lists.ListsRepository
import com.jetbrains.kmpapp.data.lists.TodoItem
import com.jetbrains.kmpapp.data.lists.TodoList
import com.jetbrains.kmpapp.data.sync.SyncRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class FamilyViewModel(
    private val groupsRepository: GroupsRepository,
    private val listsRepository: ListsRepository,
    private val syncRepository: SyncRepository,
    private val categoriesApi: CategoriesApi,
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val familySpace: StateFlow<Group?> = groupsRepository.groups
        .map { groups -> groups.firstOrNull { it.type == "family" } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val balance: StateFlow<Int> = combine(familySpace, syncRepository.memberBalances) { space, balances ->
        space?.id?.let { id -> balances.firstOrNull { it.workspaceId == id }?.balance } ?: 0
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    private val _familyLists = MutableStateFlow<List<TodoList>>(emptyList())
    val familyLists: StateFlow<List<TodoList>> = _familyLists.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isCreating = MutableStateFlow(false)
    val isCreating: StateFlow<Boolean> = _isCreating.asStateFlow()

    // ─── Rooms (G-05) ───────────────────────────────────────────────────────

    /** Список chore-категорий, используемых как «комнаты». Загружается отдельно от shared CategoriesRepository. */
    private val _rooms = MutableStateFlow<List<Category>>(emptyList())
    val rooms: StateFlow<List<RoomUi>> = _rooms
        .map { it.toRoomUi() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** id выбранной комнаты или null = вкладка «Все». */
    private val _selectedRoomId = MutableStateFlow<String?>(null)
    val selectedRoomId: StateFlow<String?> = _selectedRoomId.asStateFlow()

    private val _filters = MutableStateFlow(RoomFilters())
    val filters: StateFlow<RoomFilters> = _filters.asStateFlow()

    private val _isCreatingRoom = MutableStateFlow(false)
    val isCreatingRoom: StateFlow<Boolean> = _isCreatingRoom.asStateFlow()

    val familyMembers: StateFlow<List<GroupMember>> = groupsRepository.members

    /** Items типа home_chores из всех family-списков, отфильтрованные по комнате и фильтрам. */
    val filteredChores: StateFlow<List<TodoItem>> = combine(
        listsRepository.allItems,
        _familyLists,
        _selectedRoomId,
        _filters,
    ) { items, lists, roomId, f ->
        val choreListIds = lists.filter { it.type == "home_chores" }.map { it.id }.toSet()
        if (choreListIds.isEmpty()) return@combine emptyList()
        val now = Clock.System.now()
        items.filter { item ->
            if (item.listId !in choreListIds) return@filter false
            // Room (category) filter
            if (roomId != null && item.choreSchedule?.category != roomId) return@filter false
            // Priority filter (skip if empty = «не фильтровать»)
            if (f.priorities.isNotEmpty()) {
                val key = item.priority ?: PRIORITY_NONE_KEY
                if (key !in f.priorities) return@filter false
            }
            // Assignee filter
            if (f.assignees.isNotEmpty()) {
                val key = item.assignedTo?.takeIf { it.isNotBlank() } ?: ASSIGNEE_UNASSIGNED_KEY
                if (key !in f.assignees) return@filter false
            }
            // Status filter (всегда применяется, default = только Активные)
            val due = item.dueAt?.toInstantOrNull()
            val isOverdue = !item.isDone && due != null && due < now
            val isActive = !item.isDone && !isOverdue
            f.statuses.any { st ->
                when (st) {
                    RoomStatusFilter.ACTIVE -> isActive
                    RoomStatusFilter.OVERDUE -> isOverdue
                    RoomStatusFilter.DONE -> item.isDone
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        viewModelScope.launch {
            groupsRepository.loadGroups()
            _isLoading.value = false
            // After groups are loaded, check if we need to load family lists
            familySpace.value?.let { space ->
                loadFamilyListsForGroup(space.id)
                loadRoomsAndMembers(space.id)
            }
        }
    }

    fun loadFamilyLists() {
        val space = familySpace.value ?: return
        loadFamilyListsForGroup(space.id)
        loadRoomsAndMembers(space.id)
    }

    private fun loadFamilyListsForGroup(workspaceId: String) {
        viewModelScope.launch {
            listsRepository.loadLists(workspaceId = workspaceId)
            _familyLists.value = listsRepository.lists.value.filter {
                it.workspaceId == workspaceId
            }
        }
    }

    private fun loadRoomsAndMembers(workspaceId: String) {
        viewModelScope.launch {
            launch { groupsRepository.loadWorkspaceMembers(workspaceId) }
            launch {
                categoriesApi.getCategories("chore").onSuccess { _rooms.value = it }
            }
        }
    }

    fun selectRoom(id: String?) {
        _selectedRoomId.value = id
    }

    fun togglePriority(value: String) = _filters.update { it.copy(priorities = it.priorities.toggle(value)) }
    fun toggleAssignee(value: String) = _filters.update { it.copy(assignees = it.assignees.toggle(value)) }
    fun toggleStatus(value: RoomStatusFilter) = _filters.update {
        // Не даём убрать последний статус — иначе список бессмысленно пуст.
        val next = it.statuses.toggle(value)
        if (next.isEmpty()) it else it.copy(statuses = next)
    }

    fun resetFilters() {
        _filters.value = RoomFilters()
    }

    /**
     * Создаёт новую комнату (chore-category). Иконка не передаётся на бэкенд
     * (модель Category не поддерживает icon) — клиентское отображение через roomEmojiFor.
     */
    fun createRoom(name: String) {
        if (name.isBlank()) return
        _isCreatingRoom.value = true
        viewModelScope.launch {
            categoriesApi.createCategory(CreateCategoryRequest(scope = "chore", name = name.trim()))
                .onSuccess { newCat ->
                    _rooms.value = _rooms.value + newCat
                    _selectedRoomId.value = newCat.name
                }
                .onFailure { _error.value = it.message }
            _isCreatingRoom.value = false
        }
    }

    fun createFamilySpace(name: String) {
        _isCreating.value = true
        viewModelScope.launch {
            groupsRepository.createGroup(title = name, type = "family")
                .onSuccess { createdGroup ->
                    loadFamilyListsForGroup(createdGroup.id)
                    loadRoomsAndMembers(createdGroup.id)
                }
                .onFailure { _error.value = it.message }
            _isCreating.value = false
        }
    }

    fun clearError() { _error.value = null }
}

private fun <T> Set<T>.toggle(value: T): Set<T> =
    if (value in this) this - value else this + value

private fun String.toInstantOrNull(): Instant? = try {
    Instant.parse(this)
} catch (_: Throwable) {
    null
}
