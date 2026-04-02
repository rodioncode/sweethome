package com.jetbrains.kmpapp.auth

import platform.Foundation.NSUserDefaults

actual fun createTokenStorage(platformContext: Any?): TokenStorage = IosTokenStorage()

private class IosTokenStorage : TokenStorage {
    private val prefs = NSUserDefaults.standardUserDefaults

    override fun getAccessToken(): String? = prefs.stringForKey(KEY_ACCESS)
    override fun getRefreshToken(): String? = prefs.stringForKey(KEY_REFRESH)
    override fun getUserId(): String? = prefs.stringForKey(KEY_USER_ID)
    override fun getIsGuest(): Boolean? {
        val value = prefs.stringForKey(KEY_IS_GUEST) ?: return null
        return value == "true"
    }

    override fun saveTokens(tokens: AuthTokens, isGuest: Boolean) {
        prefs.setObject(tokens.accessToken, forKey = KEY_ACCESS)
        prefs.setObject(tokens.refreshToken, forKey = KEY_REFRESH)
        tokens.userId?.let { prefs.setObject(it, forKey = KEY_USER_ID) }
        prefs.setObject(if (isGuest) "true" else "false", forKey = KEY_IS_GUEST)
        prefs.synchronize()
    }

    override fun clear() {
        prefs.removeObjectForKey(KEY_ACCESS)
        prefs.removeObjectForKey(KEY_REFRESH)
        prefs.removeObjectForKey(KEY_USER_ID)
        prefs.removeObjectForKey(KEY_IS_GUEST)
        prefs.synchronize()
    }

    override fun getLastSyncTimestamp(): String? = prefs.stringForKey(KEY_LAST_SYNC)

    override fun saveLastSyncTimestamp(timestamp: String) {
        prefs.setObject(timestamp, forKey = KEY_LAST_SYNC)
        prefs.synchronize()
    }

    companion object {
        private const val KEY_ACCESS = "access_token"
        private const val KEY_REFRESH = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_IS_GUEST = "is_guest"
        private const val KEY_LAST_SYNC = "last_sync_timestamp"
    }
}
