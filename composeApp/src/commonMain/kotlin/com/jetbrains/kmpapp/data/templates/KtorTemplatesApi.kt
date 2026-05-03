package com.jetbrains.kmpapp.data.templates

import com.jetbrains.kmpapp.auth.ApiEnvelope
import com.jetbrains.kmpapp.data.lists.TodoItem
import com.jetbrains.kmpapp.data.lists.TodoList
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class KtorTemplatesApi(
    private val apiClient: HttpClient,
    private val baseUrl: String,
) : TemplatesApi {

    // ─── List templates ───────────────────────────────────────────────────

    override suspend fun getPublicListTemplates(scope: String?, locale: String): Result<List<ListTemplate>> = runCatching {
        val envelope: ApiEnvelope<ListTemplatesWrapper> = apiClient.get("$baseUrl/templates/public") {
            parameter("locale", locale)
            scope?.let { parameter("scope", it) }
        }.body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        envelope.data?.templates ?: emptyList()
    }

    override suspend fun getMyListTemplates(scope: String?, locale: String): Result<List<ListTemplate>> = runCatching {
        val envelope: ApiEnvelope<ListTemplatesWrapper> = apiClient.get("$baseUrl/templates/mine") {
            parameter("locale", locale)
            scope?.let { parameter("scope", it) }
        }.body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        envelope.data?.templates ?: emptyList()
    }

    override suspend fun getFavoriteListTemplates(locale: String): Result<List<ListTemplate>> = runCatching {
        val envelope: ApiEnvelope<ListTemplatesWrapper> = apiClient.get("$baseUrl/templates/favorites") {
            parameter("locale", locale)
        }.body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        envelope.data?.templates ?: emptyList()
    }

    override suspend fun getListTemplate(id: String, locale: String): Result<ListTemplateDetail> = runCatching {
        val envelope: ApiEnvelope<ListTemplateDetail> = apiClient.get("$baseUrl/templates/$id") {
            parameter("locale", locale)
        }.body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        require(envelope.data != null) { "No template" }
        envelope.data
    }

    override suspend fun useListTemplate(id: String, request: UseListTemplateRequest): Result<TodoList> = runCatching {
        val envelope: ApiEnvelope<TodoList> = apiClient.post("$baseUrl/templates/$id/use") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        require(envelope.data != null) { "No list" }
        envelope.data
    }

    override suspend fun requestListPublication(id: String): Result<Unit> = runCatching {
        apiClient.post("$baseUrl/templates/$id/request-publication")
        Unit
    }

    override suspend fun withdrawListPublication(id: String): Result<Unit> = runCatching {
        apiClient.post("$baseUrl/templates/$id/withdraw-publication")
        Unit
    }

    override suspend fun favoriteListTemplate(id: String): Result<Unit> = runCatching {
        apiClient.put("$baseUrl/templates/$id/favorite")
        Unit
    }

    override suspend fun unfavoriteListTemplate(id: String): Result<Unit> = runCatching {
        apiClient.delete("$baseUrl/templates/$id/favorite")
        Unit
    }

    override suspend fun deleteListTemplate(id: String): Result<Unit> = runCatching {
        apiClient.delete("$baseUrl/templates/$id")
        Unit
    }

    // ─── Task templates ───────────────────────────────────────────────────

    override suspend fun getPublicTaskTemplates(scope: String?, locale: String): Result<List<TaskTemplate>> = runCatching {
        val envelope: ApiEnvelope<TaskTemplatesWrapper> = apiClient.get("$baseUrl/task-templates/public") {
            parameter("locale", locale)
            scope?.let { parameter("scope", it) }
        }.body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        envelope.data?.templates ?: emptyList()
    }

    override suspend fun getMyTaskTemplates(scope: String?, locale: String): Result<List<TaskTemplate>> = runCatching {
        val envelope: ApiEnvelope<TaskTemplatesWrapper> = apiClient.get("$baseUrl/task-templates/mine") {
            parameter("locale", locale)
            scope?.let { parameter("scope", it) }
        }.body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        envelope.data?.templates ?: emptyList()
    }

    override suspend fun getFavoriteTaskTemplates(locale: String): Result<List<TaskTemplate>> = runCatching {
        val envelope: ApiEnvelope<TaskTemplatesWrapper> = apiClient.get("$baseUrl/task-templates/favorites") {
            parameter("locale", locale)
        }.body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        envelope.data?.templates ?: emptyList()
    }

    override suspend fun getTaskTemplate(id: String, locale: String): Result<TaskTemplateDetail> = runCatching {
        val envelope: ApiEnvelope<TaskTemplateDetail> = apiClient.get("$baseUrl/task-templates/$id") {
            parameter("locale", locale)
        }.body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        require(envelope.data != null) { "No template" }
        envelope.data
    }

    override suspend fun useTaskTemplate(id: String, request: UseTaskTemplateRequest): Result<TodoItem> = runCatching {
        val envelope: ApiEnvelope<TodoItem> = apiClient.post("$baseUrl/task-templates/$id/use") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        require(envelope.data != null) { "No item" }
        envelope.data
    }

    override suspend fun requestTaskPublication(id: String): Result<Unit> = runCatching {
        apiClient.post("$baseUrl/task-templates/$id/request-publication")
        Unit
    }

    override suspend fun withdrawTaskPublication(id: String): Result<Unit> = runCatching {
        apiClient.post("$baseUrl/task-templates/$id/withdraw-publication")
        Unit
    }

    override suspend fun favoriteTaskTemplate(id: String): Result<Unit> = runCatching {
        apiClient.put("$baseUrl/task-templates/$id/favorite")
        Unit
    }

    override suspend fun unfavoriteTaskTemplate(id: String): Result<Unit> = runCatching {
        apiClient.delete("$baseUrl/task-templates/$id/favorite")
        Unit
    }

    override suspend fun deleteTaskTemplate(id: String): Result<Unit> = runCatching {
        apiClient.delete("$baseUrl/task-templates/$id")
        Unit
    }

    // ─── Save list as template ────────────────────────────────────────────

    override suspend fun saveListAsTemplate(listId: String, request: SaveAsTemplateRequest): Result<ListTemplate> = runCatching {
        val envelope: ApiEnvelope<ListTemplate> = apiClient.post("$baseUrl/lists/$listId/save-as-template") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        require(envelope.data != null) { "No template" }
        envelope.data
    }
}
