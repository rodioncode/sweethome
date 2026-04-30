package com.jetbrains.kmpapp.data.chat

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val id: String,
    val workspaceId: String,
    val senderId: String,
    val senderName: String,
    val content: String,
    val type: String = "text",
    val createdAt: String,
    val editedAt: String? = null,
    val readCount: Int = 0,
)

@Serializable
data class SendMessageRequest(
    val content: String,
    val type: String = "text",
)

@Serializable
data class MessagesWrapper(
    val messages: List<ChatMessage>,
    val total: Int = 0,
)
