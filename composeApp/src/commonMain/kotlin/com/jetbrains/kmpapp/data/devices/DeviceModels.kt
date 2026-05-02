package com.jetbrains.kmpapp.data.devices

import kotlinx.serialization.Serializable

@Serializable
data class RegisterDeviceRequest(
    val platform: String,   // "ios" | "android" | "web"
    val pushToken: String,
)

@Serializable
data class RegisterDeviceResponse(
    val deviceId: String? = null,
)
