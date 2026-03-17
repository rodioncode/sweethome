package com.jetbrains.kmpapp.di

import android.util.Log
import io.ktor.client.plugins.logging.Logger

actual fun createHttpLogger(): Logger = object : Logger {
    override fun log(message: String) {
        // Android Logcat limit ~4K chars per message
        val chunkSize = 4000
        var i = 0
        while (i < message.length) {
            val end = minOf(i + chunkSize, message.length)
            Log.d("Ktor", message.substring(i, end))
            i = end
        }
    }
}
