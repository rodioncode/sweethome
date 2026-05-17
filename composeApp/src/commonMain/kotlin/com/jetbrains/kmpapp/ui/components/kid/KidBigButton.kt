package com.jetbrains.kmpapp.ui.components.kid

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetbrains.kmpapp.ui.KidColors
import com.jetbrains.kmpapp.ui.LocalKidColors
import com.jetbrains.kmpapp.ui.LocalKidTypography

enum class KidAccent { SUN, SKY, APPLE, GRASS, CANDY }
enum class KidButtonSize { SM, LG, XL }

@Composable
fun KidBigButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: KidAccent = KidAccent.GRASS,
    size: KidButtonSize = KidButtonSize.LG,
    icon: String? = null,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colors = LocalKidColors.current
    val (bg, deep) = accent.colors(colors)
    val height = when (size) {
        KidButtonSize.SM -> 52.dp
        KidButtonSize.LG -> 64.dp
        KidButtonSize.XL -> 76.dp
    }
    val shape = RoundedCornerShape(22.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height + 4.dp)
            .clip(shape)
            .drawBehind {
                val r = 22.dp.toPx()
                drawRoundRect(
                    color = deep,
                    topLeft = Offset(0f, this.size.height - height.toPx()),
                    size = Size(this.size.width, height.toPx()),
                    cornerRadius = CornerRadius(r, r),
                )
            }
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.TopCenter,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(shape)
                .background(bg)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Text(text = icon, fontSize = when (size) {
                        KidButtonSize.SM -> 20.sp
                        KidButtonSize.LG -> 26.sp
                        KidButtonSize.XL -> 30.sp
                    }, color = Color.White)
                    Spacer(Modifier.width(10.dp))
                }
                CompositionLocalProvider(LocalContentColor provides Color.White) { content() }
            }
        }
    }
}

private fun KidAccent.colors(c: KidColors): Pair<Color, Color> = when (this) {
    KidAccent.SUN   -> c.sun to c.sunDeep
    KidAccent.SKY   -> c.sky to c.skyDeep
    KidAccent.APPLE -> c.apple to c.appleDeep
    KidAccent.GRASS -> c.grass to c.grassDeep
    KidAccent.CANDY -> c.candy to c.candyDeep
}
