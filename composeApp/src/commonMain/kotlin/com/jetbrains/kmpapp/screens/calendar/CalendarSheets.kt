package com.jetbrains.kmpapp.screens.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetbrains.kmpapp.ui.LocalCozyShapes
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import com.jetbrains.kmpapp.data.calendar.CalendarEvent
import com.jetbrains.kmpapp.data.calendar.EventSource
import com.jetbrains.kmpapp.data.lists.TodoList
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCalendarTaskSheet(
    date: LocalDate,
    lists: List<TodoList>,
    onDismiss: () -> Unit,
    onCreate: (listId: String, title: String, note: String?) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedListId by remember(lists) { mutableStateOf(lists.firstOrNull()?.id ?: "") }
    var listMenuExpanded by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "Новая задача",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                "На ${date.dayOfMonth}.${date.monthNumber.toString().padStart(2, '0')}.${date.year}",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (lists.isEmpty()) {
                Text(
                    "Нет доступных списков. Сначала создайте список.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.error,
                )
                return@Column
            }

            ExposedDropdownMenuBox(
                expanded = listMenuExpanded,
                onExpandedChange = { listMenuExpanded = it },
            ) {
                OutlinedTextField(
                    value = lists.find { it.id == selectedListId }?.title ?: "Выберите список",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Список") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = listMenuExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    shape = MaterialTheme.shapes.small,
                )
                ExposedDropdownMenu(
                    expanded = listMenuExpanded,
                    onDismissRequest = { listMenuExpanded = false },
                ) {
                    lists.forEach { list ->
                        DropdownMenuItem(
                            text = { Text(list.title) },
                            onClick = {
                                selectedListId = list.id
                                listMenuExpanded = false
                            },
                        )
                    }
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Название *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
            )

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Заметка") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
            )

            Surface(
                onClick = {
                    if (title.isNotBlank() && selectedListId.isNotBlank()) {
                        onCreate(selectedListId, title, note.takeIf { it.isNotBlank() })
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = LocalCozyShapes.current.button,
                color = if (title.isNotBlank()) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "Создать",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }

            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Отмена") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailSheet(
    event: CalendarEvent,
    onDismiss: () -> Unit,
    onOpenItem: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(event.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(
                "${event.date.dayOfMonth}.${event.date.monthNumber.toString().padStart(2, '0')}.${event.date.year}" +
                    (event.time?.let { " · ${it.hour.toString().padStart(2, '0')}:${it.minute.toString().padStart(2, '0')}" } ?: " · весь день"),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Tag(when (event.source) { EventSource.TASK -> "📋 Задача"; EventSource.CHORE -> "🧹 Дело" })
                event.priority?.let { Tag(priorityLabel(it)) }
                if (event.isDone) Tag("✓ Выполнено")
            }
            Spacer(Modifier.height(4.dp))
            Surface(
                onClick = onOpenItem,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primary,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("Открыть в списке", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarFiltersSheet(
    filters: CalendarFilters,
    lists: List<TodoList>,
    onToggleSource: (EventSource) -> Unit,
    onTogglePriority: (String) -> Unit,
    onToggleList: (String) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Фильтры", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                TextButton(onClick = onReset) { Text("Сбросить") }
            }

            SectionLabel("Источник")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EventSource.values().forEach { s ->
                    Toggle(
                        label = when (s) { EventSource.TASK -> "Задачи"; EventSource.CHORE -> "Дела" },
                        active = s in filters.sources,
                        onClick = { onToggleSource(s) },
                    )
                }
            }

            SectionLabel("Приоритет")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("high" to "Высокий", "medium" to "Средний", "low" to "Низкий").forEach { (id, label) ->
                    val active = filters.priorities == null || id in filters.priorities
                    Toggle(label, active, onClick = { onTogglePriority(id) })
                }
            }

            if (lists.isNotEmpty()) {
                SectionLabel("Списки")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    lists.forEach { l ->
                        val active = filters.listIds == null || l.id in filters.listIds
                        Toggle(l.title, active, onClick = { onToggleList(l.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun Toggle(label: String, active: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = LocalCozyShapes.current.pill,
        color = if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun Tag(text: String) {
    Surface(shape = LocalCozyShapes.current.pill, color = MaterialTheme.colorScheme.surfaceVariant) {
        Text(text, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun priorityLabel(p: String): String = when (p) {
    "high" -> "🔴 Высокий"; "medium" -> "🟠 Средний"; "low" -> "🟢 Низкий"; else -> p
}

private val MONTHS_GEN = listOf(
    "января", "февраля", "марта", "апреля", "мая", "июня",
    "июля", "августа", "сентября", "октября", "ноября", "декабря",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignDateSheet(
    date: kotlinx.datetime.LocalDate,
    unscheduled: List<com.jetbrains.kmpapp.data.lists.TodoItem>,
    lists: List<TodoList>,
    onDismiss: () -> Unit,
    onAssign: (itemId: String, hour: Int?, minute: Int?) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var query by remember { mutableStateOf("") }
    var allDay by remember { mutableStateOf(true) }
    var timeText by remember { mutableStateOf("12:00") }

    val listsById = remember(lists) { lists.associateBy { it.id } }
    val filtered = remember(unscheduled, query) {
        if (query.isBlank()) unscheduled
        else unscheduled.filter { it.title.contains(query, ignoreCase = true) }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                "Назначить на ${date.dayOfMonth} ${MONTHS_GEN[date.monthNumber - 1]}",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.material3.Switch(checked = allDay, onCheckedChange = { allDay = it })
                Spacer(Modifier.width(8.dp))
                Text("Весь день", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                if (!allDay) {
                    androidx.compose.material3.OutlinedTextField(
                        value = timeText,
                        onValueChange = { timeText = it },
                        singleLine = true,
                        modifier = Modifier.width(100.dp),
                        placeholder = { Text("HH:MM") },
                    )
                }
            }
            androidx.compose.material3.OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Поиск задач...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            if (filtered.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(
                        if (unscheduled.isEmpty()) "Нет задач без даты"
                        else "Ничего не найдено",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp),
                ) {
                    items(filtered, key = { it.id }) { item ->
                        Surface(
                            onClick = {
                                if (allDay) onAssign(item.id, null, null)
                                else {
                                    val parts = timeText.split(":")
                                    val h = parts.getOrNull(0)?.toIntOrNull()?.coerceIn(0, 23)
                                    val m = parts.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 59)
                                    if (h != null && m != null) onAssign(item.id, h, m)
                                    else onAssign(item.id, null, null)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                            shape = LocalCozyShapes.current.chip,
                            color = MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                    val listTitle = listsById[item.listId]?.title ?: "—"
                                    Text(listTitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                item.priority?.let { Tag(priorityLabel(it)) }
                            }
                        }
                    }
                }
            }
        }
    }
}
