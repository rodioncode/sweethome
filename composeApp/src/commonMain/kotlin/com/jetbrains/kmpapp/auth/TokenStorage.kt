package com.jetbrains.kmpapp.auth

interface TokenStorage {
    fun getAccessToken(): String?
    fun getRefreshToken(): String?
    fun getUserId(): String?
    fun getIsGuest(): Boolean?
    fun saveTokens(tokens: AuthTokens, isGuest: Boolean = false)
    fun clear()

    /** Курсор инкрементальной синхронизации (RFC3339). null = ещё не синхронизировались. */
    fun getLastSyncTimestamp(): String?
    fun saveLastSyncTimestamp(timestamp: String)
}

expect fun createTokenStorage(platformContext: Any?): TokenStorage
