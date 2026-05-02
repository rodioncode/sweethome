package com.jetbrains.kmpapp.data.profile

import com.jetbrains.kmpapp.auth.ApiEnvelope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable

@Serializable
private data class NotificationPreferencesWrapper(val preferences: List<NotificationPreference>)

@Serializable
private data class ActivityWrapper(val events: List<ProfileActivityEvent>)

class KtorProfileApi(
    private val apiClient: HttpClient,
    private val baseUrl: String,
) : ProfileApi {

    override suspend fun getProfile(): Result<UserProfile> = runCatching {
        val envelope: ApiEnvelope<UserProfile> = apiClient.get("$baseUrl/auth/profile").body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        require(envelope.data != null) { "No data in response" }
        envelope.data
    }

    override suspend fun updateProfile(request: UpdateProfileRequest): Result<UserProfile> = runCatching {
        val envelope: ApiEnvelope<UserProfile> = apiClient.patch("$baseUrl/auth/profile") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        require(envelope.data != null) { "No data in response" }
        envelope.data
    }

    override suspend fun updateAvatar(request: UpdateAvatarRequest): Result<UserProfile> = runCatching {
        val envelope: ApiEnvelope<UserProfile> = apiClient.patch("$baseUrl/auth/profile/avatar") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        require(envelope.data != null) { "No data in response" }
        envelope.data
    }

    override suspend fun getActivity(): Result<List<ProfileActivityEvent>> = runCatching {
        val envelope: ApiEnvelope<ActivityWrapper> = apiClient.get("$baseUrl/auth/profile/activity").body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        envelope.data?.events ?: emptyList()
    }

    override suspend fun getNotificationPreferences(): Result<List<NotificationPreference>> = runCatching {
        val envelope: ApiEnvelope<NotificationPreferencesWrapper> =
            apiClient.get("$baseUrl/auth/notification-preferences").body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        envelope.data?.preferences ?: emptyList()
    }

    override suspend fun updateNotificationPreference(request: UpdateNotificationPreferenceRequest): Result<Unit> = runCatching {
        apiClient.put("$baseUrl/auth/notification-preferences") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        Unit
    }

    override suspend fun deleteAccount(): Result<Unit> = runCatching {
        val response = apiClient.delete("$baseUrl/users/me")
        if (response.status == HttpStatusCode.TooManyRequests) throw TooManyRequestsException()
        require(response.status.isSuccess()) { "delete_account_failed_${response.status.value}" }
        Unit
    }
}
