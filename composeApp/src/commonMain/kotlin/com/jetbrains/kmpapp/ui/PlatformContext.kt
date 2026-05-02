package com.jetbrains.kmpapp.ui

import androidx.compose.runtime.Composable

/** Возвращает платформенный контекст для нативных вызовов (Context на Android, null на iOS). */
@Composable
expect fun rememberPlatformContext(): Any?
