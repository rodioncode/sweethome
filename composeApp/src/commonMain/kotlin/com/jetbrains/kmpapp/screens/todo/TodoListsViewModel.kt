package com.jetbrains.kmpapp.screens.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetbrains.kmpapp.data.groups.Group
import com.jetbrains.kmpapp.data.groups.GroupsRepository
import com.jetbrains.kmpapp.data.lists.ListsRepository
import com.jetbrains.kmpapp.data.lists.TodoList
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TodoListsViewModel(
    private val listsRepository: ListsRepository,
    private val groupsRepository: GroupsRepository,
) : ViewModel() {

    val lists: StateFlow<List<TodoList>> = listsRepository.lists
    val error: StateFlow<String?> = listsRepository.error
    val groups: StateFlow<List<Group>> = groupsRepository.groups

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

    fun clearError() {
        listsRepository.clearError()
    }
}
