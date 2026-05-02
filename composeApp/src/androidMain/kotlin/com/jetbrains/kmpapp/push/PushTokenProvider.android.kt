package com.jetbrains.kmpapp.push

// TODO: подключить Firebase Messaging — сейчас возвращает null,
// пока FCM не настроен. После интеграции:
// FirebaseMessaging.getInstance().token.await()
actual fun createPushTokenProvider(platformContext: Any?): PushTokenProvider =
    object : PushTokenProvider {
        override val platform = "android"
        override suspend fun getToken(): String? = null
    }
