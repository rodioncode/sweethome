package com.jetbrains.kmpapp.ui

import androidx.compose.ui.graphics.Color

// Primary
val PrimaryGreen = Color(0xFF5B7C5A)
val PrimaryGreenLight = Color(0xFF8FAE8B)
val PrimaryGreenDark = Color(0xFF3D5C3C)
val PrimaryContainer = Color(0xFFEDF5ED)

// Secondary
val SecondaryPeach = Color(0xFFE8A87C)
val SecondaryPeachLight = Color(0xFFF2C9A8)

// Accent
val AccentTerracotta = Color(0xFFD4956B)

// Background & Surface
val BackgroundWarm = Color(0xFFFBF7F4)
val SurfaceWhite = Color(0xFFFFFFFF)
val SurfaceVariantCream = Color(0xFFF5EDE6)

// Text
val TextPrimary = Color(0xFF2C2C2C)
val TextSecondary = Color(0xFF7A7A7A)
val TextTertiary = Color(0xFFBDBDBD)

// Utility
val ErrorRed = Color(0xFFD4574E)
val DividerColor = Color(0xFFE8E0D8)
val OnPrimaryWhite = Color(0xFFFFFFFF)

// List colors — 7 presets
val ListColorCoral = Color(0xFFFF7043)
val ListColorSky = Color(0xFF42A5F5)
val ListColorMint = Color(0xFF66BB6A)
val ListColorLavender = Color(0xFFAB47BC)
val ListColorAmber = Color(0xFFFFA726)
val ListColorRose = Color(0xFFEC407A)
val ListColorSlate = Color(0xFF78909C)

// Priority colors
val PriorityHigh = Color(0xFFEF5350)
val PriorityMedium = Color(0xFFFFA726)
val PriorityLow = Color(0xFF66BB6A)

fun listColorForType(type: String): Color = when (type) {
    "shopping"      -> ListColorCoral
    "home_chores"   -> ListColorMint
    "general_todos" -> ListColorSky
    "study"         -> ListColorAmber
    "travel"        -> ListColorSky
    "wishlist"      -> ListColorRose
    "media"         -> ListColorLavender
    else            -> ListColorSlate
}

fun listEmojiForType(type: String): String = when (type) {
    "shopping"      -> "🛒"
    "home_chores"   -> "🏠"
    "general_todos" -> "✅"
    "study"         -> "📚"
    "travel"        -> "✈️"
    "wishlist"      -> "🎁"
    "media"         -> "🎬"
    else            -> "📋"
}

fun String.toComposeColor(fallback: Color = PrimaryGreen): Color {
    val hex = removePrefix("#")
    if (hex.length != 6) return fallback
    val r = hex.substring(0, 2).toIntOrNull(16) ?: return fallback
    val g = hex.substring(2, 4).toIntOrNull(16) ?: return fallback
    val b = hex.substring(4, 6).toIntOrNull(16) ?: return fallback
    return Color(red = r / 255f, green = g / 255f, blue = b / 255f)
}
