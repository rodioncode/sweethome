package com.jetbrains.kmpapp.data.profile

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String,
    val displayName: String,
    val email: String? = null,
    val avatarUrl: String? = null,
    val createdAt: String,
    val stats: ProfileStats = ProfileStats(),
)

@Serializable
data class ProfileStats(
    val totalDone: Int = 0,
    val totalLists: Int = 0,
    val workspacesCount: Int = 0,
)

@Serializable
data class ProfileActivityEvent(
    val id: String,
    val workspaceId: String,
    val workspaceName: String,
    val eventType: String,
    val payload: kotlinx.serialization.json.JsonObject? = null,
    val occurredAt: String,
)

@Serializable
data class UpdateProfileRequest(
    val displayName: String,
)

@Serializable
data class UpdateAvatarRequest(
    val avatarUrl: String,
)

@Serializable
data class NotificationPreference(
    val channel: String,
    val enabled: Boolean,
)

@Serializable
data class UpdateNotificationPreferenceRequest(
    val channel: String,
    val enabled: Boolean,
)
