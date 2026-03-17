package com.jetbrains.kmpapp.data.lists

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ListsRepository(private val listsApi: ListsApi) {

    private val _lists = MutableStateFlow<List<TodoList>>(emptyList())
    val lists: StateFlow<List<TodoList>> = _lists.asStateFlow()

    private val _currentListWithItems = MutableStateFlow<Pair<TodoList, List<TodoItem>>?>(null)
    val currentListWithItems: StateFlow<Pair<TodoList, List<TodoItem>>?> = _currentListWithItems.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    suspend fun loadLists(scope: String? = null, groupId: String? = null) {
        listsApi.getLists(scope, groupId)
            .onSuccess { _lists.value = it }
            .onFailure { _error.value = it.message }
    }

    suspend fun createList(type: String, title: String, scope: String = "personal"): Result<TodoList> {
        val result = listsApi.createList(CreateListRequest(type = type, title = title, scope = scope))
        result.onSuccess { _lists.value = _lists.value + it }
        result.onFailure { _error.value = it.message }
        return result
    }

    suspend fun loadListWithItems(listId: String) {
        listsApi.getListWithItems(listId)
            .onSuccess { _currentListWithItems.value = it }
            .onFailure { _error.value = it.message }
    }

    fun clearCurrentList() {
        _currentListWithItems.value = null
    }

    suspend fun updateList(listId: String, title: String? = null): Result<TodoList> {
        val result = listsApi.updateList(listId, UpdateListRequest(title = title))
        result.onSuccess { updated ->
            _lists.value = _lists.value.map { if (it.id == listId) updated else it }
            _currentListWithItems.value?.let { (list, items) ->
                if (list.id == listId) _currentListWithItems.value = updated to items
            }
        }
        result.onFailure { _error.value = it.message }
        return result
    }

    suspend fun deleteList(listId: String): Result<Unit> {
        val result = listsApi.deleteList(listId)
        result.onSuccess {
            _lists.value = _lists.value.filter { it.id != listId }
            if (_currentListWithItems.value?.first?.id == listId) {
                _currentListWithItems.value = null
            }
        }
        result.onFailure { _error.value = it.message }
        return result
    }

    suspend fun createItem(listId: String, title: String): Result<TodoItem> {
        val result = listsApi.createItem(listId, CreateItemRequest(title = title))
        result.onSuccess { newItem ->
            _currentListWithItems.value?.let { (list, items) ->
                if (list.id == listId) _currentListWithItems.value = list to (items + newItem)
            }
        }
        result.onFailure { _error.value = it.message }
        return result
    }

    suspend fun toggleItemDone(item: TodoItem): Result<TodoItem> {
        val result = listsApi.updateItem(item.id, UpdateItemRequest(isDone = !item.isDone))
        result.onSuccess { updated ->
            _currentListWithItems.value?.let { (list, items) ->
                if (list.id == item.listId) {
                    _currentListWithItems.value = list to items.map { if (it.id == item.id) updated else it }
                }
            }
        }
        result.onFailure { _error.value = it.message }
        return result
    }

    suspend fun updateItem(itemId: String, title: String? = null, note: String? = null): Result<TodoItem> {
        val result = listsApi.updateItem(itemId, UpdateItemRequest(title = title, note = note))
        result.onSuccess { updated ->
            _currentListWithItems.value?.let { (list, items) ->
                _currentListWithItems.value = list to items.map { if (it.id == itemId) updated else it }
            }
        }
        result.onFailure { _error.value = it.message }
        return result
    }

    suspend fun deleteItem(item: TodoItem): Result<Unit> {
        val result = listsApi.deleteItem(item.id)
        result.onSuccess {
            _currentListWithItems.value?.let { (list, items) ->
                if (list.id == item.listId) {
                    _currentListWithItems.value = list to items.filter { it.id != item.id }
                }
            }
        }
        result.onFailure { _error.value = it.message }
        return result
    }

    fun clearError() {
        _error.value = null
    }
}
