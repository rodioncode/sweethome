package com.jetbrains.kmpapp.data.sync

import com.jetbrains.kmpapp.auth.ApiEnvelope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class KtorSyncApi(
    private val apiClient: HttpClient,
    private val baseUrl: String,
) : SyncApi {

    override suspend fun sync(since: String): Result<SyncWrapper> = runCatching {
        val envelope: ApiEnvelope<SyncWrapper> = apiClient.get("$baseUrl/sync") {
            parameter("since", since)
        }.body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        require(envelope.data != null) { "No data in response" }
        envelope.data
    }
}
