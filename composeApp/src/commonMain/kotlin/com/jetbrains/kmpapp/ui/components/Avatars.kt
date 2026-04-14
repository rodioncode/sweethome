package com.jetbrains.kmpapp.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetbrains.kmpapp.ui.OnPrimaryWhite
import com.jetbrains.kmpapp.ui.PrimaryGreen

enum class AvatarSize(val sizeDp: Dp, val fontSize: TextUnit) {
    Large(56.dp, 20.sp),
    Medium(44.dp, 16.sp),
    Small(36.dp, 13.sp),
    XSmall(28.dp, 10.sp),
}

@Composable
fun SweetHomeAvatar(
    initials: String,
    modifier: Modifier = Modifier,
    size: AvatarSize = AvatarSize.Medium,
    backgroundColor: Color = PrimaryGreen,
    contentColor: Color = OnPrimaryWhite,
) {
    Surface(
        modifier = modifier.size(size.sizeDp),
        shape = CircleShape,
        color = backgroundColor,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = initials.take(2).uppercase(),
                fontSize = size.fontSize,
                fontWeight = FontWeight.SemiBold,
                color = contentColor,
            )
        }
    }
}
