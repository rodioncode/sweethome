package com.jetbrains.kmpapp.screens.family

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetbrains.kmpapp.data.groups.Group
import com.jetbrains.kmpapp.data.groups.GroupsRepository
import com.jetbrains.kmpapp.data.lists.ListsRepository
import com.jetbrains.kmpapp.data.lists.TodoList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FamilyViewModel(
    private val groupsRepository: GroupsRepository,
    private val listsRepository: ListsRepository,
) : ViewModel() {

    val familySpace: StateFlow<Group?> = groupsRepository.groups
        .map { groups -> groups.firstOrNull { it.type == "family" } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val familyLists: StateFlow<List<TodoList>> = listsRepository.lists

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isCreating = MutableStateFlow(false)
    val isCreating: StateFlow<Boolean> = _isCreating.asStateFlow()

    init {
        viewModelScope.launch {
            groupsRepository.loadGroups()
        }
    }

    fun loadFamilyLists() {
        val space = familySpace.value ?: return
        viewModelScope.launch {
            listsRepository.loadLists(scope = "group", groupId = space.id)
        }
    }

    fun createFamilySpace(name: String) {
        _isCreating.value = true
        viewModelScope.launch {
            groupsRepository.createGroup(name, type = "family")
                .onSuccess { loadFamilyLists() }
                .onFailure { _error.value = it.message }
            _isCreating.value = false
        }
    }

    fun clearError() { _error.value = null }
}
