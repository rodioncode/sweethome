package com.jetbrains.kmpapp.push

interface PushTokenProvider {
    val platform: String
    suspend fun getToken(): String?
}

expect fun createPushTokenProvider(platformContext: Any?): PushTokenProvider
