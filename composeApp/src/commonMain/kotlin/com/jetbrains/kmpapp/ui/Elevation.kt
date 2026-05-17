package com.jetbrains.kmpapp.ui

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class CozyElevation(
    val flat:       Dp = 0.dp,
    val card:       Dp = 2.dp,
    val cardLifted: Dp = 6.dp,
    val fab:        Dp = 8.dp,
    val modal:      Dp = 12.dp,
)

val cozyElevation = CozyElevation()
val LocalCozyElevation = staticCompositionLocalOf { cozyElevation }
