package com.jetbrains.kmpapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CozyChip(
    label: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    accent: Color = MaterialTheme.colorScheme.primary,
    accentContainer: Color = MaterialTheme.colorScheme.primaryContainer,
    dismissible: Boolean = false,
    onDismiss: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    val bg = if (selected) accentContainer else MaterialTheme.colorScheme.surface
    val fg = if (selected) accent else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .background(bg, RoundedCornerShape(14.dp))
            .then(
                if (!selected) Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
                else Modifier
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            text = label,
            color = fg,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
        )
        if (dismissible && selected) {
            Spacer(Modifier.width(4.dp))
            Text(
                text = "×",
                color = fg,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = if (onDismiss != null) Modifier.clickable(onClick = onDismiss) else Modifier,
            )
        }
    }
}
