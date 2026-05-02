package com.jetbrains.kmpapp.auth

import com.jetbrains.kmpapp.data.lists.ListsStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthRepository(
    private val authApi: AuthApi,
    private val tokenStorage: TokenStorage,
    private val listsStorage: ListsStorage,
    private val onLogout: () -> Unit = {},
    private val onAuthenticated: () -> Unit = {},
) {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkStoredSession()
    }

    private fun checkStoredSession() {
        val accessToken = tokenStorage.getAccessToken()
        val refreshToken = tokenStorage.getRefreshToken()
        val userId = tokenStorage.getUserId()
        when {
            accessToken != null && refreshToken != null -> {
                _authState.value = AuthState.Authenticated(
                    userId = userId,
                    isGuest = tokenStorage.getIsGuest() ?: false,
                )
                onAuthenticated()
            }
            else -> _authState.value = AuthState.Unauthenticated
        }
    }


    suspend fun register(email: String, password: String, displayName: String, acceptedTerms: Boolean): Result<Unit> {
        val result = authApi.register(RegisterRequest(email, password, displayName, acceptedTerms))
        result.onSuccess { tokens ->
            tokenStorage.saveTokens(tokens, isGuest = false)
            _authState.value = AuthState.Authenticated(userId = tokens.userId, isGuest = false)
            onAuthenticated()
        }
        return result.map { }
    }

    suspend fun login(email: String, password: String): Result<Unit> {
        val result = authApi.login(LoginRequest(email, password))
        result.onSuccess { tokens ->
            tokenStorage.saveTokens(tokens, isGuest = false)
            _authState.value = AuthState.Authenticated(userId = tokens.userId, isGuest = false)
            onAuthenticated()
        }
        return result.map { }
    }

    suspend fun loginAsGuest(): Result<Unit> {
        val result = authApi.guest()
        result.onSuccess { tokens ->
            tokenStorage.saveTokens(tokens, isGuest = true)
            _authState.value = AuthState.Authenticated(userId = tokens.userId, isGuest = true)
            onAuthenticated()
        }
        return result.map { }
    }

    suspend fun linkEmail(email: String, password: String, displayName: String): Result<Unit> {
        return authApi.linkEmail(LinkEmailRequest(email, password, displayName)).also {
            it.onSuccess {
                _authState.value = (_authState.value as? AuthState.Authenticated)?.copy(isGuest = false)
                    ?: _authState.value
            }
        }
    }

    suspend fun logout(): Result<Unit> {
        val refreshToken = tokenStorage.getRefreshToken()
        if (refreshToken != null) {
            authApi.logout(refreshToken)
        }
        tokenStorage.clear()
        listsStorage.clear()
        onLogout()
        _authState.value = AuthState.Unauthenticated
        return Result.success(Unit)
    }

    suspend fun refreshTokens(): Result<AuthTokens> {
        val refreshToken = tokenStorage.getRefreshToken() ?: return Result.failure(IllegalStateException("No refresh token"))
        val result = authApi.refresh(refreshToken)
        result.onSuccess { tokens ->
            tokenStorage.saveTokens(tokens, isGuest = false)
            _authState.value = AuthState.Authenticated(userId = tokens.userId, isGuest = false)
        }
        result.onFailure {
            tokenStorage.clear()
            _authState.value = AuthState.Unauthenticated
        }
        return result
    }

    fun forceUnauthenticated() {
        tokenStorage.clear()
        onLogout()
        _authState.value = AuthState.Unauthenticated
    }

    suspend fun requestPasswordReset(email: String): Result<Unit> =
        authApi.requestPasswordReset(email)

    suspend fun confirmPasswordReset(token: String, newPassword: String): Result<Unit> =
        authApi.confirmPasswordReset(token, newPassword)

    fun getAccessToken(): String? = tokenStorage.getAccessToken()
    fun getRefreshToken(): String? = tokenStorage.getRefreshToken()
}

sealed class AuthState {
    data object Initial : AuthState()
    data object Unauthenticated : AuthState()
    data class Authenticated(val userId: String?, val isGuest: Boolean) : AuthState()
}
