package com.jetbrains.kmpapp.data.telegram

import kotlinx.serialization.Serializable

@Serializable
data class TelegramLinkStartResponse(
    val code: String,
    val expiresAt: String,
    val deeplink: String,
)

@Serializable
data class TelegramLinkStatusResponse(
    val linked: Boolean,
    val telegramUserId: Long? = null,
    val telegramUsername: String? = null,
    val linkedAt: String? = null,
)
