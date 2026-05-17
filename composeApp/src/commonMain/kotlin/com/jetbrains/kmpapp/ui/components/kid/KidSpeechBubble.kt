package com.jetbrains.kmpapp.ui.components.kid

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jetbrains.kmpapp.ui.LocalKidColors
import com.jetbrains.kmpapp.ui.LocalKidTypography

enum class KidBubbleColor { PAPER, WARM, SUN }

@Composable
fun KidSpeechBubble(
    modifier: Modifier = Modifier,
    color: KidBubbleColor = KidBubbleColor.PAPER,
    tail: Boolean = true,
    content: @Composable () -> Unit,
) {
    val c = LocalKidColors.current
    val type = LocalKidTypography.current
    val bg = when (color) {
        KidBubbleColor.PAPER -> c.paper
        KidBubbleColor.WARM  -> c.warm
        KidBubbleColor.SUN   -> c.sun
    }
    val radius = 22.dp

    Box(modifier = modifier.widthIn(max = 280.dp)) {
        Box(
            modifier = Modifier
                .background(bg, RoundedCornerShape(radius))
                .border(2.dp, c.line, RoundedCornerShape(radius))
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            CompositionLocalProvider(
                LocalContentColor provides c.ink,
                LocalTextStyle provides type.body,
            ) { content() }
        }
    }
}
