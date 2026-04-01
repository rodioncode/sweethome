package com.jetbrains.kmpapp.screens.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetbrains.kmpapp.data.lists.ChoreSchedule
import com.jetbrains.kmpapp.data.lists.ListsRepository
import com.jetbrains.kmpapp.data.lists.ShoppingItemFields
import com.jetbrains.kmpapp.data.lists.TodoItem
import com.jetbrains.kmpapp.data.lists.TodoList
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TodoListDetailViewModel(
    private val listsRepository: ListsRepository,
) : ViewModel() {

    val listWithItems: StateFlow<Pair<TodoList, List<TodoItem>>?> =
        listsRepository.currentListWithItems
    val error: StateFlow<String?> = listsRepository.error

    fun loadList(listId: String) {
        viewModelScope.launch {
            listsRepository.loadListWithItems(listId)
        }
    }

    fun addItem(
        listId: String,
        title: String,
        note: String? = null,
        dueAt: String? = null,
        isFavorite: Boolean = false,
        shopping: ShoppingItemFields? = null,
        choreSchedule: ChoreSchedule? = null,
    ) {
        viewModelScope.launch {
            listsRepository.createItem(
                listId = listId,
                title = title,
                note = note?.takeIf { it.isNotBlank() },
                dueAt = dueAt?.takeIf { it.isNotBlank() },
                isFavorite = isFavorite.takeIf { it },
                shopping = shopping,
                choreSchedule = choreSchedule,
            )
        }
    }

    fun updateItem(
        item: TodoItem,
        title: String,
        note: String,
        dueAt: String,
        isFavorite: Boolean,
        shopping: ShoppingItemFields? = null,
        choreSchedule: ChoreSchedule? = null,
    ) {
        viewModelScope.launch {
            listsRepository.updateItem(
                itemId = item.id,
                title = title.takeIf { it.isNotBlank() && it != item.title },
                note = note.takeIf { it.isNotBlank() },
                dueAt = dueAt.takeIf { it.isNotBlank() },
                isFavorite = isFavorite.takeIf { it != item.isFavorite },
                shopping = shopping,
                choreSchedule = choreSchedule,
            )
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
