package com.jetbrains.kmpapp.auth

import kotlinx.serialization.Serializable

@Serializable
data class AuthTokens(
    val userId: String? = null,
    val accessToken: String,
    val refreshToken: String,
)

@Serializable
data class ApiEnvelope<T>(
    val data: T? = null,
    val error: ApiError? = null,
)

@Serializable
data class ApiError(
    val code: String? = null,
    val message: String? = null,
)

@Serializable
class EmptyResponse

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val displayName: String,
    val acceptedTerms: Boolean,
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
)

@Serializable
data class RefreshRequest(
    val refreshToken: String,
)

@Serializable
data class LogoutRequest(
    val refreshToken: String,
)

@Serializable
data class LinkEmailRequest(
    val email: String,
    val password: String,
    val displayName: String,
)

@Serializable
data class RegisterDeviceRequest(
    val platform: String,
    val pushToken: String,
)

@Serializable
data class PasswordResetRequestBody(
    val email: String,
)

@Serializable
data class PasswordResetConfirmBody(
    val token: String,
    val newPassword: String,
)
