package com.jetbrains.kmpapp.push

import com.jetbrains.kmpapp.auth.TokenStorage
import com.jetbrains.kmpapp.data.devices.DeviceApi
import com.jetbrains.kmpapp.data.devices.RegisterDeviceRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class DeviceRegistrar(
    private val tokenProvider: PushTokenProvider,
    private val deviceApi: DeviceApi,
    private val tokenStorage: TokenStorage,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun registerInBackground() {
        scope.launch { register() }
    }

    suspend fun register() {
        val token = runCatching { tokenProvider.getToken() }.getOrNull()?.takeIf { it.isNotBlank() } ?: return
        if (tokenStorage.getRegisteredPushToken() == token) return
        deviceApi.registerDevice(
            RegisterDeviceRequest(platform = tokenProvider.platform, pushToken = token)
        ).onSuccess { tokenStorage.saveRegisteredPushToken(token) }
    }
}
