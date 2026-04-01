package com.jetbrains.kmpapp.data.lists

import com.jetbrains.kmpapp.auth.ApiEnvelope
import com.jetbrains.kmpapp.auth.EmptyResponse
import com.jetbrains.kmpapp.auth.getApiBaseUrl
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class KtorListsApi(
    private val apiClient: HttpClient,
    private val baseUrl: String,
) : ListsApi {

    override suspend fun getLists(scope: String?, groupId: String?): Result<List<TodoList>> = runCatching {
        val params = buildList {
            scope?.let { add("scope=$it") }
            groupId?.let { add("groupId=$it") }
        }
        val query = params.takeIf { it.isNotEmpty() }?.joinToString("&")?.let { "?$it" } ?: ""
        val envelope: ApiEnvelope<ListsWrapper> = apiClient.get("$baseUrl/lists$query").body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        require(envelope.data != null) { "No data in response" }
        envelope.data.lists
    }

    override suspend fun createList(request: CreateListRequest): Result<TodoList> = runCatching {
        val envelope: ApiEnvelope<TodoList> = apiClient.post("$baseUrl/lists") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        require(envelope.data != null) { "No data in response" }
        envelope.data
    }

    override suspend fun getListWithItems(listId: String): Result<Pair<TodoList, List<TodoItem>>> = runCatching {
        val envelope: ApiEnvelope<ListWithItemsWrapper> = apiClient.get("$baseUrl/lists/$listId").body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        require(envelope.data != null) { "No data in response" }
        envelope.data.list to envelope.data.items
    }

    override suspend fun updateList(listId: String, request: UpdateListRequest): Result<TodoList> = runCatching {
        val envelope: ApiEnvelope<TodoList> = apiClient.patch("$baseUrl/lists/$listId") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        require(envelope.data != null) { "No data in response" }
        envelope.data
    }

    override suspend fun deleteList(listId: String): Result<Unit> = runCatching {
        val envelope: ApiEnvelope<EmptyResponse> = apiClient.delete("$baseUrl/lists/$listId").body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        Unit
    }

    override suspend fun createItem(listId: String, request: CreateItemRequest): Result<TodoItem> = runCatching {
        val envelope: ApiEnvelope<TodoItem> = apiClient.post("$baseUrl/lists/$listId/items") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        require(envelope.data != null) { "No data in response" }
        envelope.data
    }

    override suspend fun updateItem(itemId: String, request: UpdateItemRequest): Result<TodoItem> = runCatching {
        val envelope: ApiEnvelope<TodoItem> = apiClient.patch("$baseUrl/items/$itemId") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        require(envelope.data != null) { "No data in response" }
        envelope.data
    }

    override suspend fun deleteItem(itemId: String): Result<Unit> = runCatching {
        val envelope: ApiEnvelope<EmptyResponse> = apiClient.delete("$baseUrl/items/$itemId").body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        Unit
    }
}
