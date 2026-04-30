package com.jetbrains.kmpapp.data.notifications

import com.jetbrains.kmpapp.auth.ApiEnvelope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post

class KtorNotificationsApi(
    private val apiClient: HttpClient,
    private val baseUrl: String,
) : NotificationsApi {

    override suspend fun getNotifications(unreadOnly: Boolean, limit: Int, before: String?): Result<NotificationsWrapper> = runCatching {
        val envelope: ApiEnvelope<NotificationsWrapper> = apiClient.get("$baseUrl/notifications") {
            parameter("limit", limit)
            if (unreadOnly) parameter("unreadOnly", true)
            before?.let { parameter("before", it) }
        }.body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        envelope.data ?: NotificationsWrapper(emptyList())
    }

    override suspend fun markAllRead(): Result<Unit> = runCatching {
        apiClient.post("$baseUrl/notifications/read-all")
        Unit
    }

    override suspend fun markRead(id: String): Result<Unit> = runCatching {
        apiClient.patch("$baseUrl/notifications/$id/read")
        Unit
    }
}
