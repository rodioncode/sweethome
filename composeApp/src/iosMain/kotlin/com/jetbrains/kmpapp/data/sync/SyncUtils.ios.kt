package com.jetbrains.kmpapp.data.sync

import platform.Foundation.NSDate
import platform.Foundation.NSUUID
import platform.Foundation.timeIntervalSince1970
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.localeWithLocaleIdentifier

actual fun nowIso(): String {
    val formatter = NSDateFormatter().apply {
        dateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'"
        locale = NSLocale.localeWithLocaleIdentifier("en_US_POSIX")
    }
    return formatter.stringFromDate(NSDate())
}

actual fun generateId(): String = NSUUID().UUIDString()
