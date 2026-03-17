package com.jetbrains.kmpapp.screens.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetbrains.kmpapp.data.lists.ListsRepository
import com.jetbrains.kmpapp.data.lists.TodoList
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TodoListsViewModel(
    private val listsRepository: ListsRepository,
) : ViewModel() {

    val lists: StateFlow<List<TodoList>> = listsRepository.lists
    val error: StateFlow<String?> = listsRepository.error

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

    fun createList(title: String, type: String = "general_todos") {
        viewModelScope.launch {
            listsRepository.createList(type = type, title = title)
        }
    }

    fun clearError() {
        listsRepository.clearError()
    }
}
