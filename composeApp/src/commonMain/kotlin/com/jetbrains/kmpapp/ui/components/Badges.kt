package com.jetbrains.kmpapp.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetbrains.kmpapp.ui.ErrorRed
import com.jetbrains.kmpapp.ui.OnPrimaryWhite
import com.jetbrains.kmpapp.ui.PrimaryGreen
import com.jetbrains.kmpapp.ui.PrimaryGreenDark
import com.jetbrains.kmpapp.ui.SecondaryPeach
import com.jetbrains.kmpapp.ui.SweetHomeShapes

@Composable
fun SweetHomeNotificationBadge(
    count: Int,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.defaultMinSize(minWidth = 24.dp, minHeight = 24.dp),
        shape = SweetHomeShapes.Chip,
        color = PrimaryGreen,
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 6.dp)) {
            Text(
                text = count.toString(),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = OnPrimaryWhite,
            )
        }
    }
}

@Composable
fun SweetHomeCounterBadge(
    count: Int,
    modifier: Modifier = Modifier,
    color: Color = SecondaryPeach,
) {
    Surface(
        modifier = modifier.defaultMinSize(minWidth = 24.dp, minHeight = 24.dp),
        shape = SweetHomeShapes.Chip,
        color = color,
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 6.dp)) {
            Text(
                text = count.toString(),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = OnPrimaryWhite,
            )
        }
    }
}

@Composable
fun SweetHomeNewBadge(
    modifier: Modifier = Modifier,
    text: String = "Новое",
) {
    Surface(
        modifier = modifier,
        shape = SweetHomeShapes.Chip,
        color = PrimaryGreenDark,
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = OnPrimaryWhite,
            )
        }
    }
}

@Composable
fun SweetHomeOnlineDot(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.size(12.dp),
        shape = CircleShape,
        color = ErrorRed,
    ) {}
}
