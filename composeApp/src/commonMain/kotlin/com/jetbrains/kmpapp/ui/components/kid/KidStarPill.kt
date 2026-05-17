package com.jetbrains.kmpapp.ui.components.kid

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetbrains.kmpapp.ui.LocalKidColors

@Composable
fun KidStarPill(
    count: Int,
    modifier: Modifier = Modifier,
    big: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val c = LocalKidColors.current
    val starSize = if (big) 22.sp else 18.sp
    val countSize = if (big) 20.sp else 16.sp
    val hPad = if (big) 16.dp else 12.dp
    val vPad = if (big) 10.dp else 6.dp
    val shape = RoundedCornerShape(999.dp)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(c.sun, shape)
            .border(2.dp, c.sunDeep, shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = hPad, vertical = vPad),
    ) {
        Text(text = "⭐", fontSize = starSize)
        Spacer(Modifier.width(6.dp))
        Text(
            text = count.toString(),
            fontSize = countSize,
            color = c.ink,
            fontWeight = FontWeight.Bold,
        )
    }
}
