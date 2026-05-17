package com.jetbrains.kmpapp.ui.components.kid

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jetbrains.kmpapp.ui.LocalKidColors

@Composable
fun KidCard(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 16.dp,
    radius: Dp = 24.dp,
    tilt: Float = 0f,
    backgroundColor: Color? = null,
    borderColor: Color? = null,
    content: @Composable () -> Unit,
) {
    val colors = LocalKidColors.current
    val bg = backgroundColor ?: colors.paper
    val bd = borderColor ?: colors.line
    val shape = RoundedCornerShape(radius)

    Box(
        modifier = modifier
            .then(if (tilt != 0f) Modifier.graphicsLayer { rotationZ = tilt } else Modifier)
            .background(bg, shape)
            .border(2.dp, bd, shape)
            .padding(contentPadding),
    ) {
        content()
    }
}
