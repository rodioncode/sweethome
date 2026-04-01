package com.jetbrains.kmpapp

import kotlinx.coroutines.flow.MutableStateFlow

object DeepLinkHandler {
    val pendingDeepLink = MutableStateFlow<String?>(null)

    fun handleDeepLink(url: String) {
        pendingDeepLink.value = url
    }
}
