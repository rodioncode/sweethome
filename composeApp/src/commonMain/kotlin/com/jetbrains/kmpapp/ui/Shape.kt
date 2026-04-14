package com.jetbrains.kmpapp.ui

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

object SweetHomeShapes {
    val Small = RoundedCornerShape(4.dp)
    val Medium = RoundedCornerShape(8.dp)
    val Card = RoundedCornerShape(12.dp)
    val Chip = RoundedCornerShape(24.dp)
    val Circle = CircleShape
}

val AppShapes = Shapes(
    small = SweetHomeShapes.Small,
    medium = SweetHomeShapes.Medium,
    large = SweetHomeShapes.Card,
)
