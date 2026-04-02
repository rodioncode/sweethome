package com.jetbrains.kmpapp.screens.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetbrains.kmpapp.data.groups.GroupDTO
import com.jetbrains.kmpapp.data.groups.GroupsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class GroupsUiState {
    data object Idle : GroupsUiState()
    data object Loading : GroupsUiState()
    data class Error(val message: String) : GroupsUiState()
    data object Success : GroupsUiState()
}

class GroupsViewModel(
    private val groupsRepository: GroupsRepository,
) : ViewModel() {

    val groups: StateFlow<List<GroupDTO>> = groupsRepository.groups
    val error: StateFlow<String?> = groupsRepository.error

    private val _uiState = MutableStateFlow<GroupsUiState>(GroupsUiState.Idle)
    val uiState: StateFlow<GroupsUiState> = _uiState.asStateFlow()

    init {
        loadGroups()
    }

    fun loadGroups() {
        viewModelScope.launch {
            _uiState.value = GroupsUiState.Loading
            groupsRepository.loadGroups()
            _uiState.value = GroupsUiState.Idle
        }
    }

    fun createGroup(name: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = GroupsUiState.Loading
            groupsRepository.createGroup(name)
                .onSuccess {
                    _uiState.value = GroupsUiState.Success
                    onSuccess()
                }
                .onFailure { _uiState.value = GroupsUiState.Error(it.message ?: "Ошибка создания группы") }
        }
    }

    fun acceptInvite(token: String, onSuccess: (groupId: String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = GroupsUiState.Loading
            groupsRepository.acceptInvite(token)
                .onSuccess {
                    _uiState.value = GroupsUiState.Idle
                    onSuccess(it.groupId)
                }
                .onFailure { _uiState.value = GroupsUiState.Error(it.message ?: "Не удалось принять приглашение") }
        }
    }

    fun clearError() {
        groupsRepository.clearError()
        _uiState.value = GroupsUiState.Idle
    }
}
