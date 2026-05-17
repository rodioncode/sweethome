package com.jetbrains.kmpapp.ui

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ─────────────────────────────────────────────────────────────────────
// Spacing scale.
// 4 / 8 / 12 / 14 / 16 / 20 / 24 / 32 / 40 dp
// ─────────────────────────────────────────────────────────────────────

@Immutable
data class CozySpacing(
    val xxs: Dp = 4.dp,
    val xs:  Dp = 8.dp,
    val sm:  Dp = 12.dp,
    val md:  Dp = 14.dp,
    val lg:  Dp = 16.dp,
    val xl:  Dp = 20.dp,
    val xxl: Dp = 24.dp,
    val xxxl:Dp = 32.dp,
    val huge:Dp = 40.dp,

    val screenH: Dp = 24.dp,
    val cardP:   Dp = 14.dp,
    val cardPL:  Dp = 16.dp,
    val rowGap:  Dp = 8.dp,
    val sectionGap: Dp = 20.dp,
)

val cozySpacing = CozySpacing()
val LocalCozySpacing = staticCompositionLocalOf { cozySpacing }
