package com.jetbrains.kmpapp.auth

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.alloc
import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.CoreFoundation.CFBridgingRelease
import platform.CoreFoundation.CFBridgingRetain
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFDictionarySetValue
import platform.CoreFoundation.CFMutableDictionaryRef
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFStringCreateWithCString
import platform.CoreFoundation.CFStringRef
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFStringEncodingUTF8
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.SecItemUpdate
import platform.Security.errSecItemNotFound
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

actual fun createTokenStorage(platformContext: Any?): TokenStorage = IosKeychainTokenStorage()

@OptIn(ExperimentalForeignApi::class)
private class IosKeychainTokenStorage : TokenStorage {

    private fun MemScope.cfStr(s: String): CFStringRef? =
        CFStringCreateWithCString(null, s.cstr.ptr, kCFStringEncodingUTF8)

    private fun MemScope.buildQuery(account: String): CFMutableDictionaryRef? {
        val dict = CFDictionaryCreateMutable(null, 4, null, null) ?: return null
        CFDictionarySetValue(dict, kSecClass, kSecClassGenericPassword)
        CFDictionarySetValue(dict, kSecAttrService, cfStr(SERVICE))
        CFDictionarySetValue(dict, kSecAttrAccount, cfStr(account))
        return dict
    }

    private fun keychainSave(account: String, value: String) = memScoped {
        val data = NSString.create(string = value)!!.dataUsingEncoding(NSUTF8StringEncoding)!!
        val dataRef = CFBridgingRetain(data)
        val query = buildQuery(account) ?: run { dataRef?.let { CFRelease(it) }; return@memScoped }

        val updateDict = CFDictionaryCreateMutable(null, 1, null, null)!!
        CFDictionarySetValue(updateDict, kSecValueData, dataRef)

        val status = SecItemUpdate(query, updateDict)
        if (status == errSecItemNotFound) {
            CFDictionarySetValue(query, kSecValueData, dataRef)
            SecItemAdd(query, null)
        }

        dataRef?.let { CFRelease(it) }
        CFRelease(updateDict)
        CFRelease(query)
    }

    private fun keychainRead(account: String): String? = memScoped {
        val query = buildQuery(account) ?: return@memScoped null
        CFDictionarySetValue(query, kSecReturnData, kCFBooleanTrue)
        CFDictionarySetValue(query, kSecMatchLimit, kSecMatchLimitOne)

        val result = alloc<CFTypeRefVar>()
        val status = SecItemCopyMatching(query, result.ptr)
        CFRelease(query)

        if (status != errSecSuccess) return@memScoped null
        val dataRef = result.value ?: return@memScoped null
        val nsData = CFBridgingRelease(dataRef) as? NSData ?: return@memScoped null
        NSString.create(nsData, NSUTF8StringEncoding)?.toString()
    }

    private fun keychainDelete(account: String) = memScoped {
        val query = buildQuery(account) ?: return@memScoped
        SecItemDelete(query)
        CFRelease(query)
    }

    override fun getAccessToken() = keychainRead(KEY_ACCESS)
    override fun getRefreshToken() = keychainRead(KEY_REFRESH)
    override fun getUserId() = keychainRead(KEY_USER_ID)
    override fun getIsGuest() = keychainRead(KEY_IS_GUEST)?.let { it == "true" }

    override fun saveTokens(tokens: AuthTokens, isGuest: Boolean) {
        keychainSave(KEY_ACCESS, tokens.accessToken)
        keychainSave(KEY_REFRESH, tokens.refreshToken)
        tokens.userId?.let { keychainSave(KEY_USER_ID, it) }
        keychainSave(KEY_IS_GUEST, if (isGuest) "true" else "false")
    }

    override fun clear() {
        keychainDelete(KEY_ACCESS)
        keychainDelete(KEY_REFRESH)
        keychainDelete(KEY_USER_ID)
        keychainDelete(KEY_IS_GUEST)
    }

    override fun getSyncTimestamp() = keychainRead(KEY_SYNC_TS)
    override fun saveSyncTimestamp(timestamp: String) = keychainSave(KEY_SYNC_TS, timestamp)

    companion object {
        private const val SERVICE = "com.sweethome.app"
        private const val KEY_ACCESS = "access_token"
        private const val KEY_REFRESH = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_IS_GUEST = "is_guest"
        private const val KEY_SYNC_TS = "sync_timestamp"
    }
}
