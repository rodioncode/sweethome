package com.jetbrains.kmpapp.screens.todo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetbrains.kmpapp.data.lists.TodoItem
import com.jetbrains.kmpapp.data.templates.TaskTemplate
import com.jetbrains.kmpapp.data.templates.TemplateVisibility
import com.jetbrains.kmpapp.ui.LocalCozyExtraColors
import com.jetbrains.kmpapp.ui.LocalCozyShapes
import com.jetbrains.kmpapp.ui.LocalCozySpacing
import com.jetbrains.kmpapp.ui.listEmojiForType
import kotlinx.coroutines.launch

private enum class PickerTab(val label: String) {
    TEMPLATES("Шаблоны"),
    FREQUENT("Часто"),
    FAVORITES("Избранное"),
}

/**
 * Inline-выбор шаблона задачи (G-03). Показывается поверх ItemBottomSheet при создании/редактировании.
 *
 * Источники данных:
 *  - **Шаблоны** = `taskTemplates` (TemplatesRepository.publicTaskByScope[scope] ∪ myTask, mine сверху).
 *  - **Часто** = `frequentItems` (SuggestionsRepository.frequentItems).
 *  - **Избранное** = `favoriteItems` (SuggestionsRepository.favoriteItems).
 *
 * При выборе шаблона задачи репо подгружает [TaskTemplateDetail] и колбэк делает pre-fill полей формы.
 * Для frequent/favorite items pre-fill идёт сразу — у нас уже есть TodoItem.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TemplatePickerSheet(
    listType: String,
    taskTemplates: List<TaskTemplate>,
    frequentItems: List<TodoItem>,
    favoriteItems: List<TodoItem>,
    onPickTemplate: (TaskTemplate) -> Unit,
    onPickItem: (TodoItem) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val close: () -> Unit = {
        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
    }

    var tab by remember { mutableStateOf(PickerTab.TEMPLATES) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = LocalCozySpacing.current.xxl, vertical = LocalCozySpacing.current.xs),
        ) {
            Text(
                text = "Из шаблона",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(vertical = LocalCozySpacing.current.xs),
            )
            // Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = LocalCozySpacing.current.xs),
                horizontalArrangement = Arrangement.spacedBy(LocalCozySpacing.current.xs),
            ) {
                PickerTab.entries.forEach { t ->
                    val count = when (t) {
                        PickerTab.TEMPLATES -> taskTemplates.size
                        PickerTab.FREQUENT -> frequentItems.size
                        PickerTab.FAVORITES -> favoriteItems.size
                    }
                    PickerTabChip(
                        label = if (count > 0) "${t.label} · $count" else t.label,
                        selected = tab == t,
                        onClick = { tab = t },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Spacer(Modifier.height(LocalCozySpacing.current.xs))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 240.dp, max = 480.dp),
            ) {
                when (tab) {
                    PickerTab.TEMPLATES -> TemplatesList(
                        templates = taskTemplates,
                        listType = listType,
                        onPick = { template ->
                            onPickTemplate(template)
                            close()
                        },
                    )
                    PickerTab.FREQUENT -> ItemsList(
                        items = frequentItems,
                        emptyMessage = "Здесь появятся часто добавляемые элементы",
                        onPick = { item ->
                            onPickItem(item)
                            close()
                        },
                    )
                    PickerTab.FAVORITES -> ItemsList(
                        items = favoriteItems,
                        emptyMessage = "Нет избранных элементов",
                        onPick = { item ->
                            onPickItem(item)
                            close()
                        },
                    )
                }
            }
            Spacer(Modifier.height(LocalCozySpacing.current.sm))
        }
    }
}

@Composable
private fun PickerTabChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = LocalCozyShapes.current.chip,
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Box(
            modifier = Modifier.padding(vertical = LocalCozySpacing.current.xs),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (selected) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TemplatesList(
    templates: List<TaskTemplate>,
    listType: String,
    onPick: (TaskTemplate) -> Unit,
) {
    if (templates.isEmpty()) {
        EmptyPickerState(
            emoji = "📋",
            message = "Для этого типа списка шаблонов пока нет",
        )
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(LocalCozySpacing.current.xs)) {
        items(templates, key = { it.id }) { template ->
            TemplateRow(
                emoji = listEmojiForType(template.scope.ifBlank { listType }),
                title = template.title,
                subtitle = template.description?.takeIf { it.isNotBlank() },
                visibilityBadge = visibilityBadge(template),
                isFavorite = template.isFavorite,
                onClick = { onPick(template) },
            )
        }
    }
}

@Composable
private fun ItemsList(
    items: List<TodoItem>,
    emptyMessage: String,
    onPick: (TodoItem) -> Unit,
) {
    if (items.isEmpty()) {
        EmptyPickerState(emoji = "✨", message = emptyMessage)
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(LocalCozySpacing.current.xs)) {
        items(items, key = { it.id }) { item ->
            val parts = buildList {
                item.shopping?.let { s ->
                    val qty = s.quantity?.let { q -> if (q % 1 == 0.0) q.toLong().toString() else q.toString() }
                    if (qty != null || s.unit != null) add("${qty ?: ""}${s.unit ?: ""}".trim())
                    s.category?.let { add(it) }
                }
                item.choreSchedule?.intervalDays?.let { add("каждые $it д.") }
                item.priority?.let { add(priorityLabel(it)) }
            }.filter { it.isNotBlank() }
            TemplateRow(
                emoji = if (item.isFavorite) "⭐" else "•",
                title = item.title,
                subtitle = parts.joinToString(" · ").takeIf { it.isNotBlank() },
                visibilityBadge = null,
                isFavorite = item.isFavorite,
                onClick = { onPick(item) },
            )
        }
    }
}

@Composable
private fun TemplateRow(
    emoji: String,
    title: String,
    subtitle: String?,
    visibilityBadge: VisibilityBadge?,
    isFavorite: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = LocalCozyShapes.current.card,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(LocalCozySpacing.current.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = emoji, fontSize = 18.sp)
            }
            Spacer(Modifier.width(LocalCozySpacing.current.sm))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    visibilityBadge?.let { badge ->
                        Spacer(Modifier.width(LocalCozySpacing.current.xs))
                        VisibilityChip(badge = badge)
                    }
                    if (isFavorite && visibilityBadge == null) {
                        Spacer(Modifier.width(LocalCozySpacing.current.xs))
                        Text("⭐", fontSize = 12.sp)
                    }
                }
                subtitle?.let {
                    Text(
                        text = it,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Spacer(Modifier.width(LocalCozySpacing.current.xs))
            Text(
                text = "→",
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EmptyPickerState(emoji: String, message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(LocalCozySpacing.current.xxxl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(emoji, fontSize = 32.sp)
        Spacer(Modifier.height(LocalCozySpacing.current.xs))
        Text(
            text = message,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private enum class VisibilityBadge(val label: String) {
    MINE("✦ Моё"),
    PENDING("⏳ Pending"),
}

private fun visibilityBadge(t: TaskTemplate): VisibilityBadge? = when {
    t.visibility == TemplateVisibility.PENDING -> VisibilityBadge.PENDING
    t.userId != null && !t.isSystem && t.visibility == TemplateVisibility.PRIVATE -> VisibilityBadge.MINE
    else -> null
}

@Composable
private fun VisibilityChip(badge: VisibilityBadge) {
    val extras = LocalCozyExtraColors.current
    val badgeColor = when (badge) {
        VisibilityBadge.MINE -> extras.success
        VisibilityBadge.PENDING -> extras.ochre
    }
    Surface(
        shape = LocalCozyShapes.current.chip,
        color = badgeColor.copy(alpha = 0.14f),
    ) {
        Text(
            text = badge.label,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = badgeColor,
            modifier = Modifier.padding(horizontal = LocalCozySpacing.current.xs, vertical = 2.dp),
        )
    }
}

private fun priorityLabel(p: String): String = when (p) {
    "high" -> "⬆️ высокий"
    "medium" -> "➡️ средний"
    "low" -> "⬇️ низкий"
    else -> ""
}
