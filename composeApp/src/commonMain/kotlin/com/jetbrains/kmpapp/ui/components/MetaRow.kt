package com.jetbrains.kmpapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MetaRow(
    icon: String,
    title: String,
    modifier: Modifier = Modifier,
    value: String? = null,
    valueAdornment: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    danger: Boolean = false,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(text = icon, fontSize = 18.sp, modifier = Modifier.width(22.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = if (danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        valueAdornment?.invoke()
        if (value != null) {
            Text(
                text = value,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
            )
        }
        if (onClick != null) {
            Spacer(Modifier.width(4.dp))
            Text(text = "›", color = MaterialTheme.colorScheme.outlineVariant, fontSize = 14.sp)
        }
    }
}
