package com.jetbrains.kmpapp.screens.permissions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetbrains.kmpapp.ui.LocalCozyExtraColors
import com.jetbrains.kmpapp.ui.LocalCozyShapes
import com.jetbrains.kmpapp.ui.LocalCozySpacing
import com.jetbrains.kmpapp.ui.components.CozyCard
import com.jetbrains.kmpapp.ui.components.CozyTopBar
import com.jetbrains.kmpapp.ui.components.EmptyHero

enum class PermissionType(val emoji: String, val title: String, val why: List<String>) {
    PUSH(
        "🔔",
        "Уведомления",
        listOf("Напомнить о задачах", "Получать сообщения от семьи", "Не пропустить важное"),
    ),
    CALENDAR(
        "📅",
        "Календарь",
        listOf("Видеть события вместе с задачами", "Синхронизация с системой", "Планирование с семьёй"),
    ),
    LOCATION(
        "📍",
        "Геолокация",
        listOf("Напоминания у магазинов", "Делиться местом с семьёй", "Точные подсказки"),
    ),
    CONTACTS(
        "👤",
        "Контакты",
        listOf("Быстро пригласить близких", "Найти друзей в SweetHome", "Делиться списками"),
    ),
}

@Composable
fun PermissionAskScreen(
    type: PermissionType,
    onAllow: () -> Unit,
    onSkip: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalCozySpacing.current
    val shapes = LocalCozyShapes.current
    val extras = LocalCozyExtraColors.current

    val accentSoft = when (type) {
        PermissionType.PUSH -> MaterialTheme.colorScheme.primaryContainer
        PermissionType.CALENDAR -> extras.lavenderSoft
        PermissionType.LOCATION -> extras.coralSoft
        PermissionType.CONTACTS -> extras.ochreSoft
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        CozyTopBar(onBack = onBack)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(spacing.lg))
            EmptyHero(
                emoji = type.emoji,
                size = 160.dp,
                bgColor = accentSoft,
            )
            Spacer(Modifier.height(spacing.xxl))
            Text(
                text = type.title,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(spacing.xl))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(spacing.xs),
            ) {
                type.why.forEach { bullet ->
                    CozyCard(
                        modifier = Modifier.fillMaxWidth(),
                        background = MaterialTheme.colorScheme.surface,
                        bordered = true,
                        contentPadding = spacing.md,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                        ) {
                            Text(
                                text = "✓",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = bullet,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(spacing.xxxl))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.xxl)
                .padding(bottom = spacing.xxl),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .clip(shapes.button)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable(onClick = onAllow)
                    .padding(vertical = spacing.md),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Разрешить",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 44.dp)
                    .clip(shapes.button)
                    .clickable(onClick = onSkip)
                    .padding(vertical = spacing.sm),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Не сейчас",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}
