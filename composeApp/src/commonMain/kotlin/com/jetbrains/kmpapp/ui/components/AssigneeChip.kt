package com.jetbrains.kmpapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetbrains.kmpapp.data.groups.GroupMember
import com.jetbrains.kmpapp.ui.LocalCozyExtraColors
import com.jetbrains.kmpapp.ui.LocalCozyShapes
import com.jetbrains.kmpapp.ui.LocalCozySpacing

@Composable
fun memberColorFor(userId: String, isDark: Boolean = false): Color {
    val extras = LocalCozyExtraColors.current
    val palette = listOf(
        extras.coral,
        extras.lavenderSoft,
        extras.success,
        extras.lavender,
        extras.ochre,
        extras.coralSoft,
        extras.ochreSoft,
    )
    val idx = userId.hashCode().and(0x7fffffff) % palette.size
    return palette[idx]
}

/**
 * Compact pill showing the assignee (avatar + name) or a placeholder.
 * Tap to open assignee picker.
 */
@Composable
fun AssigneeChip(
    assignee: GroupMember?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = false,
) {
    if (assignee == null) {
        Surface(
            onClick = onClick,
            modifier = modifier.height(28.dp),
            shape = LocalCozyShapes.current.button,
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = LocalCozySpacing.current.xs),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(LocalCozySpacing.current.xxs),
            ) {
                Icon(
                    Icons.Outlined.Person,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Назначить",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    } else {
        val color = memberColorFor(assignee.userId, isDark)
        val name = assignee.displayName?.takeIf { it.isNotBlank() } ?: "Без имени"
        val initial = name.firstOrNull()?.uppercase() ?: "?"
        Surface(
            onClick = onClick,
            modifier = modifier.height(28.dp),
            shape = LocalCozyShapes.current.button,
            color = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Row(
                modifier = Modifier.padding(start = LocalCozySpacing.current.xxs, end = LocalCozySpacing.current.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(color),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = initial,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
                Spacer(Modifier.width(LocalCozySpacing.current.xs))
                Text(
                    text = name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

/** Single row in the assignee picker: avatar + name + optional role badge. */
@Composable
fun MemberPickerRow(
    member: GroupMember,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = false,
) {
    val color = memberColorFor(member.userId, isDark)
    val name = member.displayName?.takeIf { it.isNotBlank() } ?: "Без имени"
    val initial = name.firstOrNull()?.uppercase() ?: "?"

    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(horizontal = LocalCozySpacing.current.lg, vertical = LocalCozySpacing.current.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(LocalCozySpacing.current.sm),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = initial,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
        Text(
            text = name,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        if (selected) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Text("✓", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}
