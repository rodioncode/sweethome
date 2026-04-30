package com.jetbrains.kmpapp.auth

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class KtorAuthApi(
    private val authClient: HttpClient,
    private val apiClient: HttpClient,
    private val baseUrl: String,
) : AuthApi {

    override suspend fun register(request: RegisterRequest): Result<AuthTokens> = runCatching {
        val envelope: ApiEnvelope<AuthTokens> = authClient.post("$baseUrl/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        require(envelope.data != null) { "No data in response" }
        envelope.data
    }

    override suspend fun login(request: LoginRequest): Result<AuthTokens> = runCatching {
        val envelope: ApiEnvelope<AuthTokens> = authClient.post("$baseUrl/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        require(envelope.data != null) { "No data in response" }
        envelope.data
    }

    override suspend fun guest(): Result<AuthTokens> = runCatching {
        val envelope: ApiEnvelope<AuthTokens> = authClient.post("$baseUrl/auth/guest") {
            contentType(ContentType.Application.Json)
        }.body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        require(envelope.data != null) { "No data in response" }
        envelope.data
    }

    override suspend fun refresh(refreshToken: String): Result<AuthTokens> = runCatching {
        val envelope: ApiEnvelope<AuthTokens> = authClient.post("$baseUrl/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody(RefreshRequest(refreshToken))
        }.body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        require(envelope.data != null) { "No data in response" }
        envelope.data
    }

    override suspend fun logout(refreshToken: String): Result<Unit> = runCatching {
        val envelope: ApiEnvelope<EmptyResponse> = authClient.post("$baseUrl/auth/logout") {
            contentType(ContentType.Application.Json)
            setBody(LogoutRequest(refreshToken))
        }.body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        Unit
    }

    override suspend fun linkEmail(request: LinkEmailRequest): Result<Unit> = runCatching {
        val envelope: ApiEnvelope<EmptyResponse> = apiClient.post("$baseUrl/auth/link/email") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        Unit
    }

    override suspend fun registerDevice(request: RegisterDeviceRequest): Result<Unit> = runCatching {
        val envelope: ApiEnvelope<EmptyResponse> = apiClient.post("$baseUrl/auth/devices") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        Unit
    }

    override suspend fun requestPasswordReset(email: String): Result<Unit> = runCatching {
        val envelope: ApiEnvelope<EmptyResponse> = authClient.post("$baseUrl/auth/password/reset/request") {
            contentType(ContentType.Application.Json)
            setBody(PasswordResetRequestBody(email))
        }.body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        Unit
    }

    override suspend fun confirmPasswordReset(token: String, newPassword: String): Result<Unit> = runCatching {
        val envelope: ApiEnvelope<EmptyResponse> = authClient.post("$baseUrl/auth/password/reset/confirm") {
            contentType(ContentType.Application.Json)
            setBody(PasswordResetConfirmBody(token, newPassword))
        }.body()
        require(envelope.error == null) { envelope.error?.message ?: "Unknown error" }
        Unit
    }
}
