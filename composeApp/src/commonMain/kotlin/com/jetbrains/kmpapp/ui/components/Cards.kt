package com.jetbrains.kmpapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetbrains.kmpapp.ui.PrimaryGreen
import com.jetbrains.kmpapp.ui.SurfaceVariantCream
import com.jetbrains.kmpapp.ui.SurfaceWhite
import com.jetbrains.kmpapp.ui.SweetHomeShapes
import com.jetbrains.kmpapp.ui.SweetHomeSpacing
import com.jetbrains.kmpapp.ui.TextPrimary
import com.jetbrains.kmpapp.ui.TextSecondary

@Composable
fun SweetHomeListCard(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    categoryLabel: String? = null,
    progressText: String? = null,
    progress: Float? = null,
    updatedText: String? = null,
    dotColor: Color = PrimaryGreen,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = SweetHomeShapes.Card,
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(SweetHomeSpacing.md)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(dotColor),
                )
                Spacer(Modifier.width(SweetHomeSpacing.xs))
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (categoryLabel != null) {
                    Text(
                        text = categoryLabel,
                        fontSize = 11.sp,
                        color = TextSecondary,
                    )
                }
            }

            if (progressText != null) {
                Spacer(Modifier.height(SweetHomeSpacing.xs))
                Text(text = progressText, fontSize = 13.sp, color = TextSecondary)
            }

            if (progress != null) {
                Spacer(Modifier.height(SweetHomeSpacing.xs))
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(SweetHomeShapes.Small),
                    color = PrimaryGreen,
                    trackColor = SurfaceVariantCream,
                    strokeCap = StrokeCap.Round,
                )
            }

            if (updatedText != null) {
                Spacer(Modifier.height(SweetHomeSpacing.xs))
                Text(text = updatedText, fontSize = 11.sp, color = TextSecondary.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
fun SweetHomeTaskItem(
    text: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = SweetHomeShapes.Card,
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isChecked) 0.5.dp else 2.dp,
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = SweetHomeSpacing.md, vertical = SweetHomeSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = PrimaryGreen,
                    uncheckedColor = PrimaryGreen,
                    checkmarkColor = SurfaceWhite,
                ),
            )
            Spacer(Modifier.width(SweetHomeSpacing.xs))
            Column {
                Text(
                    text = text,
                    fontSize = 15.sp,
                    color = if (isChecked) TextSecondary else TextPrimary,
                    textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitle != null && !isChecked) {
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = TextSecondary,
                    )
                }
            }
        }
    }
}
