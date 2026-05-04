package com.jetbrains.kmpapp.data.chat

import kotlinx.coroutines.flow.Flow

interface ChatApi {
    suspend fun getMessages(workspaceId: String, limit: Int = 50, before: String? = null): Result<List<ChatMessage>>
    suspend fun sendMessage(workspaceId: String, request: SendMessageRequest): Result<ChatMessage>
    suspend fun markRead(workspaceId: String): Result<Unit>

    /**
     * Поток новых сообщений + состояния соединения.
     * Реализация: WebSocket к `/v1/workspaces/{id}/chat/ws`. На обрыве — exponential backoff
     * + REST polling, чтобы пользователь не висел без апдейтов до восстановления WS.
     */
    fun streamMessages(workspaceId: String): Flow<ChatStreamEvent>
}

sealed class ChatStreamEvent {
    data class Snapshot(val messages: List<ChatMessage>) : ChatStreamEvent()
    data object Connecting : ChatStreamEvent()
    data class Disconnected(val cause: String?) : ChatStreamEvent()
}
