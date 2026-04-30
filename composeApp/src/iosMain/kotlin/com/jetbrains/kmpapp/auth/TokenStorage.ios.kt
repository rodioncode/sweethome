package com.jetbrains.kmpapp.auth

import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.interpretCPointer
import kotlinx.cinterop.interpretObjCPointerOrNull
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.objcPtr
import kotlinx.cinterop.ptr
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFStringRef
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFBooleanTrue
import platform.Foundation.NSData
import platform.Foundation.NSMutableDictionary
import platform.Foundation.NSNumber
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
@Suppress("UNCHECKED_CAST")
private class IosKeychainTokenStorage : TokenStorage {

    private fun CFStringRef?.ns(): NSString? =
        this?.let { interpretObjCPointerOrNull<NSString>(it.rawValue) }

    // Toll-free bridge via raw ObjC pointer — avoids the unsafe `as CFDictionaryRef` class cast
    // Type parameter inferred from CFDictionaryRef = CPointer<__CFDictionary>
    private fun NSMutableDictionary.toCFDict(): CFDictionaryRef? =
        interpretCPointer(objcPtr())

    private fun buildQuery(account: String): NSMutableDictionary = NSMutableDictionary().apply {
        setObject(kSecClassGenericPassword.ns()!!, kSecClass.ns()!!)
        setObject(NSString.create(string = SERVICE)!!, kSecAttrService.ns()!!)
        setObject(NSString.create(string = account)!!, kSecAttrAccount.ns()!!)
    }

    private fun String.toNSData(): NSData {
        val bytes = encodeToByteArray()
        return bytes.usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
        }
    }

    private fun keychainSave(account: String, value: String) {
        try {
            val data: NSData = value.toNSData()
            val query = buildQuery(account)
            val updateAttrs = NSMutableDictionary().apply {
                setObject(data, kSecValueData.ns()!!)
            }
            val status = SecItemUpdate(query.toCFDict(), updateAttrs.toCFDict())
            if (status == errSecItemNotFound) {
                query.setObject(data, kSecValueData.ns()!!)
                SecItemAdd(query.toCFDict(), null)
            }
        } catch (_: Throwable) {}
    }

    private fun keychainRead(account: String): String? = try {
        keychainReadInternal(account)
    } catch (_: Throwable) {
        null
    }

    private fun keychainReadInternal(account: String): String? = memScoped {
        val boolTrue = interpretObjCPointerOrNull<NSNumber>(kCFBooleanTrue!!.rawValue)
            ?: return@memScoped null
        val query = buildQuery(account).apply {
            setObject(boolTrue, kSecReturnData.ns()!!)
            setObject(kSecMatchLimitOne.ns()!!, kSecMatchLimit.ns()!!)
        }
        val result = alloc<ObjCObjectVar<Any?>>()
        val status = SecItemCopyMatching(
            query.toCFDict(),
            result.ptr as CValuesRef<CFTypeRefVar>
        )
        if (status != errSecSuccess) return@memScoped null
        val nsData = result.value as? NSData ?: run {
            keychainDelete(account)
            return@memScoped null
        }
        NSString.create(nsData, NSUTF8StringEncoding)?.toString()
    }

    private fun keychainDelete(account: String) {
        try {
            SecItemDelete(buildQuery(account).toCFDict())
        } catch (_: Throwable) {}
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
