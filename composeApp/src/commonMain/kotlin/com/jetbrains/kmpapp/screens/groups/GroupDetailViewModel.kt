package com.jetbrains.kmpapp.screens.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetbrains.kmpapp.data.groups.GroupDTO
import com.jetbrains.kmpapp.data.groups.GroupsRepository
import com.jetbrains.kmpapp.data.groups.InviteDTO
import com.jetbrains.kmpapp.data.lists.ListsRepository
import com.jetbrains.kmpapp.data.lists.TodoList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class GroupDetailUiState {
    data object Idle : GroupDetailUiState()
    data object Loading : GroupDetailUiState()
    data class Error(val message: String) : GroupDetailUiState()
    data class InviteCreated(val invite: InviteDTO) : GroupDetailUiState()
}

class GroupDetailViewModel(
    private val groupId: String,
    private val groupsRepository: GroupsRepository,
    private val listsRepository: ListsRepository,
) : ViewModel() {

    private val _group = MutableStateFlow<GroupDTO?>(null)
    val group: StateFlow<GroupDTO?> = _group.asStateFlow()

    private val _groupLists = MutableStateFlow<List<TodoList>>(emptyList())
    val groupLists: StateFlow<List<TodoList>> = _groupLists.asStateFlow()

    private val _uiState = MutableStateFlow<GroupDetailUiState>(GroupDetailUiState.Idle)
    val uiState: StateFlow<GroupDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = GroupDetailUiState.Loading
            // Синхронизируем группу из репозитория
            groupsRepository.loadGroups()
            _group.value = groupsRepository.groups.value.find { it.id == groupId }
            // Загружаем списки группы
            listsRepository.loadLists(scope = "group", groupId = groupId)
            _groupLists.value = listsRepository.lists.value
            _uiState.value = GroupDetailUiState.Idle
        }
    }

    fun createInvite() {
        viewModelScope.launch {
            _uiState.value = GroupDetailUiState.Loading
            groupsRepository.createInvite(groupId)
                .onSuccess { _uiState.value = GroupDetailUiState.InviteCreated(it) }
                .onFailure { _uiState.value = GroupDetailUiState.Error(it.message ?: "Ошибка создания инвайта") }
        }
    }

    fun transferOwnership(newOwnerId: String) {
        viewModelScope.launch {
            _uiState.value = GroupDetailUiState.Loading
            groupsRepository.transferOwnership(groupId, newOwnerId)
                .onSuccess {
                    load()
                    _uiState.value = GroupDetailUiState.Idle
                }
                .onFailure { _uiState.value = GroupDetailUiState.Error(it.message ?: "Ошибка передачи роли") }
        }
    }

    fun deleteGroup(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = GroupDetailUiState.Loading
            groupsRepository.deleteGroup(groupId)
                .onSuccess { onSuccess() }
                .onFailure { _uiState.value = GroupDetailUiState.Error(it.message ?: "Ошибка удаления группы") }
        }
    }

    fun removeMember(userId: String, onSelfLeave: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = GroupDetailUiState.Loading
            groupsRepository.removeMember(groupId, userId)
                .onSuccess {
                    val currentGroup = _group.value
                    // Если удалили себя — уходим с экрана
                    if (currentGroup != null && userId == currentGroup.createdBy) {
                        onSelfLeave()
                    } else {
                        load()
                    }
                }
                .onFailure { _uiState.value = GroupDetailUiState.Error(it.message ?: "Ошибка удаления участника") }
        }
    }

    fun dismissState() {
        _uiState.value = GroupDetailUiState.Idle
    }
}
