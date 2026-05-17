package com.jetbrains.kmpapp.ui

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ─────────────────────────────────────────────────────────────────────
// Typography.
// Adult mode: Inter Display (M3-compatible).
// Kid mode: Fredoka — bundled separately, see [LocalKidTypography].
// Until fonts are bundled, FontFamily.Default is used.
// ─────────────────────────────────────────────────────────────────────

private val InterDisplay = FontFamily.Default  // TODO(art): swap to Inter Tight when font assets added
private val Fredoka      = FontFamily.Default  // TODO(art): swap to Fredoka

internal val cozyTypography = Typography(
    displayLarge   = TextStyle(fontFamily = InterDisplay, fontWeight = FontWeight.Bold,    fontSize = 40.sp, lineHeight = 44.sp, letterSpacing = (-1).sp),
    displayMedium  = TextStyle(fontFamily = InterDisplay, fontWeight = FontWeight.Bold,    fontSize = 32.sp, lineHeight = 36.sp, letterSpacing = (-0.5).sp),
    displaySmall   = TextStyle(fontFamily = InterDisplay, fontWeight = FontWeight.Bold,    fontSize = 26.sp, lineHeight = 30.sp, letterSpacing = (-0.5).sp),

    headlineLarge  = TextStyle(fontFamily = InterDisplay, fontWeight = FontWeight.Bold,    fontSize = 28.sp, lineHeight = 32.sp, letterSpacing = (-0.5).sp),
    headlineMedium = TextStyle(fontFamily = InterDisplay, fontWeight = FontWeight.Bold,    fontSize = 22.sp, lineHeight = 26.sp),
    headlineSmall  = TextStyle(fontFamily = InterDisplay, fontWeight = FontWeight.Bold,    fontSize = 18.sp, lineHeight = 22.sp),

    titleLarge     = TextStyle(fontFamily = InterDisplay, fontWeight = FontWeight.Bold,    fontSize = 17.sp, lineHeight = 22.sp),
    titleMedium    = TextStyle(fontFamily = InterDisplay, fontWeight = FontWeight.SemiBold,fontSize = 15.sp, lineHeight = 20.sp),
    titleSmall     = TextStyle(fontFamily = InterDisplay, fontWeight = FontWeight.SemiBold,fontSize = 13.sp, lineHeight = 18.sp),

    bodyLarge      = TextStyle(fontFamily = InterDisplay, fontWeight = FontWeight.Normal,  fontSize = 15.sp, lineHeight = 22.sp),
    bodyMedium     = TextStyle(fontFamily = InterDisplay, fontWeight = FontWeight.Normal,  fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall      = TextStyle(fontFamily = InterDisplay, fontWeight = FontWeight.Normal,  fontSize = 13.sp, lineHeight = 18.sp),

    labelLarge     = TextStyle(fontFamily = InterDisplay, fontWeight = FontWeight.SemiBold,fontSize = 13.sp, lineHeight = 16.sp),
    labelMedium    = TextStyle(fontFamily = InterDisplay, fontWeight = FontWeight.SemiBold,fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall     = TextStyle(fontFamily = InterDisplay, fontWeight = FontWeight.Bold,    fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 1.2.sp),
)

// ─────────────────────────────────────────────────────────────────────
// Kid mode typography — Fredoka, slightly larger, less letter-spacing.
// ─────────────────────────────────────────────────────────────────────

@Immutable
data class KidTypography(
    val display:   TextStyle,
    val heading:   TextStyle,
    val title:     TextStyle,
    val body:      TextStyle,
    val caption:   TextStyle,
    val button:    TextStyle,
)

val kidTypography = KidTypography(
    display = TextStyle(fontFamily = Fredoka, fontWeight = FontWeight.Bold,     fontSize = 40.sp, lineHeight = 44.sp, letterSpacing = (-1).sp),
    heading = TextStyle(fontFamily = Fredoka, fontWeight = FontWeight.Bold,     fontSize = 26.sp, lineHeight = 30.sp, letterSpacing = (-0.5).sp),
    title   = TextStyle(fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 22.sp),
    body    = TextStyle(fontFamily = Fredoka, fontWeight = FontWeight.Medium,   fontSize = 15.sp, lineHeight = 22.sp),
    caption = TextStyle(fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 16.sp),
    button  = TextStyle(fontFamily = Fredoka, fontWeight = FontWeight.Bold,     fontSize = 18.sp, lineHeight = 22.sp, letterSpacing = 0.2.sp),
)

val LocalKidTypography = staticCompositionLocalOf { kidTypography }
