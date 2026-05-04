package com.jetbrains.kmpapp.ui

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * SweetHome typography — 12 levels.
 *
 * Font: FontFamily.Default (system Roboto on Android, SF on iOS).
 * Weights: 400 / 600 / 700 only — never 500/800/900.
 */

private val Sans = FontFamily.Default

// ── Display & headings ────────────────────────────────────────────

val Display = TextStyle(
    fontFamily = Sans,
    fontSize = 36.sp,
    fontWeight = FontWeight.Bold,
    lineHeight = 44.sp,
    letterSpacing = 8.sp,
)

val Heading1 = TextStyle(
    fontFamily = Sans,
    fontSize = 28.sp,
    fontWeight = FontWeight.Bold,
    lineHeight = 34.sp,
)

val Heading2 = TextStyle(
    fontFamily = Sans,
    fontSize = 22.sp,
    fontWeight = FontWeight.Bold,
    lineHeight = 28.sp,
)

val Heading3 = TextStyle(
    fontFamily = Sans,
    fontSize = 20.sp,
    fontWeight = FontWeight.Bold,
    lineHeight = 26.sp,
)

val TitleLarge = TextStyle(
    fontFamily = Sans,
    fontSize = 18.sp,
    fontWeight = FontWeight.Bold,
    lineHeight = 24.sp,
)

val Subtitle = TextStyle(
    fontFamily = Sans,
    fontSize = 17.sp,
    fontWeight = FontWeight.Bold,
    lineHeight = 22.sp,
)

val TitleMedium = TextStyle(
    fontFamily = Sans,
    fontSize = 16.sp,
    fontWeight = FontWeight.Bold,
    lineHeight = 22.sp,
)

// ── Body ──────────────────────────────────────────────────────────

val Body = TextStyle(
    fontFamily = Sans,
    fontSize = 15.sp,
    fontWeight = FontWeight.Normal,
    lineHeight = 22.sp,
)

val BodyMedium = TextStyle(
    fontFamily = Sans,
    fontSize = 14.sp,
    fontWeight = FontWeight.Normal,
    lineHeight = 20.sp,
)

val BodySmall = TextStyle(
    fontFamily = Sans,
    fontSize = 13.sp,
    fontWeight = FontWeight.Normal,
    lineHeight = 18.sp,
)

val Caption = TextStyle(
    fontFamily = Sans,
    fontSize = 12.sp,
    fontWeight = FontWeight.Normal,
    lineHeight = 16.sp,
)

val CaptionSmall = TextStyle(
    fontFamily = Sans,
    fontSize = 11.sp,
    fontWeight = FontWeight.Bold,
    lineHeight = 14.sp,
    letterSpacing = 0.5.sp,
)

val Micro = TextStyle(
    fontFamily = Sans,
    fontSize = 10.sp,
    fontWeight = FontWeight.Normal,
    lineHeight = 14.sp,
)

/**
 * Backward-compat namespace for callers that read styles via [SweetHomeTypography.Heading1] etc.
 * Prefer the top-level vals in new code.
 */
object SweetHomeTypography {
    val Display = com.jetbrains.kmpapp.ui.Display
    val Heading1 = com.jetbrains.kmpapp.ui.Heading1
    val Heading2 = com.jetbrains.kmpapp.ui.Heading2
    val Heading3 = com.jetbrains.kmpapp.ui.Heading3
    val TitleLarge = com.jetbrains.kmpapp.ui.TitleLarge
    val Subtitle = com.jetbrains.kmpapp.ui.Subtitle
    val TitleMedium = com.jetbrains.kmpapp.ui.TitleMedium
    val Body = com.jetbrains.kmpapp.ui.Body
    val BodyMedium = com.jetbrains.kmpapp.ui.BodyMedium
    val BodySmall = com.jetbrains.kmpapp.ui.BodySmall
    val Caption = com.jetbrains.kmpapp.ui.Caption
    val CaptionSmall = com.jetbrains.kmpapp.ui.CaptionSmall
    val Micro = com.jetbrains.kmpapp.ui.Micro
}

// ── M3 Typography mapping ─────────────────────────────────────────

val SweetHomeM3Typography = Typography(
    displayLarge   = Display,
    displayMedium  = Heading1,
    displaySmall   = Heading2,

    headlineLarge  = Heading1,
    headlineMedium = Heading2,
    headlineSmall  = Heading3,

    titleLarge     = TitleLarge,
    titleMedium    = TitleMedium,
    titleSmall     = Subtitle,

    bodyLarge      = Body,
    bodyMedium     = BodyMedium,
    bodySmall      = BodySmall,

    labelLarge     = TitleMedium.copy(fontSize = 14.sp), // buttons
    labelMedium    = Caption,
    labelSmall     = CaptionSmall,
)

/** Legacy alias — used by [Theme]. */
val AppTypography = SweetHomeM3Typography
