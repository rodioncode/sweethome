package com.jetbrains.kmpapp.data.lists

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ListsRepository(
    private val listsApi: ListsApi,
    private val listsStorage: ListsStorage,
) {

    private val _lists = MutableStateFlow<List<TodoList>>(emptyList())
    val lists: StateFlow<List<TodoList>> = _lists.asStateFlow()

    private val _currentListWithItems = MutableStateFlow<Pair<TodoList, List<TodoItem>>?>(null)
    val currentListWithItems: StateFlow<Pair<TodoList, List<TodoItem>>?> = _currentListWithItems.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    suspend fun loadLists(scope: String? = null, groupId: String? = null) {
        _lists.value = listsStorage.getLists()
        listsApi.getLists(scope, groupId)
            .onSuccess {
                _lists.value = it
                listsStorage.saveLists(it)
            }
            .onFailure { _error.value = it.message }
    }

    suspend fun createList(
        type: String,
        title: String,
        icon: String? = null,
        color: String? = null,
        scope: String = "personal",
        groupId: String? = null,
    ): Result<TodoList> {
        val result = listsApi.createList(CreateListRequest(type = type, title = title, icon = icon, color = color, scope = scope, groupId = groupId))
        result.onSuccess {
            _lists.value = _lists.value + it
            listsStorage.saveLists(_lists.value)
        }
        result.onFailure { _error.value = it.message }
        return result
    }

    suspend fun loadListWithItems(listId: String) {
        _currentListWithItems.value = listsStorage.getListWithItems(listId)
        listsApi.getListWithItems(listId)
            .onSuccess {
                _currentListWithItems.value = it
                listsStorage.saveListWithItems(it.first, it.second)
            }
            .onFailure { _error.value = it.message }
    }

    fun clearCurrentList() {
        _currentListWithItems.value = null
    }

    suspend fun reloadListsFromStorage() {
        _lists.value = listsStorage.getLists()
    }

    suspend fun reloadCurrentListFromStorage(listId: String) {
        listsStorage.getListWithItems(listId)?.let { _currentListWithItems.value = it }
    }

    suspend fun updateList(listId: String, title: String? = null): Result<TodoList> {
        val result = listsApi.updateList(listId, UpdateListRequest(title = title))
        result.onSuccess { updated ->
            _lists.value = _lists.value.map { if (it.id == listId) updated else it }
            _currentListWithItems.value?.let { (list, items) ->
                if (list.id == listId) _currentListWithItems.value = updated to items
            }
            listsStorage.saveLists(_lists.value)
            _currentListWithItems.value?.let { (list, items) ->
                listsStorage.saveListWithItems(list, items)
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
            listsStorage.saveLists(_lists.value)
        }
        result.onFailure { _error.value = it.message }
        return result
    }

    suspend fun createItem(
        listId: String,
        title: String,
        note: String? = null,
        assignedTo: String? = null,
        dueAt: String? = null,
        isFavorite: Boolean? = null,
        shopping: ShoppingItemFields? = null,
        choreSchedule: ChoreSchedule? = null,
    ): Result<TodoItem> {
        val result = listsApi.createItem(
            listId,
            CreateItemRequest(
                title = title,
                note = note,
                assignedTo = assignedTo,
                dueAt = dueAt,
                isFavorite = isFavorite,
                shopping = shopping,
                choreSchedule = choreSchedule,
            )
        )
        result.onSuccess { newItem ->
            _currentListWithItems.value?.let { (list, items) ->
                if (list.id == listId) {
                    val updated = list to (items + newItem)
                    _currentListWithItems.value = updated
                    listsStorage.saveListWithItems(updated.first, updated.second)
                }
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
                    val newItems = items.map { if (it.id == item.id) updated else it }
                    _currentListWithItems.value = list to newItems
                    listsStorage.saveListWithItems(list, newItems)
                }
            }
        }
        result.onFailure { _error.value = it.message }
        return result
    }

    suspend fun updateItem(
        itemId: String,
        title: String? = null,
        note: String? = null,
        assignedTo: String? = null,  // null = не менять; "" = сбросить
        dueAt: String? = null,        // null = не менять; "" = сбросить
        isFavorite: Boolean? = null,
        shopping: ShoppingItemFields? = null,
        choreSchedule: ChoreSchedule? = null,
    ): Result<TodoItem> {
        val result = listsApi.updateItem(
            itemId,
            UpdateItemRequest(
                title = title,
                note = note,
                assignedTo = assignedTo,
                dueAt = dueAt,
                isFavorite = isFavorite,
                shopping = shopping,
                choreSchedule = choreSchedule,
            )
        )
        result.onSuccess { updated ->
            _currentListWithItems.value?.let { (list, items) ->
                val newItems = items.map { if (it.id == itemId) updated else it }
                _currentListWithItems.value = list to newItems
                listsStorage.saveListWithItems(list, newItems)
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
                    val newItems = items.filter { it.id != item.id }
                    _currentListWithItems.value = list to newItems
                    listsStorage.saveListWithItems(list, newItems)
                }
            }
        }
        result.onFailure { _error.value = it.message }
        return result
    }

    fun clearAll() {
        _lists.value = emptyList()
        _currentListWithItems.value = null
        _error.value = null
    }

    fun clearError() {
        _error.value = null
    }
}
