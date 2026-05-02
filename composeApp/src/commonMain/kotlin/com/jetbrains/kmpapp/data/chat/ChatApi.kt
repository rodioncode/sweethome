package com.jetbrains.kmpapp.data.chat

import kotlinx.coroutines.flow.Flow

interface ChatApi {
    suspend fun getMessages(workspaceId: String, limit: Int = 50, before: String? = null): Result<List<ChatMessage>>
    suspend fun sendMessage(workspaceId: String, request: SendMessageRequest): Result<ChatMessage>
    suspend fun markRead(workspaceId: String): Result<Unit>

    /**
     * Поток новых сообщений + состояния соединения.
     * Текущая реализация — long-poll каждые ~3с (контракт совместим с будущим SSE).
     * TODO: переключить на Ktor SSE: install(SSE) на apiClient + sse("$baseUrl/workspaces/$id/chat/stream").
     * WS (`/v1/workspaces/{id}/chat/ws`) — последующая итерация для двунаправленного канала.
     */
    fun streamMessages(workspaceId: String): Flow<ChatStreamEvent>
}

sealed class ChatStreamEvent {
    data class Snapshot(val messages: List<ChatMessage>) : ChatStreamEvent()
    data object Connecting : ChatStreamEvent()
    data class Disconnected(val cause: String?) : ChatStreamEvent()
}
