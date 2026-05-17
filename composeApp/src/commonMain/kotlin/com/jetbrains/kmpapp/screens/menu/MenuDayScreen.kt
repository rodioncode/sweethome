package com.jetbrains.kmpapp.screens.menu

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetbrains.kmpapp.ui.LocalCozyExtraColors
import com.jetbrains.kmpapp.ui.LocalCozySpacing
import com.jetbrains.kmpapp.ui.components.CozyCard
import com.jetbrains.kmpapp.ui.components.CozyTopBar

data class MenuDayState(
    val day: MenuDay? = null,
    val dayLabel: String = "",
)

sealed interface MenuDayIntent {
    data class OpenSlot(val slot: MealSlot) : MenuDayIntent
    data object Back : MenuDayIntent
}

@Composable
fun MenuDayScreen(
    state: MenuDayState,
    onIntent: (MenuDayIntent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val extras = LocalCozyExtraColors.current
    val spacing = LocalCozySpacing.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        CozyTopBar(
            onBack = {
                onIntent(MenuDayIntent.Back)
                onBack()
            },
            title = state.dayLabel.ifEmpty { "День" },
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = spacing.xxl)
                .padding(top = spacing.md)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            MealSlot.entries.forEach { slot ->
                val cell = state.day?.slots?.get(slot)

                CozyCard(
                    onClick = { onIntent(MenuDayIntent.OpenSlot(slot)) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(spacing.md),
                        verticalArrangement = Arrangement.spacedBy(spacing.xs),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                        ) {
                            Text(
                                text = slot.emoji,
                                fontSize = 20.sp,
                            )
                            Text(
                                text = slot.displayName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                        }

                        if (cell != null) {
                            Text(
                                text = "${cell.emoji} ${cell.name}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            when (cell) {
                                is MenuCell.Recipe -> Text(
                                    text = "от ${cell.author}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                is MenuCell.Out -> cell.note?.let {
                                    Text(
                                        text = it,
                                        fontSize = 12.sp,
                                        color = extras.lavender,
                                    )
                                }
                                is MenuCell.Delivery -> cell.price?.let {
                                    Text(
                                        text = "~$it ₽",
                                        fontSize = 12.sp,
                                        color = extras.coral,
                                    )
                                }
                                is MenuCell.Free -> {}
                            }
                        } else {
                            Text(
                                text = "+ Добавить",
                                fontSize = 14.sp,
                                color = extras.textTer,
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(spacing.xxl))
        }
    }
}
