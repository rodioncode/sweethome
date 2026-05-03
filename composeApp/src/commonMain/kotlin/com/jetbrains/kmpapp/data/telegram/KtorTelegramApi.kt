package com.jetbrains.kmpapp.data.telegram

import com.jetbrains.kmpapp.auth.ApiEnvelope
import com.jetbrains.kmpapp.auth.EmptyResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post

class KtorTelegramApi(
    private val apiClient: HttpClient,
    private val baseUrl: String,
) : TelegramApi {

    override suspend fun startLink(): Result<TelegramLinkStartResponse> = runCatching {
        val envelope: ApiEnvelope<TelegramLinkStartResponse> =
            apiClient.post("$baseUrl/telegram/link/start").body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        require(envelope.data != null) { "No data in response" }
        envelope.data
    }

    override suspend fun getStatus(): Result<TelegramLinkStatusResponse> = runCatching {
        val envelope: ApiEnvelope<TelegramLinkStatusResponse> =
            apiClient.get("$baseUrl/telegram/link/status").body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        require(envelope.data != null) { "No data in response" }
        envelope.data
    }

    override suspend fun unlink(): Result<Unit> = runCatching {
        val envelope: ApiEnvelope<EmptyResponse> =
            apiClient.delete("$baseUrl/telegram/link").body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        Unit
    }
}
