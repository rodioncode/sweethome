package com.jetbrains.kmpapp.data.chat

import com.jetbrains.kmpapp.auth.ApiEnvelope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.sse.sse
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

class KtorChatApi(
    private val apiClient: HttpClient,
    private val baseUrl: String,
) : ChatApi {

    override suspend fun getMessages(workspaceId: String, limit: Int, before: String?): Result<List<ChatMessage>> = runCatching {
        val envelope: ApiEnvelope<MessagesWrapper> = apiClient.get("$baseUrl/workspaces/$workspaceId/chat/messages") {
            parameter("limit", limit)
            before?.let { parameter("before", it) }
        }.body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        envelope.data?.messages ?: emptyList()
    }

    override suspend fun sendMessage(workspaceId: String, request: SendMessageRequest): Result<ChatMessage> = runCatching {
        val envelope: ApiEnvelope<ChatMessage> = apiClient.post("$baseUrl/workspaces/$workspaceId/chat/messages") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        require(envelope.data != null) { "No data in response" }
        envelope.data
    }

    override suspend fun markRead(workspaceId: String): Result<Unit> = runCatching {
        apiClient.post("$baseUrl/workspaces/$workspaceId/chat/read")
        Unit
    }

    override fun streamMessages(workspaceId: String): Flow<ChatStreamEvent> = flow {
        val json = Json { ignoreUnknownKeys = true; explicitNulls = false }
        var backoffMs = 1_000L
        while (true) {
            emit(ChatStreamEvent.Connecting)
            // Стартовый снапшот через REST.
            val initial = getMessages(workspaceId).getOrNull() ?: emptyList()
            val accumulated = initial.toMutableList()
            emit(ChatStreamEvent.Snapshot(accumulated.toList()))

            try {
                apiClient.sse(urlString = "$baseUrl/workspaces/$workspaceId/chat/stream") {
                    incoming.collect { event ->
                        val data = event.data ?: return@collect
                        // Пропускаем heartbeat (event начинается с ":") — Ktor SSE plugin фильтрует их сам.
                        val msg = runCatching { json.decodeFromString<ChatMessage>(data) }.getOrNull() ?: return@collect
                        val idx = accumulated.indexOfFirst { it.id == msg.id }
                        if (idx >= 0) accumulated[idx] = msg else accumulated += msg
                        emit(ChatStreamEvent.Snapshot(accumulated.toList()))
                    }
                }
                // Соединение закрылось без ошибки — реконнект.
                emit(ChatStreamEvent.Disconnected("closed"))
                backoffMs = 1_000L
            } catch (ce: CancellationException) {
                throw ce
            } catch (t: Throwable) {
                emit(ChatStreamEvent.Disconnected(t.message))
                // На время ожидания держим polling, чтобы пользователь не висел без апдейтов.
                val pollUntil = backoffMs
                val pollStart = currentTimeMillisCompat()
                while (currentTimeMillisCompat() - pollStart < pollUntil) {
                    delay(3_000)
                    getMessages(workspaceId).onSuccess { msgs ->
                        if (msgs != accumulated) {
                            accumulated.clear(); accumulated.addAll(msgs)
                            emit(ChatStreamEvent.Snapshot(accumulated.toList()))
                        }
                    }
                }
                backoffMs = (backoffMs * 2).coerceAtMost(30_000L)
            }
        }
    }
}

private fun currentTimeMillisCompat(): Long = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
