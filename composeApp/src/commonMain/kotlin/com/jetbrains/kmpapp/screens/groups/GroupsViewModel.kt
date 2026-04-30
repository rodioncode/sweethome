package com.jetbrains.kmpapp.screens.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetbrains.kmpapp.auth.AuthRepository
import com.jetbrains.kmpapp.auth.AuthState
import com.jetbrains.kmpapp.data.groups.Group
import com.jetbrains.kmpapp.data.groups.GroupsRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class GroupsUiEvent {
    data class NavigateToGroup(val groupId: String, val groupName: String) : GroupsUiEvent()
}

class GroupsViewModel(
    private val groupsRepository: GroupsRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    val groups: StateFlow<List<Group>> = groupsRepository.groups
    val error: StateFlow<String?> = groupsRepository.error

    // Non-personal workspaces (group, family, mentoring)
    val groupSpaces: StateFlow<List<Group>> = groupsRepository.groups
        .map { it.filter { g -> g.type != "family" && g.type != "personal" } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val isGuest: StateFlow<Boolean> = authRepository.authState
        .map { (it as? AuthState.Authenticated)?.isGuest ?: false }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

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

    fun clearError() = groupsRepository.clearError()
}
