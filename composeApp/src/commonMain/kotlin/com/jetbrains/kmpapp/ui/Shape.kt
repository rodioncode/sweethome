package com.jetbrains.kmpapp.ui

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * SweetHome shape tokens.
 *
 * Strict scale — no other radii allowed:
 *   8 / 10 / 12 / 14 / 16 / 20 / 22 / Circle
 */
object SweetHomeShapes {
    val Small: Shape    = RoundedCornerShape(8.dp)
    val Pill: Shape     = RoundedCornerShape(10.dp)
    val Medium: Shape   = RoundedCornerShape(12.dp)
    val Button: Shape   = RoundedCornerShape(14.dp)
    val Card: Shape     = RoundedCornerShape(16.dp)
    val PillLarge: Shape = RoundedCornerShape(20.dp)
    val ChatInput: Shape = RoundedCornerShape(22.dp)
    val Circle: Shape   = CircleShape

    /** Backward-compat: previous `Chip` (24dp) maps to PillLarge (20dp) per new scale. */
    val Chip: Shape     = PillLarge

    /** Bottom sheet — top corners only. */
    val BottomSheet: Shape = RoundedCornerShape(
        topStart = 24.dp,
        topEnd = 24.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp,
    )

    /** Chat bubbles — asymmetric tail. */
    val ChatBubbleMine: Shape = RoundedCornerShape(
        topStart = 18.dp, topEnd = 18.dp,
        bottomStart = 18.dp, bottomEnd = 4.dp,
    )
    val ChatBubbleTheirs: Shape = RoundedCornerShape(
        topStart = 18.dp, topEnd = 18.dp,
        bottomStart = 4.dp, bottomEnd = 18.dp,
    )
}

/**
 * Material3 Shapes mapping. M3 only exposes `extraSmall…extraLarge` — we route:
 *   extraSmall → 8
 *   small      → 10
 *   medium     → 12
 *   large      → 16
 *   extraLarge → 22
 */
val SweetHomeM3Shapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small      = RoundedCornerShape(10.dp),
    medium     = RoundedCornerShape(12.dp),
    large      = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(22.dp),
)

/** Legacy alias — used by [Theme]. */
val AppShapes = SweetHomeM3Shapes

val LocalShapes = staticCompositionLocalOf { SweetHomeShapes }
