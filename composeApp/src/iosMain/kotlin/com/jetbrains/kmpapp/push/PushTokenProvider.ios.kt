package com.jetbrains.kmpapp.push

// TODO: получать APNs deviceToken из AppDelegate и пробрасывать через mutable provider.
// Сейчас заглушка — возвращает null, пока iOS-сторона не отдаёт токен.
actual fun createPushTokenProvider(platformContext: Any?): PushTokenProvider =
    object : PushTokenProvider {
        override val platform = "ios"
        override suspend fun getToken(): String? = null
    }
