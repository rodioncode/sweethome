package com.jetbrains.kmpapp.data.notifications

interface NotificationsApi {
    suspend fun getNotifications(unreadOnly: Boolean = false, limit: Int = 50, before: String? = null): Result<NotificationsWrapper>
    suspend fun markAllRead(): Result<Unit>
    suspend fun markRead(id: String): Result<Unit>
}
