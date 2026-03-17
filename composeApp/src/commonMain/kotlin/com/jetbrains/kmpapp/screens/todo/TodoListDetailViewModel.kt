package com.jetbrains.kmpapp.screens.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetbrains.kmpapp.data.lists.CreateItemRequest
import com.jetbrains.kmpapp.data.lists.ListsRepository
import com.jetbrains.kmpapp.data.lists.TodoItem
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TodoListDetailViewModel(
    private val listsRepository: ListsRepository,
) : ViewModel() {

    val listWithItems: StateFlow<Pair<com.jetbrains.kmpapp.data.lists.TodoList, List<TodoItem>>?> =
        listsRepository.currentListWithItems
    val error: StateFlow<String?> = listsRepository.error

    fun loadList(listId: String) {
        viewModelScope.launch {
            listsRepository.loadListWithItems(listId)
        }
    }

    fun addItem(listId: String, title: String) {
        viewModelScope.launch {
            listsRepository.createItem(listId, title)
        }
    }

    fun toggleItem(item: TodoItem) {
        viewModelScope.launch {
            listsRepository.toggleItemDone(item)
        }
    }

    fun deleteItem(item: TodoItem) {
        viewModelScope.launch {
            listsRepository.deleteItem(item)
        }
    }

    fun clearError() {
        listsRepository.clearError()
    }

    override fun onCleared() {
        super.onCleared()
        listsRepository.clearCurrentList()
    }
}
