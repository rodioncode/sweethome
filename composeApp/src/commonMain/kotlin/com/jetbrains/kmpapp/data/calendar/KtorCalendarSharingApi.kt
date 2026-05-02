package com.jetbrains.kmpapp.data.calendar

import com.jetbrains.kmpapp.auth.ApiEnvelope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class KtorCalendarSharingApi(
    private val apiClient: HttpClient,
    private val baseUrl: String,
) : CalendarSharingApi {

    override suspend fun getSharing(workspaceId: String): Result<CalendarSharing> = runCatching {
        val envelope: ApiEnvelope<CalendarSharing> =
            apiClient.get("$baseUrl/workspaces/$workspaceId/calendar/sharing").body()
        require(envelope.error == null) { envelope.error?.message ?: "sharing_failed" }
        require(envelope.data != null) { "no_sharing" }
        envelope.data
    }

    override suspend fun putSharing(workspaceId: String, request: PutSharingRequest): Result<CalendarSharing> = runCatching {
        val envelope: ApiEnvelope<CalendarSharing> =
            apiClient.put("$baseUrl/workspaces/$workspaceId/calendar/sharing") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
        require(envelope.error == null) { envelope.error?.message ?: "sharing_failed" }
        require(envelope.data != null) { "no_sharing" }
        envelope.data
    }

    override suspend fun rotateToken(workspaceId: String): Result<RotateTokenResponse> = runCatching {
        val envelope: ApiEnvelope<RotateTokenResponse> =
            apiClient.post("$baseUrl/workspaces/$workspaceId/calendar/sharing/rotate-token").body()
        require(envelope.error == null) { envelope.error?.message ?: "rotate_failed" }
        require(envelope.data != null) { "no_token" }
        envelope.data
    }
}
