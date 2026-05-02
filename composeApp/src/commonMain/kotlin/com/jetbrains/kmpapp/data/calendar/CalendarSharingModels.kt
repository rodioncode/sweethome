package com.jetbrains.kmpapp.data.calendar

import kotlinx.serialization.Serializable

@Serializable
data class CalendarSharing(
    val workspaceId: String,
    val userId: String,
    val visibilityLevel: String,           // busy_only | by_type | custom
    val allowedTypes: List<String> = emptyList(),
    val allowedListIds: List<String> = emptyList(),
    val shareToken: String? = null,
)

@Serializable
data class PutSharingRequest(
    val visibilityLevel: String,
    val allowedTypes: List<String> = emptyList(),
    val allowedListIds: List<String> = emptyList(),
)

@Serializable
data class RotateTokenResponse(
    val shareToken: String,
)
