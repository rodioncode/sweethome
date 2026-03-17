package com.jetbrains.kmpapp.auth

interface TokenStorage {
    fun getAccessToken(): String?
    fun getRefreshToken(): String?
    fun getUserId(): String?
    fun getIsGuest(): Boolean?
    fun saveTokens(tokens: AuthTokens, isGuest: Boolean = false)
    fun clear()
}

expect fun createTokenStorage(platformContext: Any?): TokenStorage
