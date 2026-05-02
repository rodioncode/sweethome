package com.jetbrains.kmpapp.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

actual fun createTokenStorage(platformContext: Any?): TokenStorage =
    AndroidTokenStorage(platformContext as Context)

private class AndroidTokenStorage(private val context: Context) : TokenStorage {
    private val prefs: SharedPreferences = openOrRecreate()

    private fun openOrRecreate(): SharedPreferences {
        return try {
            createEncryptedPrefs()
        } catch (e: Exception) {
            // Keystore key was invalidated (e.g. app reinstall) — wipe stale prefs and start fresh
            context.deleteSharedPreferences(PREFS_NAME)
            createEncryptedPrefs()
        }
    }

    private fun createEncryptedPrefs(): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    override fun getAccessToken(): String? = prefs.getString(KEY_ACCESS, null)
    override fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH, null)
    override fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)
    override fun getIsGuest(): Boolean? = prefs.getBoolean(KEY_IS_GUEST, false).takeIf { prefs.contains(KEY_IS_GUEST) }

    override fun saveTokens(tokens: AuthTokens, isGuest: Boolean) {
        prefs.edit()
            .putString(KEY_ACCESS, tokens.accessToken)
            .putString(KEY_REFRESH, tokens.refreshToken)
            .putString(KEY_USER_ID, tokens.userId)
            .putBoolean(KEY_IS_GUEST, isGuest)
            .apply()
    }

    override fun clear() {
        prefs.edit().clear().apply()
    }

    override fun getSyncTimestamp(): String? = prefs.getString(KEY_SYNC_TS, null)

    override fun saveSyncTimestamp(timestamp: String) {
        prefs.edit().putString(KEY_SYNC_TS, timestamp).apply()
    }

    override fun getRegisteredPushToken(): String? = prefs.getString(KEY_PUSH_TOKEN, null)

    override fun saveRegisteredPushToken(token: String) {
        prefs.edit().putString(KEY_PUSH_TOKEN, token).apply()
    }

    companion object {
        private const val PREFS_NAME = "auth_tokens"
        private const val KEY_ACCESS = "access_token"
        private const val KEY_REFRESH = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_IS_GUEST = "is_guest"
        private const val KEY_SYNC_TS = "sync_timestamp"
        private const val KEY_PUSH_TOKEN = "push_token"
    }
}
