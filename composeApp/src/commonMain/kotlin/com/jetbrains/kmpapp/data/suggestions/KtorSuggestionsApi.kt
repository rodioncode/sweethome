package com.jetbrains.kmpapp.data.suggestions

import com.jetbrains.kmpapp.auth.ApiEnvelope
import com.jetbrains.kmpapp.data.lists.TodoItem
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
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
}
