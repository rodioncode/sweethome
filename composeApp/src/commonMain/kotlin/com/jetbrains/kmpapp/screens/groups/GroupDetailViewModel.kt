package com.jetbrains.kmpapp.screens.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetbrains.kmpapp.auth.AuthRepository
import com.jetbrains.kmpapp.auth.AuthState
import com.jetbrains.kmpapp.data.groups.EmailRequiredException
import com.jetbrains.kmpapp.data.groups.Group
import com.jetbrains.kmpapp.data.groups.GroupMember
import com.jetbrains.kmpapp.data.groups.GroupsRepository
import com.jetbrains.kmpapp.data.groups.Invite
import com.jetbrains.kmpapp.data.groups.OwnerCannotLeaveException
import com.jetbrains.kmpapp.data.lists.ListsRepository
import com.jetbrains.kmpapp.data.lists.TodoList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class GroupDetailUiEvent {
    data class ShowInvite(val invite: Invite) : GroupDetailUiEvent()
    data object NavigateToLinkEmail : GroupDetailUiEvent()
    data object GroupDeleted : GroupDetailUiEvent()
    data class ShowError(val message: String) : GroupDetailUiEvent()
}

class GroupDetailViewModel(
    private val groupsRepository: GroupsRepository,
    private val listsRepository: ListsRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _groupId = MutableStateFlow<String?>(null)

    val group: StateFlow<Group?> = combine(_groupId, groupsRepository.groups) { id, groups ->
        groups.find { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val members: StateFlow<List<GroupMember>> = groupsRepository.members

    val groupLists: StateFlow<List<TodoList>> = listsRepository.lists

    private val _uiEvent = MutableSharedFlow<GroupDetailUiEvent>()
    val uiEvent: SharedFlow<GroupDetailUiEvent> = _uiEvent.asSharedFlow()

    val error: StateFlow<String?> = groupsRepository.error

    fun load(groupId: String) {
        _groupId.value = groupId
        viewModelScope.launch {
            groupsRepository.loadGroups()
            groupsRepository.loadWorkspaceMembers(groupId)
            listsRepository.loadLists(workspaceId = groupId)
        }
    }

    fun createInvite() {
        val groupId = _groupId.value ?: return
        viewModelScope.launch {
            groupsRepository.createInvite(groupId)
                .onSuccess { invite -> _uiEvent.emit(GroupDetailUiEvent.ShowInvite(invite)) }
                .onFailure { _uiEvent.emit(GroupDetailUiEvent.ShowError(it.message ?: "Ошибка")) }
        }
    }

    fun deleteGroup() {
        val groupId = _groupId.value ?: return
        viewModelScope.launch {
            groupsRepository.deleteGroup(groupId)
                .onSuccess { _uiEvent.emit(GroupDetailUiEvent.GroupDeleted) }
                .onFailure { _uiEvent.emit(GroupDetailUiEvent.ShowError(it.message ?: "Ошибка")) }
        }
    }

    fun removeMember(userId: String) {
        val groupId = _groupId.value ?: return
        val currentUserId = (authRepository.authState.value as? AuthState.Authenticated)?.userId
        viewModelScope.launch {
            groupsRepository.removeMember(groupId, userId, currentUserId)
                .onFailure { err ->
                    when (err) {
                        is OwnerCannotLeaveException ->
                            _uiEvent.emit(GroupDetailUiEvent.ShowError("Сначала передайте роль владельца"))
                        is EmailRequiredException ->
                            _uiEvent.emit(GroupDetailUiEvent.NavigateToLinkEmail)
                        else ->
                            _uiEvent.emit(GroupDetailUiEvent.ShowError(err.message ?: "Ошибка"))
                    }
                }
        }
    }

    fun leaveGroup() {
        val groupId = _groupId.value ?: return
        val userId = (authRepository.authState.value as? AuthState.Authenticated)?.userId ?: return
        viewModelScope.launch {
            groupsRepository.removeMember(groupId, userId, userId)
                .onFailure { err ->
                    when (err) {
                        is OwnerCannotLeaveException ->
                            _uiEvent.emit(GroupDetailUiEvent.ShowError("Сначала передайте роль владельца"))
                        else ->
                            _uiEvent.emit(GroupDetailUiEvent.ShowError(err.message ?: "Ошибка"))
                    }
                }
        }
    }

    fun transferOwnership(userId: String) {
        val groupId = _groupId.value ?: return
        viewModelScope.launch {
            groupsRepository.transferOwnership(groupId, userId)
                .onFailure { _uiEvent.emit(GroupDetailUiEvent.ShowError(it.message ?: "Ошибка")) }
        }
    }

    fun createListInGroup(title: String) {
        val groupId = _groupId.value ?: return
        viewModelScope.launch {
            listsRepository.createList(
                type = "general_todos",
                title = title,
                workspaceId = groupId,
            )
        }
    }

    fun clearError() = groupsRepository.clearError()
}
