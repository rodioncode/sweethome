package com.jetbrains.kmpapp.data.chat

interface ChatApi {
    suspend fun getMessages(workspaceId: String, limit: Int = 50, before: String? = null): Result<List<ChatMessage>>
    suspend fun sendMessage(workspaceId: String, request: SendMessageRequest): Result<ChatMessage>
    suspend fun markRead(workspaceId: String): Result<Unit>
}
