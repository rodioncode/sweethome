package com.jetbrains.kmpapp.data.sync

import com.jetbrains.kmpapp.auth.ApiEnvelope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class KtorSyncApi(
    private val apiClient: HttpClient,
    private val baseUrl: String,
) : SyncApi {

    override suspend fun sync(since: String): Result<SyncResponse> = runCatching {
        val envelope: ApiEnvelope<SyncWrapper> =
            apiClient.get("$baseUrl/sync?since=$since").body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        val wrapper = requireNotNull(envelope.data) { "No data in response" }
        SyncResponse(items = wrapper.items, timestamp = wrapper.timestamp)
    }
}
