package com.jetbrains.kmpapp.ui.components.kid

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jetbrains.kmpapp.ui.LocalKidColors
import com.jetbrains.kmpapp.ui.LocalKidShapes

@Composable
fun KidSticker(
    modifier: Modifier = Modifier,
    accent: KidAccent = KidAccent.SUN,
    tilt: Float = 0f,
    padding: Dp = 10.dp,
    radius: Dp = 14.dp,
    content: @Composable () -> Unit,
) {
    val c = LocalKidColors.current
    val (bg, border) = when (accent) {
        KidAccent.SUN    -> c.sun    to c.sunDeep
        KidAccent.SKY    -> c.sky    to c.skyDeep
        KidAccent.APPLE  -> c.apple  to c.appleDeep
        KidAccent.GRASS  -> c.grass  to c.grassDeep
        KidAccent.CANDY  -> c.candy  to c.candyDeep
    }
    val shape = RoundedCornerShape(radius)
    Box(
        modifier = modifier
            .graphicsLayer(rotationZ = tilt)
            .clip(shape)
            .background(bg)
            .border(2.dp, border, shape)
            .padding(horizontal = padding, vertical = padding * 0.5f),
        contentAlignment = Alignment.Center,
    ) { content() }
}
