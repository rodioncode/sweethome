package com.jetbrains.kmpapp.ui

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Font: Inter (using Default which maps to Roboto/SF Pro, metrically close)
private val AppFontFamily = FontFamily.Default

object SweetHomeTypography {
    val Heading1 = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
    )
    val Heading2 = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
    )
    val Heading3 = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
    )
    val Subtitle = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
    )
    val Body = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
    )
    val BodySmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
    )
    val Caption = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
    )
}

val AppTypography = Typography(
    headlineLarge = SweetHomeTypography.Heading1,
    headlineSmall = SweetHomeTypography.Heading2,
    titleLarge = SweetHomeTypography.Heading3,
    titleMedium = SweetHomeTypography.Subtitle,
    bodyLarge = SweetHomeTypography.Body,
    bodyMedium = SweetHomeTypography.BodySmall,
    labelSmall = SweetHomeTypography.Caption,
)
