package com.jetbrains.kmpapp.screens.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetbrains.kmpapp.ui.LocalCozyExtraColors
import com.jetbrains.kmpapp.ui.LocalCozyShapes
import com.jetbrains.kmpapp.ui.LocalCozySpacing
import com.jetbrains.kmpapp.ui.components.CozyCard
import com.jetbrains.kmpapp.ui.components.CozyChip
import com.jetbrains.kmpapp.ui.components.CozyTopBar
import com.jetbrains.kmpapp.ui.components.MetaRow
import com.jetbrains.kmpapp.ui.models.ListType
import com.jetbrains.kmpapp.ui.models.Palette

@Composable
fun AddTaskScreen(
    onBack: () -> Unit,
    onCreate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedType by remember { mutableStateOf(ListType.SHOPPING) }
    val spacing = LocalCozySpacing.current
    val shapes = LocalCozyShapes.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        CozyTopBar(onBack = onBack, title = "Новая карточка", action = {
            TextButton(onClick = onCreate) {
                Text(
                    text = "Создать",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        })

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.xxl),
        ) {
            SectionLabel("ТИП СПИСКА")
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = spacing.xxs),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                ListType.entries.forEach { lt ->
                    TypePill(
                        type = lt,
                        selected = lt == selectedType,
                        onClick = { selectedType = lt },
                    )
                }
            }

            Spacer(Modifier.height(spacing.md))

            SectionLabel("В СПИСОК")
            CozyCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(selectedType.palette.resolveContainer()),
                        contentAlignment = Alignment.Center,
                    ) { Text(selectedType.emoji, fontSize = 18.sp) }
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Список — ${selectedType.displayName}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            text = "Меняется при смене типа",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                    Text("↕", color = MaterialTheme.colorScheme.outlineVariant, fontSize = 14.sp)
                }
            }

            Spacer(Modifier.height(spacing.md))

            CozyCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, selectedType.palette.resolve(), shapes.button),
            ) {
                Column {
                    Text(
                        text = AddTaskFieldsSpec.titleLabelFor(selectedType),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = selectedType.palette.resolve(),
                    )
                    Text(
                        text = AddTaskFieldsSpec.titlePlaceholderFor(selectedType),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }

            Spacer(Modifier.height(spacing.md))

            AddTaskFieldsSpec.fieldsFor(selectedType).forEach { block ->
                AddTaskFieldBlock(block = block, accent = selectedType.palette.resolve())
                Spacer(Modifier.height(spacing.md))
            }

            SectionLabel("ДОПОЛНИТЕЛЬНО")
            CozyCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    MetaRow(icon = "⚡", title = "Приоритет", value = "Средний")
                    MetaRow(icon = "👤", title = "Кому", value = "Я")
                    MetaRow(icon = "📅", title = "Когда", value = "Сегодня")
                    MetaRow(icon = "📝", title = "Заметка", value = "—")
                }
            }

            Spacer(Modifier.height(spacing.xxxl))
        }
    }
}

@Composable
private fun TypePill(type: ListType, selected: Boolean, onClick: () -> Unit) {
    val container = if (selected) type.palette.resolveContainer()
    else MaterialTheme.colorScheme.surface
    val accent = if (selected) type.palette.resolve()
    else MaterialTheme.colorScheme.onSurfaceVariant
    val shape = LocalCozyShapes.current.button

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .background(container, shape)
            .then(
                if (!selected) Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
                else Modifier
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(text = type.emoji, fontSize = 14.sp)
        Text(text = type.displayName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = accent)
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 14.dp, bottom = 10.dp),
    )
}

@Composable
private fun Palette.resolve(): Color = when (this) {
    Palette.PRIMARY -> MaterialTheme.colorScheme.primary
    Palette.CORAL -> LocalCozyExtraColors.current.coral
    Palette.OCHRE -> LocalCozyExtraColors.current.ochre
    Palette.LAVENDER -> LocalCozyExtraColors.current.lavender
}

@Composable
private fun Palette.resolveContainer(): Color = when (this) {
    Palette.PRIMARY -> MaterialTheme.colorScheme.primaryContainer
    Palette.CORAL -> LocalCozyExtraColors.current.coralSoft
    Palette.OCHRE -> LocalCozyExtraColors.current.ochreSoft
    Palette.LAVENDER -> LocalCozyExtraColors.current.lavenderSoft
}
