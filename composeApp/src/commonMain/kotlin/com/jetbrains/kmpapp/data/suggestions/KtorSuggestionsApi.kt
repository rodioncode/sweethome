package com.jetbrains.kmpapp.data.suggestions

import com.jetbrains.kmpapp.auth.ApiEnvelope
import com.jetbrains.kmpapp.data.lists.TodoItem
import com.jetbrains.kmpapp.data.lists.TodoList
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

@Serializable
private data class ItemsWrapper(val items: List<TodoItem>)

class KtorSuggestionsApi(
    private val apiClient: HttpClient,
    private val baseUrl: String,
) : SuggestionsApi {

    override suspend fun getChoreTemplates(locale: String): Result<List<ChoreTemplate>> = runCatching {
        val envelope: ApiEnvelope<ChoreTemplatesWrapper> =
            apiClient.get("$baseUrl/suggestions/chore-templates?locale=$locale").body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        require(envelope.data != null) { "No data in response" }
        envelope.data.templates
    }

    override suspend fun getFrequentItems(listId: String): Result<List<TodoItem>> = runCatching {
        val envelope: ApiEnvelope<ItemsWrapper> =
            apiClient.get("$baseUrl/suggestions/frequent-items?listId=$listId").body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        require(envelope.data != null) { "No data in response" }
        envelope.data.items
    }

    override suspend fun getFavorites(): Result<List<TodoItem>> = runCatching {
        val envelope: ApiEnvelope<ItemsWrapper> =
            apiClient.get("$baseUrl/suggestions/favorites").body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        require(envelope.data != null) { "No data in response" }
        envelope.data.items
    }

    override suspend fun getAllTemplates(locale: String): Result<List<Template>> = runCatching {
        val envelope: ApiEnvelope<TemplatesListWrapper> =
            apiClient.get("$baseUrl/templates?locale=$locale").body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        envelope.data?.templates ?: emptyList()
    }

    override suspend fun getTemplate(id: String): Result<TemplateWithItems> = runCatching {
        val envelope: ApiEnvelope<TemplateWithItems> =
            apiClient.get("$baseUrl/templates/$id").body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        require(envelope.data != null) { "No data in response" }
        envelope.data
    }

    override suspend fun useTemplate(id: String, request: UseTemplateRequest): Result<TodoList> = runCatching {
        val envelope: ApiEnvelope<TodoList> =
            apiClient.post("$baseUrl/templates/$id/use") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        require(envelope.data != null) { "No data in response" }
        envelope.data
    }
}
