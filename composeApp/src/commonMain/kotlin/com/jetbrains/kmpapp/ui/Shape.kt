package com.jetbrains.kmpapp.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

// ─────────────────────────────────────────────────────────────────────
// Material 3 default Shapes mapped to Cozy radii.
// Buttons → 14, cards → 18, large cards → 22.
// ─────────────────────────────────────────────────────────────────────

internal val cozyShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small      = RoundedCornerShape(12.dp),
    medium     = RoundedCornerShape(14.dp),
    large      = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(22.dp),
)

// ─────────────────────────────────────────────────────────────────────
// Extra named shapes — for cases where M3 Shapes mapping isn't enough.
// ─────────────────────────────────────────────────────────────────────

@Immutable
data class CozyShapes(
    val chip:        RoundedCornerShape = RoundedCornerShape(14.dp),
    val pill:        RoundedCornerShape = RoundedCornerShape(999.dp),
    val card:        RoundedCornerShape = RoundedCornerShape(18.dp),
    val cardLarge:   RoundedCornerShape = RoundedCornerShape(22.dp),
    val sheet:       RoundedCornerShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
    val button:      RoundedCornerShape = RoundedCornerShape(14.dp),
    val avatarTile:  RoundedCornerShape = RoundedCornerShape(14.dp),
    val chatBubbleMine: RoundedCornerShape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp),
    val chatBubbleTheirs: RoundedCornerShape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp),
)

val cozyShapesSpec = CozyShapes()
val LocalCozyShapes = staticCompositionLocalOf { cozyShapesSpec }

// ─────────────────────────────────────────────────────────────────────
// Kid mode shapes — bigger radii, chunkier feel.
// ─────────────────────────────────────────────────────────────────────

@Immutable
data class KidShapes(
    val pill:     RoundedCornerShape = RoundedCornerShape(999.dp),
    val card:     RoundedCornerShape = RoundedCornerShape(22.dp),
    val sticker:  RoundedCornerShape = RoundedCornerShape(28.dp),
    val button:   RoundedCornerShape = RoundedCornerShape(22.dp),
    val sceneTile:RoundedCornerShape = RoundedCornerShape(50),
)

val kidShapes = KidShapes()
val LocalKidShapes = staticCompositionLocalOf { kidShapes }
