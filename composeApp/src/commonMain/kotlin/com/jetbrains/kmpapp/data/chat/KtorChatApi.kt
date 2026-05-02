package com.jetbrains.kmpapp.data.chat

import com.jetbrains.kmpapp.auth.ApiEnvelope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

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
        emit(ChatStreamEvent.Connecting)
        var lastSnapshot: List<ChatMessage> = emptyList()
        while (true) {
            getMessages(workspaceId).fold(
                onSuccess = { msgs ->
                    if (msgs != lastSnapshot) {
                        lastSnapshot = msgs
                        emit(ChatStreamEvent.Snapshot(msgs))
                    }
                },
                onFailure = { emit(ChatStreamEvent.Disconnected(it.message)) },
            )
            delay(3_000)
        }
    }
}
