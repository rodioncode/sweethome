package com.jetbrains.kmpapp.data.devices

interface DeviceApi {
    suspend fun registerDevice(request: RegisterDeviceRequest): Result<RegisterDeviceResponse>
}
