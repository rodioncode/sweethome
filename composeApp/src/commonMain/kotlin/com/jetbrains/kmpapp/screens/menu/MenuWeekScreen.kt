package com.jetbrains.kmpapp.screens.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetbrains.kmpapp.ui.LocalCozyExtraColors
import com.jetbrains.kmpapp.ui.LocalCozyShapes
import com.jetbrains.kmpapp.ui.LocalCozySpacing
import com.jetbrains.kmpapp.ui.CozyExtraColors
import com.jetbrains.kmpapp.ui.components.CozyChip
import com.jetbrains.kmpapp.ui.components.CozyTopBar

@Composable
fun MenuWeekScreen(
    state: MenuWeekState,
    onIntent: (MenuWeekIntent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val extras = LocalCozyExtraColors.current
    val spacing = LocalCozySpacing.current
    val shapes = LocalCozyShapes.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        CozyTopBar(onBack = onBack, title = "Меню недели")

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.xxl, vertical = spacing.xxs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.xs),
        ) {
            CircleIconButton(emoji = "‹", onClick = { onIntent(MenuWeekIntent.PrevWeek) })
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "ЭТА НЕДЕЛЯ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "4 – 10 мая",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            CircleIconButton(emoji = "›", onClick = { onIntent(MenuWeekIntent.NextWeek) })
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.xxl, vertical = spacing.md)
                .clip(shapes.card)
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            extras.ochreSoft,
                        )
                    )
                )
                .padding(spacing.md),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🍽", fontSize = 32.sp)
                Spacer(Modifier.width(spacing.sm))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${state.recipesPlanned} блюд · ${state.emptySlots} пустых слотов",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = buildString {
                            if (state.outsidePlanned > 0) append("${state.outsidePlanned} раз вне · ")
                            if (state.deliveriesPlanned > 0) append("${state.deliveriesPlanned} доставка · ")
                            append("Повар недели: 👩‍🍳")
                        },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .padding(horizontal = spacing.xxl)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            FilterChip("Все", MenuFilter.ALL, state.filter) { onIntent(MenuWeekIntent.SetFilter(it)) }
            FilterChip("📖 По рецепту", MenuFilter.RECIPE, state.filter) { onIntent(MenuWeekIntent.SetFilter(it)) }
            FilterChip("🍣 Вне дома", MenuFilter.OUT, state.filter) { onIntent(MenuWeekIntent.SetFilter(it)) }
            FilterChip("🍕 Доставка", MenuFilter.DELIVERY, state.filter) { onIntent(MenuWeekIntent.SetFilter(it)) }
            FilterChip("+ Пустые", MenuFilter.EMPTY, state.filter) { onIntent(MenuWeekIntent.SetFilter(it)) }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = spacing.xxl)
                .padding(top = spacing.md)
                .verticalScroll(rememberScrollState()),
        ) {
            state.week?.days?.forEach { day ->
                DayRow(
                    day = day,
                    filter = state.filter,
                    onDayTap = { onIntent(MenuWeekIntent.OpenDay(day.date.toString())) },
                    onSlotTap = { slot -> onIntent(MenuWeekIntent.OpenSlot(day.date.toString(), slot)) },
                )
                Spacer(Modifier.height(spacing.md))
            }
            Spacer(Modifier.height(110.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant)
                .padding(horizontal = spacing.xxl, vertical = spacing.sm),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(shapes.button)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable { onIntent(MenuWeekIntent.GenerateShoppingList) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "🛒 Собрать в покупки · ${state.totalIngredients} ингредиентов",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

@Composable
private fun DayRow(
    day: MenuDay,
    filter: MenuFilter,
    onDayTap: () -> Unit,
    onSlotTap: (MealSlot) -> Unit,
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = day.label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "День →",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onDayTap),
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            MealSlot.entries.forEach { slot ->
                val cell = day.slots[slot]
                val visible = filter.matches(cell)
                SlotCell(
                    slot = slot,
                    cell = cell,
                    dimmed = !visible,
                    onClick = { onSlotTap(slot) },
                )
            }
        }
    }
}

@Composable
private fun SlotCell(slot: MealSlot, cell: MenuCell?, dimmed: Boolean, onClick: () -> Unit) {
    val extras = LocalCozyExtraColors.current
    val shapes = LocalCozyShapes.current
    val (bg, accent, border) = cellKindStyle(cell, extras)
    val width = if (cell == null) 110.dp else 130.dp

    Column(
        modifier = Modifier
            .width(width)
            .clip(shapes.button)
            .background(bg)
            .border(1.dp, border, shapes.button)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .then(if (dimmed) Modifier.alpha(0.35f) else Modifier),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "${slot.emoji} ${slot.displayName.uppercase()}",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (cell == null) extras.textTer else accent,
            letterSpacing = 0.5.sp,
        )
        if (cell == null) {
            Text(text = "+ Добавить", fontSize = 13.sp, color = extras.textTer)
        } else {
            Text(
                text = "${cell.emoji} ${cell.name}",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            when (cell) {
                is MenuCell.Recipe -> Text(
                    text = "от ${cell.author}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                is MenuCell.Out -> cell.note?.let {
                    Text(text = it, fontSize = 10.sp, color = accent)
                }
                is MenuCell.Delivery -> cell.price?.let {
                    Text(text = "~$it ₽", fontSize = 10.sp, color = accent)
                }
                is MenuCell.Free -> {}
            }
        }
    }
}

@Composable
private fun FilterChip(label: String, kind: MenuFilter, current: MenuFilter, onSelect: (MenuFilter) -> Unit) {
    CozyChip(
        label = label,
        selected = kind == current,
        onClick = { onSelect(kind) },
    )
}

@Composable
private fun CircleIconButton(emoji: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = emoji, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
    }
}

private data class SlotStyle(val bg: Color, val accent: Color, val border: Color)

@Composable
private fun cellKindStyle(cell: MenuCell?, extras: CozyExtraColors): SlotStyle = when (cell) {
    is MenuCell.Recipe -> SlotStyle(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary)
    is MenuCell.Free -> SlotStyle(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onBackground, MaterialTheme.colorScheme.outlineVariant)
    is MenuCell.Out -> SlotStyle(extras.lavenderSoft, extras.lavender, extras.lavender)
    is MenuCell.Delivery -> SlotStyle(extras.coralSoft, extras.coral, extras.coral)
    null -> SlotStyle(Color.Transparent, extras.textTer, MaterialTheme.colorScheme.outline)
}

private fun MenuFilter.matches(cell: MenuCell?): Boolean = when (this) {
    MenuFilter.ALL -> true
    MenuFilter.EMPTY -> cell == null
    MenuFilter.RECIPE -> cell is MenuCell.Recipe
    MenuFilter.FREE -> cell is MenuCell.Free
    MenuFilter.OUT -> cell is MenuCell.Out
    MenuFilter.DELIVERY -> cell is MenuCell.Delivery
}
