package com.jetbrains.kmpapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetbrains.kmpapp.ui.LocalCozyExtraColors

@Composable
fun EmptyHero(
    emoji: String,
    modifier: Modifier = Modifier,
    decor: List<String> = listOf("✨", "🌿", "☁️", "🍃"),
    bgColor: Color? = null,
    size: Dp = 140.dp,
) {
    val extras = LocalCozyExtraColors.current
    val center = bgColor ?: MaterialTheme.colorScheme.primaryContainer
    val edge = extras.surfaceSoft

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(colors = listOf(center, edge), radius = size.value * 0.8f)
            )
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
    ) {
        val pads = listOf(
            Alignment.TopStart    to (size * 0.18f to size * 0.22f),
            Alignment.TopEnd      to (size * 0.18f to size * 0.22f),
            Alignment.BottomStart to (size * 0.22f to size * 0.18f),
            Alignment.BottomEnd   to (size * 0.22f to size * 0.18f),
        )
        decor.take(4).forEachIndexed { i, e ->
            val (align, _) = pads[i]
            Box(
                modifier = Modifier.fillMaxSize().padding(size * 0.10f),
                contentAlignment = align,
            ) {
                Text(text = e, fontSize = (size.value * 0.11f).sp)
            }
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = emoji, fontSize = (size.value * 0.5f).sp)
        }
    }
}
