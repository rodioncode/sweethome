package com.jetbrains.kmpapp.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun KidTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalKidColors     provides kidColors,
        LocalKidTypography provides kidTypography,
        LocalKidShapes     provides kidShapes,
        LocalIsKidMode     provides true,
        content            = content,
    )
}
