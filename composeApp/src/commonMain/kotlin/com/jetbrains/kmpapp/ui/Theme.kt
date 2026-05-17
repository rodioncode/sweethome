package com.jetbrains.kmpapp.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun CozyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) cozyDarkScheme else cozyLightScheme
    val extras      = if (darkTheme) cozyDarkExtras else cozyLightExtras

    CompositionLocalProvider(
        LocalCozyExtraColors provides extras,
        LocalCozySpacing     provides cozySpacing,
        LocalCozyElevation   provides cozyElevation,
        LocalCozyShapes      provides cozyShapesSpec,
        LocalIsKidMode       provides false,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = cozyTypography,
            shapes      = cozyShapes,
            content     = content,
        )
    }
}

object CozyThemeAccessors {
    val extras    @Composable get() = LocalCozyExtraColors.current
    val spacing   @Composable get() = LocalCozySpacing.current
    val elevation @Composable get() = LocalCozyElevation.current
    val shapes    @Composable get() = LocalCozyShapes.current
}
