package com.jetbrains.kmpapp.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jetbrains.kmpapp.data.calendar.CalendarEvent
import com.jetbrains.kmpapp.data.calendar.EventSource
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.koin.compose.viewmodel.koinViewModel

private val MONTHS_RU = listOf(
    "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
    "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь",
)
private val WEEKDAYS_RU = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")

@Composable
fun CalendarContent(
    contentPadding: PaddingValues,
    onItemOpen: (listId: String, itemId: String) -> Unit,
) {
    val vm = koinViewModel<CalendarViewModel>()
    val view by vm.view.collectAsStateWithLifecycle()
    val cursor by vm.cursor.collectAsStateWithLifecycle()
    val selected by vm.selected.collectAsStateWithLifecycle()
    val eventsByDate by vm.eventsByDate.collectAsStateWithLifecycle()
    val filters by vm.filters.collectAsStateWithLifecycle()

    var showFilters by remember { mutableStateOf(false) }
    var openEvent by remember { mutableStateOf<CalendarEvent?>(null) }
    var showAssign by remember { mutableStateOf(false) }
    val unscheduled by vm.unscheduledItems.collectAsStateWithLifecycle()
    val lists by vm.lists.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize().padding(contentPadding)) {
        Column(modifier = Modifier.fillMaxSize()) {
            CalendarHeader(
                cursor = cursor,
                view = view,
                onPrev = {
                    val unit = if (view == CalendarView.MONTH) DateTimeUnit.MONTH else DateTimeUnit.WEEK
                    vm.setCursor(cursor.minus(1, unit))
                },
                onNext = {
                    val unit = if (view == CalendarView.MONTH) DateTimeUnit.MONTH else DateTimeUnit.WEEK
                    vm.setCursor(cursor.plus(1, unit))
                },
                onToday = vm::goToday,
                onSetView = vm::setView,
                onFilters = { showFilters = true },
                filtersActive = filters != com.jetbrains.kmpapp.screens.calendar.CalendarFilters(),
            )

            when (view) {
                CalendarView.MONTH -> MonthView(
                    cursor = cursor,
                    selected = selected,
                    eventsByDate = eventsByDate,
                    onSelect = vm::setSelected,
                    onOpenEvent = { openEvent = it },
                )
                CalendarView.WEEK -> WeekView(
                    cursor = cursor,
                    selected = selected,
                    eventsByDate = eventsByDate,
                    onSelect = vm::setSelected,
                    onOpenEvent = { openEvent = it },
                )
                CalendarView.AGENDA -> AgendaView(
                    cursor = cursor,
                    eventsByDate = eventsByDate,
                    onOpenEvent = { openEvent = it },
                )
            }
        }

        // FAB для назначения даты выбранному дню
        androidx.compose.material3.FloatingActionButton(
            onClick = { showAssign = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
        ) {
            Text("+", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }

    if (showAssign) {
        AssignDateSheet(
            date = selected,
            unscheduled = unscheduled,
            lists = lists,
            onDismiss = { showAssign = false },
            onAssign = { itemId, hour, minute ->
                vm.assignDate(itemId, selected, hour, minute)
                showAssign = false
            },
        )
    }

    if (showFilters) {
        CalendarFiltersSheet(
            filters = filters,
            lists = vm.lists.collectAsStateWithLifecycle().value,
            onToggleSource = vm::toggleSource,
            onTogglePriority = vm::togglePriority,
            onToggleList = vm::toggleList,
            onReset = vm::resetFilters,
            onDismiss = { showFilters = false },
        )
    }

    openEvent?.let { ev ->
        EventDetailSheet(
            event = ev,
            onDismiss = { openEvent = null },
            onOpenItem = {
                onItemOpen(ev.listId, ev.itemId)
                openEvent = null
            },
        )
    }
}

@Composable
private fun CalendarHeader(
    cursor: LocalDate,
    view: CalendarView,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit,
    onSetView: (CalendarView) -> Unit,
    onFilters: () -> Unit,
    filtersActive: Boolean,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBtn("‹", onPrev)
                Spacer(Modifier.width(6.dp))
                Text(
                    "${MONTHS_RU[cursor.monthNumber - 1]} ${cursor.year}",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                IconBtn("›", onNext)
                Spacer(Modifier.width(6.dp))
                Surface(onClick = onToday, shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surfaceVariant) {
                    Text("Сегодня", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(Modifier.width(6.dp))
                Surface(
                    onClick = onFilters,
                    shape = CircleShape,
                    color = if (filtersActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(32.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("⚙", fontSize = 14.sp, color = if (filtersActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row {
                listOf(
                    CalendarView.MONTH to "Месяц",
                    CalendarView.WEEK to "Неделя",
                    CalendarView.AGENDA to "Список",
                ).forEach { (v, label) ->
                    val active = view == v
                    Surface(
                        onClick = { onSetView(v) },
                        shape = RoundedCornerShape(50),
                        color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.padding(end = 6.dp),
                    ) {
                        Text(label, modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold,
                            color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}

@Composable
private fun IconBtn(label: String, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(32.dp)) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun MonthView(
    cursor: LocalDate,
    selected: LocalDate,
    eventsByDate: Map<LocalDate, List<CalendarEvent>>,
    onSelect: (LocalDate) -> Unit,
    onOpenEvent: (CalendarEvent) -> Unit,
) {
    val firstOfMonth = LocalDate(cursor.year, cursor.month, 1)
    val firstOffset = ((firstOfMonth.dayOfWeek.ordinal) % 7)  // Mon=0
    val gridStart = firstOfMonth.minus(firstOffset, DateTimeUnit.DAY)

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp)) {
            WEEKDAYS_RU.forEach { d ->
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(d, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        // 6 weeks grid
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).weight(1f, fill = false)) {
            for (w in 0 until 6) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (d in 0 until 7) {
                        val date = gridStart.plus(w * 7 + d, DateTimeUnit.DAY)
                        val isCurrentMonth = date.month == cursor.month
                        val isSelected = date == selected
                        val dayEvents = eventsByDate[date].orEmpty()
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp)
                                .padding(2.dp)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surface,
                                    RoundedCornerShape(8.dp),
                                )
                                .clickable { onSelect(date) }
                                .padding(4.dp),
                        ) {
                            Column {
                                Text(
                                    date.dayOfMonth.toString(),
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        isSelected -> MaterialTheme.colorScheme.primary
                                        !isCurrentMonth -> MaterialTheme.colorScheme.outline
                                        else -> MaterialTheme.colorScheme.onSurface
                                    },
                                )
                                Spacer(Modifier.height(2.dp))
                                Row {
                                    dayEvents.take(3).forEach { ev ->
                                        Box(
                                            modifier = Modifier
                                                .size(5.dp)
                                                .padding(end = 2.dp)
                                                .background(eventColor(ev), CircleShape),
                                        )
                                    }
                                    if (dayEvents.size > 3) {
                                        Text("+", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Events for selected day
        val dayEvents = eventsByDate[selected].orEmpty()
        Spacer(Modifier.height(8.dp))
        Text(
            "${selected.dayOfMonth} ${MONTHS_RU[selected.monthNumber - 1].lowercase()} · ${dayEvents.size} ${pluralEvents(dayEvents.size)}",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )
        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
            items(dayEvents, key = { it.id }) { ev ->
                EventChip(ev, onClick = { onOpenEvent(ev) })
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun WeekView(
    cursor: LocalDate,
    selected: LocalDate,
    eventsByDate: Map<LocalDate, List<CalendarEvent>>,
    onSelect: (LocalDate) -> Unit,
    onOpenEvent: (CalendarEvent) -> Unit,
) {
    // Неделя, начиная с понедельника, содержащая cursor
    val dayOfWeekIdx = (cursor.dayOfWeek.ordinal) % 7  // Mon=0..Sun=6
    val weekStart = cursor.minus(dayOfWeekIdx, DateTimeUnit.DAY)
    val days = (0 until 7).map { weekStart.plus(it, DateTimeUnit.DAY) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp)) {
            days.forEachIndexed { idx, day ->
                val isSelected = day == selected
                val dayEvents = eventsByDate[day].orEmpty()
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 2.dp)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surface,
                            RoundedCornerShape(10.dp),
                        )
                        .clickable { onSelect(day) }
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            WEEKDAYS_RU[idx],
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            day.dayOfMonth.toString(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(Modifier.height(4.dp))
                        Row {
                            dayEvents.take(4).forEach { ev ->
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .padding(end = 2.dp)
                                        .background(eventColor(ev), CircleShape),
                                )
                            }
                        }
                    }
                }
            }
        }

        val dayEvents = eventsByDate[selected].orEmpty()
        Spacer(Modifier.height(4.dp))
        Text(
            "${selected.dayOfMonth} ${MONTHS_RU[selected.monthNumber - 1].lowercase()} · ${dayEvents.size} ${pluralEvents(dayEvents.size)}",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )
        if (dayEvents.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                Text("Нет событий. Нажмите +, чтобы назначить.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(dayEvents, key = { it.id }) { ev -> EventChip(ev, onClick = { onOpenEvent(ev) }) }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun AgendaView(
    cursor: LocalDate,
    eventsByDate: Map<LocalDate, List<CalendarEvent>>,
    onOpenEvent: (CalendarEvent) -> Unit,
) {
    val days = (0 until 30).map { cursor.plus(it, DateTimeUnit.DAY) }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        days.forEach { day ->
            val evs = eventsByDate[day].orEmpty()
            if (evs.isNotEmpty()) {
                item(key = "h_$day") {
                    Text(
                        "${day.dayOfMonth} ${MONTHS_RU[day.monthNumber - 1].lowercase()} · ${WEEKDAYS_RU[(day.dayOfWeek.ordinal) % 7]}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
                items(evs, key = { it.id }) { ev ->
                    EventChip(ev, onClick = { onOpenEvent(ev) })
                }
            }
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun EventChip(ev: CalendarEvent, onClick: () -> Unit) {
    val color = eventColor(ev)
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    ev.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    sourceLabel(ev.source) + (ev.time?.let { " · ${it.hour.toString().padStart(2, '0')}:${it.minute.toString().padStart(2, '0')}" } ?: ""),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun pluralEvents(n: Int): String = when {
    n % 10 == 1 && n % 100 != 11 -> "событие"
    n % 10 in 2..4 && n % 100 !in 12..14 -> "события"
    else -> "событий"
}

private fun sourceLabel(s: EventSource): String = when (s) {
    EventSource.TASK -> "Задача"
    EventSource.CHORE -> "Дело"
}

@androidx.compose.runtime.Composable
private fun eventColor(ev: CalendarEvent): Color = when (ev.priority) {
    "high" -> MaterialTheme.colorScheme.error
    "medium" -> Color(0xFFE8A87C)
    "low" -> MaterialTheme.colorScheme.primary
    else -> MaterialTheme.colorScheme.outline
}

