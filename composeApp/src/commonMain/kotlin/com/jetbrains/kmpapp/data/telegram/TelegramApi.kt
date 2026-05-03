package com.jetbrains.kmpapp.data.telegram

interface TelegramApi {
    suspend fun startLink(): Result<TelegramLinkStartResponse>
    suspend fun getStatus(): Result<TelegramLinkStatusResponse>
    suspend fun unlink(): Result<Unit>
}
