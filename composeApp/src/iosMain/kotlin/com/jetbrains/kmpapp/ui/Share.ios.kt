package com.jetbrains.kmpapp.ui

import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

actual fun shareUrl(text: String, platformContext: Any?) {
    val items = listOf(text)
    val controller = UIActivityViewController(activityItems = items, applicationActivities = null)
    val rootVc = UIApplication.sharedApplication.keyWindow?.rootViewController ?: return
    rootVc.presentViewController(controller, animated = true, completion = null)
}
