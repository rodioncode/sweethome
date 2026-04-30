package com.jetbrains.kmpapp.data.notifications

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class Notification(
    val id: String,
    val type: String,
    val title: String,
    val body: String,
    val isRead: Boolean = false,
    val createdAt: String,
    val payload: JsonObject? = null,
)

@Serializable
data class NotificationsWrapper(
    val notifications: List<Notification>,
    val total: Int = 0,
    val unreadCount: Int = 0,
)
