package com.jetbrains.kmpapp.screens.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetbrains.kmpapp.auth.AuthRepository
import com.jetbrains.kmpapp.auth.AuthState
import com.jetbrains.kmpapp.data.groups.Group
import com.jetbrains.kmpapp.data.groups.GroupsRepository
import com.jetbrains.kmpapp.data.groups.WorkspaceType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class GroupsUiEvent {
    data class NavigateToGroup(val groupId: String, val groupName: String) : GroupsUiEvent()
}

/** Sections shown on the Groups screen. */
data class GroupsListState(
    val pinned: List<Group> = emptyList(),
    val personal: List<Group> = emptyList(),  // active, in "personal" space (non-work non-family types)
    val work: List<Group> = emptyList(),      // active, type = work
    val family: List<Group> = emptyList(),    // active, type = family
    val archived: List<Group> = emptyList(),
)

class GroupsViewModel(
    private val groupsRepository: GroupsRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    val groups: StateFlow<List<Group>> = groupsRepository.groups
    val error: StateFlow<String?> = groupsRepository.error

    // Workspaces shown in the Groups tab — everything except the synthetic "personal" workspace.
    // Family/work/hobby/etc all live here per the new design.
    val groupSpaces: StateFlow<List<Group>> = groupsRepository.groups
        .map { it.filter { g -> g.type != WorkspaceType.PERSONAL } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val isGuest: StateFlow<Boolean> = authRepository.authState
        .map { (it as? AuthState.Authenticated)?.isGuest ?: false }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    /** `null` = show all types. Otherwise one of [WorkspaceType] constants. */
    private val _typeFilter = MutableStateFlow<String?>(null)
    val typeFilter: StateFlow<String?> = _typeFilter.asStateFlow()

    private val _archiveExpanded = MutableStateFlow(false)
    val archiveExpanded: StateFlow<Boolean> = _archiveExpanded.asStateFlow()

    val sections: StateFlow<GroupsListState> = combine(
        groupsRepository.groups,
        _searchQuery,
        _typeFilter,
    ) { all, query, type ->
        // Hide the synthetic "personal" workspace — it represents the current user, not a group.
        val visible = all.filter { it.type != WorkspaceType.PERSONAL }
        val filtered = visible.asSequence()
            .filter { type == null || it.type == type }
            .filter { query.isBlank() || it.title.contains(query, ignoreCase = true) }
            .toList()
        val (archived, active) = filtered.partition { it.archivedAt != null }
        val pinned = active.filter { it.pinnedAt != null }
            .sortedByDescending { it.pinnedAt }
        val nonPinnedActive = active.filter { it.pinnedAt == null }
        GroupsListState(
            pinned = pinned,
            personal = nonPinnedActive.filter { it.type != WorkspaceType.WORK && it.type != WorkspaceType.FAMILY },
            work = nonPinnedActive.filter { it.type == WorkspaceType.WORK },
            family = nonPinnedActive.filter { it.type == WorkspaceType.FAMILY },
            archived = archived.sortedByDescending { it.archivedAt },
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, GroupsListState())

    private val _uiEvent = MutableSharedFlow<GroupsUiEvent>()
    val uiEvent: SharedFlow<GroupsUiEvent> = _uiEvent.asSharedFlow()

    init {
        viewModelScope.launch { groupsRepository.loadGroups() }
    }

    fun refresh() {
        viewModelScope.launch { groupsRepository.loadGroups() }
    }

    fun createGroup(name: String, type: String = "group") {
        viewModelScope.launch {
            groupsRepository.createGroup(title = name, type = type).onSuccess { group ->
                _uiEvent.emit(GroupsUiEvent.NavigateToGroup(group.id, group.title))
            }
        }
    }

    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setTypeFilter(type: String?) { _typeFilter.value = type }
    fun toggleArchiveExpanded() { _archiveExpanded.value = !_archiveExpanded.value }

    fun togglePinned(groupId: String) = groupsRepository.togglePinned(groupId)
    fun toggleMuted(groupId: String) = groupsRepository.toggleMuted(groupId)
    fun archive(groupId: String) = groupsRepository.setArchived(groupId, archived = true)
    fun unarchive(groupId: String) = groupsRepository.setArchived(groupId, archived = false)

    fun clearError() = groupsRepository.clearError()
}
