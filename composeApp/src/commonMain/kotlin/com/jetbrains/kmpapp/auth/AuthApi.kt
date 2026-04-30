package com.jetbrains.kmpapp.auth

interface AuthApi {
    suspend fun register(request: RegisterRequest): Result<AuthTokens>
    suspend fun login(request: LoginRequest): Result<AuthTokens>
    suspend fun guest(): Result<AuthTokens>
    suspend fun refresh(refreshToken: String): Result<AuthTokens>
    suspend fun logout(refreshToken: String): Result<Unit>
    suspend fun linkEmail(request: LinkEmailRequest): Result<Unit>
    suspend fun registerDevice(request: RegisterDeviceRequest): Result<Unit>
    suspend fun requestPasswordReset(email: String): Result<Unit>
    suspend fun confirmPasswordReset(token: String, newPassword: String): Result<Unit>
}
