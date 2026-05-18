package com.jetbrains.kmpapp.screens.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetbrains.kmpapp.data.groups.Group
import com.jetbrains.kmpapp.data.groups.GroupsRepository
import com.jetbrains.kmpapp.data.groups.WorkspaceType
import com.jetbrains.kmpapp.data.lists.ListsRepository
import com.jetbrains.kmpapp.data.lists.TodoList
import com.jetbrains.kmpapp.data.preferences.LocalPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TodoListsViewModel(
    private val listsRepository: ListsRepository,
    private val groupsRepository: GroupsRepository,
    private val localPreferences: LocalPreferences,
) : ViewModel() {

    val lists: StateFlow<List<TodoList>> = listsRepository.lists
    val error: StateFlow<String?> = listsRepository.error
    val groups: StateFlow<List<Group>> = groupsRepository.groups

    private val _selectedWorkspaceId = MutableStateFlow(localPreferences.getSelectedWorkspaceId())
    val selectedWorkspaceId: StateFlow<String?> = _selectedWorkspaceId.asStateFlow()

    /** `null` = «Все типы»; otherwise the canonical list type string. */
    private val _typeFilter = MutableStateFlow<String?>(null)
    val typeFilter: StateFlow<String?> = _typeFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    /**
     * Workspace currently shown to the user. Resolves to the saved preference if it still exists,
     * otherwise falls back to the first available workspace.
     */
    val activeWorkspace: StateFlow<Group?> = combine(
        groupsRepository.groups,
        _selectedWorkspaceId,
    ) { allGroups, savedId ->
        val visible = allGroups.filter { it.archivedAt == null }
        visible.firstOrNull { it.id == savedId }
            ?: visible.firstOrNull { it.type == WorkspaceType.PERSONAL }
            ?: visible.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /**
     * Lists for the active workspace, type-filtered and search-filtered, pinned-first.
     */
    val visibleLists: StateFlow<List<TodoList>> = combine(
        listsRepository.lists,
        activeWorkspace,
        _typeFilter,
        _searchQuery,
    ) { all, ws, type, query ->
        all.asSequence()
            .filter { it.archivedAt == null }
            .filter { ws == null || it.workspaceId == ws.id }
            .filter { type == null || it.type == type }
            .filter { query.isBlank() || it.title.contains(query, ignoreCase = true) }
            .sortedWith(
                compareByDescending<TodoList> { it.pinnedAt != null }
                    .thenByDescending { it.pinnedAt ?: "" }
                    .thenBy { it.title }
            )
            .toList()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        viewModelScope.launch {
            listsRepository.loadLists()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            listsRepository.loadLists()
        }
    }

    fun createList(
        title: String,
        type: String = "general_todos",
        workspaceId: String,
        icon: String? = null,
        color: String? = null,
    ) {
        viewModelScope.launch {
            listsRepository.createList(
                type = type,
                title = title,
                workspaceId = workspaceId,
                icon = icon,
                color = color,
            )
        }
    }

    fun selectWorkspace(workspaceId: String) {
        _selectedWorkspaceId.value = workspaceId
        localPreferences.setSelectedWorkspaceId(workspaceId)
    }

    fun setTypeFilter(type: String?) { _typeFilter.value = type }
    fun setSearchQuery(query: String) { _searchQuery.value = query }

    fun togglePinned(listId: String) = listsRepository.togglePinned(listId)

    fun clearError() {
        listsRepository.clearError()
    }
}
