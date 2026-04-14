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

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val familySpace: StateFlow<Group?> = groupsRepository.groups
        .map { groups -> groups.firstOrNull { it.type == "family" } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _familyLists = MutableStateFlow<List<TodoList>>(emptyList())
    val familyLists: StateFlow<List<TodoList>> = _familyLists.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isCreating = MutableStateFlow(false)
    val isCreating: StateFlow<Boolean> = _isCreating.asStateFlow()

    init {
        viewModelScope.launch {
            groupsRepository.loadGroups()
            _isLoading.value = false
            // After groups are loaded, check if we need to load family lists
            familySpace.value?.let { loadFamilyListsForGroup(it.id) }
        }
    }

    fun loadFamilyLists() {
        val space = familySpace.value ?: return
        loadFamilyListsForGroup(space.id)
    }

    private fun loadFamilyListsForGroup(groupId: String) {
        viewModelScope.launch {
            listsRepository.loadLists(scope = "group", groupId = groupId)
            _familyLists.value = listsRepository.lists.value.filter {
                it.scope == "group" && it.ownerGroupId == groupId
            }
        }
    }

    fun createFamilySpace(name: String) {
        _isCreating.value = true
        viewModelScope.launch {
            groupsRepository.createGroup(name, type = "family")
                .onSuccess { createdGroup ->
                    loadFamilyListsForGroup(createdGroup.id)
                }
                .onFailure { _error.value = it.message }
            _isCreating.value = false
        }
    }

    fun clearError() { _error.value = null }
}
