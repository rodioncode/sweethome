package com.jetbrains.kmpapp.ui

import android.content.Context
import android.content.Intent

actual fun shareUrl(text: String, platformContext: Any?) {
    val ctx = platformContext as? Context ?: return
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    val chooser = Intent.createChooser(intent, null).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    ctx.startActivity(chooser)
}
