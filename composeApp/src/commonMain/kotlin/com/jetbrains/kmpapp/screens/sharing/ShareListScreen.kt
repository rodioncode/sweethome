package com.jetbrains.kmpapp.screens.sharing

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetbrains.kmpapp.ui.LocalCozyShapes
import com.jetbrains.kmpapp.ui.LocalCozySpacing
import com.jetbrains.kmpapp.ui.components.CozyAvatar
import com.jetbrains.kmpapp.ui.components.CozyCard
import com.jetbrains.kmpapp.ui.components.CozyChip
import com.jetbrains.kmpapp.ui.components.CozyTopBar
import com.jetbrains.kmpapp.ui.models.Palette

data class ShareListMember(
    val id: String,
    val name: String,
    val initial: String,
    val palette: Palette,
    val selected: Boolean,
    val role: String,
)

private val Roles = listOf("Просмотр", "Редактор")

@Composable
fun ShareListScreen(
    listTitle: String,
    members: List<ShareListMember>,
    onToggleMember: (String) -> Unit,
    onChangeRole: (String, String) -> Unit,
    onDone: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalCozySpacing.current
    val shapes = LocalCozyShapes.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        CozyTopBar(title = "Поделиться списком", onBack = onBack)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.xxl)
                .padding(top = spacing.md),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            Text(
                text = listTitle,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(spacing.xs))

            members.forEach { member ->
                CozyCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = spacing.md,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                        ) {
                            CozyAvatar(
                                letter = member.initial,
                                palette = member.palette,
                                size = 40.dp,
                            )
                            Text(
                                text = member.name,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f),
                            )
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (member.selected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surface
                                    )
                                    .border(
                                        1.dp,
                                        if (member.selected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outline,
                                        CircleShape,
                                    )
                                    .clickable { onToggleMember(member.id) },
                                contentAlignment = Alignment.Center,
                            ) {
                                if (member.selected) {
                                    Text(
                                        text = "✓",
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                        ) {
                            Roles.forEach { role ->
                                CozyChip(
                                    label = role,
                                    selected = member.role == role,
                                    onClick = { onChangeRole(member.id, role) },
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(spacing.xxxl))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.xxl)
                .padding(bottom = spacing.xxl)
                .heightIn(min = 52.dp)
                .clip(shapes.button)
                .background(MaterialTheme.colorScheme.primary)
                .clickable(onClick = onDone)
                .padding(vertical = spacing.md),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Готово",
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
