package com.jetbrains.kmpapp.data.lists

interface ListsApi {
    suspend fun getLists(workspaceId: String? = null): Result<List<TodoList>>
    suspend fun createList(request: CreateListRequest): Result<TodoList>
    suspend fun getListWithItems(listId: String): Result<Pair<TodoList, List<TodoItem>>>
    suspend fun updateList(listId: String, request: UpdateListRequest): Result<TodoList>
    suspend fun deleteList(listId: String): Result<Unit>
    suspend fun createItem(listId: String, request: CreateItemRequest): Result<TodoItem>
    suspend fun updateItem(itemId: String, request: UpdateItemRequest): Result<TodoItem>
    suspend fun deleteItem(itemId: String): Result<Unit>
}
