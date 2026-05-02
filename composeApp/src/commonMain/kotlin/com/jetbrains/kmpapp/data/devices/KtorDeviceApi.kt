package com.jetbrains.kmpapp.data.devices

import com.jetbrains.kmpapp.auth.ApiEnvelope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class KtorDeviceApi(
    private val apiClient: HttpClient,
    private val baseUrl: String,
) : DeviceApi {

    override suspend fun registerDevice(request: RegisterDeviceRequest): Result<RegisterDeviceResponse> = runCatching {
        val envelope: ApiEnvelope<RegisterDeviceResponse> = apiClient.post("$baseUrl/auth/devices") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        envelope.data ?: RegisterDeviceResponse()
    }
}
