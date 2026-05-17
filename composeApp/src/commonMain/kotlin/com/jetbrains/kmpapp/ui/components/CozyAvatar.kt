package com.jetbrains.kmpapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetbrains.kmpapp.ui.LocalCozyExtraColors
import com.jetbrains.kmpapp.ui.models.Palette

@Composable
fun CozyAvatar(
    letter: String,
    palette: Palette,
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
) {
    CozyAvatar(letter = letter, color = palette.resolve(), modifier = modifier, size = size)
}

@Composable
fun CozyAvatar(
    letter: String,
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
) {
    Box(
        modifier = modifier.size(size).clip(CircleShape).background(color),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = letter,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.4f).sp,
        )
    }
}

@Composable
fun Palette.resolve(): Color = when (this) {
    Palette.PRIMARY  -> MaterialTheme.colorScheme.primary
    Palette.CORAL    -> LocalCozyExtraColors.current.coral
    Palette.OCHRE    -> LocalCozyExtraColors.current.ochre
    Palette.LAVENDER -> LocalCozyExtraColors.current.lavender
}

@Composable
fun Palette.resolveContainer(): Color = when (this) {
    Palette.PRIMARY  -> MaterialTheme.colorScheme.primaryContainer
    Palette.CORAL    -> LocalCozyExtraColors.current.coralSoft
    Palette.OCHRE    -> LocalCozyExtraColors.current.ochreSoft
    Palette.LAVENDER -> LocalCozyExtraColors.current.lavenderSoft
}
