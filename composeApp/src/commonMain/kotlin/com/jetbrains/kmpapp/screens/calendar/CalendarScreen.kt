package com.jetbrains.kmpapp.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jetbrains.kmpapp.data.calendar.CalendarEvent
import com.jetbrains.kmpapp.data.calendar.EventSource
import com.jetbrains.kmpapp.ui.LocalCozyExtraColors
import com.jetbrains.kmpapp.ui.LocalCozyShapes
import com.jetbrains.kmpapp.ui.LocalCozySpacing
import com.jetbrains.kmpapp.ui.components.CozyCard
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.koin.compose.viewmodel.koinViewModel

private val MONTHS_RU = listOf(
    "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
    "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь",
)
private val MONTHS_RU_GEN = listOf(
    "января", "февраля", "марта", "апреля", "мая", "июня",
    "июля", "августа", "сентября", "октября", "ноября", "декабря",
)
private val WEEKDAYS_RU = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
private val WEEKDAYS_RU_FULL = listOf(
    "ПОНЕДЕЛЬНИК", "ВТОРНИК", "СРЕДА", "ЧЕТВЕРГ", "ПЯТНИЦА", "СУББОТА", "ВОСКРЕСЕНЬЕ",
)

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
    val lists by vm.lists.collectAsStateWithLifecycle()

    var showFilters by remember { mutableStateOf(false) }
    var openEvent by remember { mutableStateOf<CalendarEvent?>(null) }
    var showCreateTask by remember { mutableStateOf(false) }

    val filtersActive = filters != CalendarFilters()
    val activeFilterCount = remember(filters) {
        var n = 0
        if (filters.sources.size < EventSource.entries.size) n++
        if (filters.priorities != null) n++
        if (filters.listIds != null) n++
        n
    }

    Box(modifier = Modifier.fillMaxSize().padding(contentPadding)) {
        Column(modifier = Modifier.fillMaxSize()) {
            CalendarHeader(
                cursor = cursor,
                view = view,
                filtersActive = filtersActive,
                filterCount = activeFilterCount,
                onPrev = {
                    val unit = if (view == CalendarView.MONTH) DateTimeUnit.MONTH else DateTimeUnit.WEEK
                    vm.setCursor(cursor.minus(1, unit))
                },
                onNext = {
                    val unit = if (view == CalendarView.MONTH) DateTimeUnit.MONTH else DateTimeUnit.WEEK
                    vm.setCursor(cursor.plus(1, unit))
                },
                onAdd = { showCreateTask = true },
                onSetView = vm::setView,
                onFilters = { showFilters = true },
                onToday = vm::goToday,
            )

            when (view) {
                CalendarView.MONTH -> MonthPager(
                    cursor = cursor,
                    selected = selected,
                    eventsByDate = eventsByDate,
                    onCursorChange = vm::setCursor,
                    onSelect = vm::setSelected,
                    onOpenEvent = { openEvent = it },
                )
                CalendarView.WEEK -> WeekPager(
                    cursor = cursor,
                    selected = selected,
                    eventsByDate = eventsByDate,
                    onCursorChange = vm::setCursor,
                    onSelect = vm::setSelected,
                    onOpenEvent = { openEvent = it },
                )
                CalendarView.AGENDA -> DayView(
                    selected = selected,
                    eventsByDate = eventsByDate,
                    onOpenEvent = { openEvent = it },
                )
            }
        }

        FloatingActionButton(
            onClick = { showCreateTask = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(LocalCozySpacing.current.xl)
                .size(56.dp),
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ) {
            Text("+", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }

    if (showCreateTask) {
        CreateCalendarTaskSheet(
            date = selected,
            lists = lists,
            onDismiss = { showCreateTask = false },
            onCreate = { listId, title, note ->
                vm.createItemForList(listId, title, selected, note)
                showCreateTask = false
            },
        )
    }

    if (showFilters) {
        CalendarFiltersSheet(
            filters = filters,
            lists = lists,
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
    filtersActive: Boolean,
    filterCount: Int,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onAdd: () -> Unit,
    onSetView: (CalendarView) -> Unit,
    onFilters: () -> Unit,
    onToday: () -> Unit,
) {
    val spacing = LocalCozySpacing.current
    val shapes = LocalCozyShapes.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(top = spacing.sm, bottom = spacing.sm),
    ) {
        // Title row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.xxl),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SmallIconBtn("‹", onPrev)
                    Spacer(Modifier.width(spacing.xs))
                    Text(
                        "${MONTHS_RU[cursor.monthNumber - 1]} ${cursor.year}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .clickable(onClick = onToday)
                            .padding(horizontal = spacing.xxs),
                    )
                    Spacer(Modifier.width(spacing.xxs))
                    SmallIconBtn("›", onNext)
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    "Все списки · мои задачи",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = spacing.xs),
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable(onClick = onAdd),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "+",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }

        Spacer(Modifier.height(spacing.lg))

        // View toggle + filter button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.xxl),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ViewToggleSegment(view = view, onSetView = onSetView)
            Spacer(Modifier.weight(1f))
            FilterButton(active = filtersActive, count = filterCount, onClick = onFilters)
        }
    }
}

@Composable
private fun SmallIconBtn(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun ViewToggleSegment(view: CalendarView, onSetView: (CalendarView) -> Unit) {
    val shapes = LocalCozyShapes.current
    Row(
        modifier = Modifier
            .clip(shapes.chip)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(3.dp),
    ) {
        listOf(
            CalendarView.MONTH to "Месяц",
            CalendarView.WEEK to "Неделя",
            CalendarView.AGENDA to "День",
        ).forEach { (v, label) ->
            val sel = view == v
            val segShape = RoundedCornerShape(11.dp)
            Box(
                modifier = Modifier
                    .then(if (sel) Modifier.shadow(2.dp, segShape, clip = false) else Modifier)
                    .clip(segShape)
                    .background(if (sel) MaterialTheme.colorScheme.surface else Color.Transparent)
                    .clickable { onSetView(v) }
                    .padding(horizontal = 14.dp, vertical = 7.dp),
            ) {
                Text(
                    label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (sel) MaterialTheme.colorScheme.onBackground
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun FilterButton(active: Boolean, count: Int, onClick: () -> Unit) {
    val shapes = LocalCozyShapes.current
    val bg = if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val fg = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = Modifier
            .height(32.dp)
            .clip(shapes.chip)
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "⚙ Фильтры" + if (count > 0) " · $count" else "",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = fg,
        )
    }
}

private const val PAGER_INITIAL = 1000

private fun monthsBetween(a: LocalDate, b: LocalDate): Int =
    (b.year - a.year) * 12 + (b.monthNumber - a.monthNumber)

@Composable
private fun MonthPager(
    cursor: LocalDate,
    selected: LocalDate,
    eventsByDate: Map<LocalDate, List<CalendarEvent>>,
    onCursorChange: (LocalDate) -> Unit,
    onSelect: (LocalDate) -> Unit,
    onOpenEvent: (CalendarEvent) -> Unit,
) {
    val baseMonth = remember { LocalDate(cursor.year, cursor.month, 1) }
    val pagerState = rememberPagerState(initialPage = PAGER_INITIAL) { Int.MAX_VALUE }

    LaunchedEffect(cursor) {
        val targetPage = PAGER_INITIAL + monthsBetween(baseMonth, cursor)
        if (pagerState.currentPage != targetPage && !pagerState.isScrollInProgress) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { page ->
                val delta = page - PAGER_INITIAL
                val newCursor = baseMonth.plus(delta, DateTimeUnit.MONTH)
                if (newCursor != cursor) onCursorChange(newCursor)
            }
    }

    HorizontalPager(state = pagerState) { page ->
        val pageMonth = baseMonth.plus(page - PAGER_INITIAL, DateTimeUnit.MONTH)
        MonthView(
            cursor = pageMonth,
            selected = selected,
            eventsByDate = eventsByDate,
            onSelect = onSelect,
            onOpenEvent = onOpenEvent,
        )
    }
}

@Composable
private fun WeekPager(
    cursor: LocalDate,
    selected: LocalDate,
    eventsByDate: Map<LocalDate, List<CalendarEvent>>,
    onCursorChange: (LocalDate) -> Unit,
    onSelect: (LocalDate) -> Unit,
    onOpenEvent: (CalendarEvent) -> Unit,
) {
    val baseWeek = remember {
        val dow = (cursor.dayOfWeek.ordinal) % 7
        cursor.minus(dow, DateTimeUnit.DAY)
    }
    val pagerState = rememberPagerState(initialPage = PAGER_INITIAL) { Int.MAX_VALUE }

    LaunchedEffect(cursor) {
        val cursorWeekStart = cursor.minus((cursor.dayOfWeek.ordinal) % 7, DateTimeUnit.DAY)
        val daysDelta = cursorWeekStart.toEpochDays() - baseWeek.toEpochDays()
        val targetPage = PAGER_INITIAL + (daysDelta / 7).toInt()
        if (pagerState.currentPage != targetPage && !pagerState.isScrollInProgress) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { page ->
                val delta = page - PAGER_INITIAL
                val newCursor = baseWeek.plus(delta * 7L, DateTimeUnit.DAY)
                if (newCursor != cursor) onCursorChange(newCursor)
            }
    }

    HorizontalPager(state = pagerState) { page ->
        val weekStart = baseWeek.plus((page - PAGER_INITIAL) * 7L, DateTimeUnit.DAY)
        WeekView(
            weekStart = weekStart,
            selected = selected,
            eventsByDate = eventsByDate,
            onSelect = onSelect,
            onOpenEvent = onOpenEvent,
        )
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
    val spacing = LocalCozySpacing.current
    val firstOfMonth = LocalDate(cursor.year, cursor.month, 1)
    val firstOffset = ((firstOfMonth.dayOfWeek.ordinal) % 7)
    val gridStart = firstOfMonth.minus(firstOffset, DateTimeUnit.DAY)
    val extras = LocalCozyExtraColors.current
    val today = remember { com.jetbrains.kmpapp.screens.calendar.todayDate() }

    Column(modifier = Modifier.fillMaxSize()) {
        // Weekday headers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.lg, vertical = spacing.xxs),
        ) {
            WEEKDAYS_RU.forEach { d ->
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        d,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = extras.textTer,
                    )
                }
            }
        }

        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = spacing.lg)) {
            for (w in 0 until 6) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (d in 0 until 7) {
                        val date = gridStart.plus(w * 7 + d, DateTimeUnit.DAY)
                        val isCurrentMonth = date.month == cursor.month
                        val isToday = date == today
                        val isSelected = date == selected
                        val dayEvents = eventsByDate[date].orEmpty()
                        DayCell(
                            date = date,
                            isCurrentMonth = isCurrentMonth,
                            isToday = isToday,
                            isSelected = isSelected,
                            events = dayEvents,
                            onClick = { onSelect(date) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(spacing.lg))

        // Section header for selected day
        val dayEvents = eventsByDate[selected].orEmpty()
        SectionLabel(
            text = "${WEEKDAYS_RU_FULL[(selected.dayOfWeek.ordinal) % 7]}, " +
                "${selected.dayOfMonth} ${MONTHS_RU_GEN[selected.monthNumber - 1].uppercase()} · ${dayEvents.size}",
            modifier = Modifier.padding(horizontal = spacing.xxl),
        )
        Spacer(Modifier.height(spacing.xs))

        if (dayEvents.isEmpty()) {
            EmptyDay(modifier = Modifier.padding(horizontal = spacing.xxl, vertical = spacing.xxl))
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                items(dayEvents, key = { it.id }) { ev ->
                    EventRow(ev, onClick = { onOpenEvent(ev) })
                }
                item { Spacer(Modifier.height(96.dp)) }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    isCurrentMonth: Boolean,
    isToday: Boolean,
    isSelected: Boolean,
    events: List<CalendarEvent>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val extras = LocalCozyExtraColors.current
    val bg = when {
        isToday -> MaterialTheme.colorScheme.primary
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        else -> Color.Transparent
    }
    val dayColor = when {
        isToday -> MaterialTheme.colorScheme.onPrimary
        isSelected -> MaterialTheme.colorScheme.primary
        !isCurrentMonth -> MaterialTheme.colorScheme.outline
        else -> MaterialTheme.colorScheme.onBackground
    }
    val dotColors = remember(events, isToday) {
        val out = mutableListOf<Color>()
        if (events.isNotEmpty()) out += if (isToday) Color.White else Color.Unspecified
        if (events.size > 1) out += if (isToday) Color.White else Color.Unspecified
        out
    }

    Box(
        modifier = modifier
            .padding(1.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(4.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                date.dayOfMonth.toString(),
                fontSize = 13.sp,
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Medium,
                color = dayColor,
            )
            Spacer(Modifier.weight(1f))
            // Up to 2 dots
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                if (events.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .background(
                                if (isToday) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.primary,
                                CircleShape,
                            ),
                    )
                }
                if (events.size > 1) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .background(
                                if (isToday) MaterialTheme.colorScheme.onPrimary
                                else extras.lavender,
                                CircleShape,
                            ),
                    )
                }
            }
            Spacer(Modifier.height(2.dp))
        }
    }
}

@Composable
private fun WeekView(
    weekStart: LocalDate,
    selected: LocalDate,
    eventsByDate: Map<LocalDate, List<CalendarEvent>>,
    onSelect: (LocalDate) -> Unit,
    onOpenEvent: (CalendarEvent) -> Unit,
) {
    val spacing = LocalCozySpacing.current
    val days = (0 until 7).map { weekStart.plus(it, DateTimeUnit.DAY) }
    val today = remember { com.jetbrains.kmpapp.screens.calendar.todayDate() }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.lg, vertical = spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(spacing.xxs),
        ) {
            days.forEachIndexed { idx, day ->
                WeekDayCard(
                    weekdayLabel = WEEKDAYS_RU[idx],
                    day = day,
                    events = eventsByDate[day].orEmpty(),
                    isToday = day == today,
                    isSelected = day == selected,
                    onClick = { onSelect(day) },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        val dayEvents = eventsByDate[selected].orEmpty()
        SectionLabel(
            text = "НЕДЕЛЯ · ${days.sumOf { (eventsByDate[it]?.size ?: 0) }} СОБЫТИЙ",
            modifier = Modifier.padding(horizontal = spacing.xxl),
        )
        Spacer(Modifier.height(spacing.xs))

        if (dayEvents.isEmpty()) {
            EmptyDay(modifier = Modifier.padding(horizontal = spacing.xxl, vertical = spacing.xxl))
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(dayEvents, key = { it.id }) { ev ->
                    EventRow(ev, onClick = { onOpenEvent(ev) })
                }
                item { Spacer(Modifier.height(96.dp)) }
            }
        }
    }
}

@Composable
private fun WeekDayCard(
    weekdayLabel: String,
    day: LocalDate,
    events: List<CalendarEvent>,
    isToday: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shapes = LocalCozyShapes.current
    val extras = LocalCozyExtraColors.current
    val bg = when {
        isToday -> MaterialTheme.colorScheme.primary
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surface
    }
    val fg = when {
        isToday -> MaterialTheme.colorScheme.onPrimary
        isSelected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onBackground
    }
    Box(
        modifier = modifier
            .shadow(if (isToday) 6.dp else 2.dp, shapes.chip, clip = false)
            .clip(shapes.chip)
            .background(bg)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                weekdayLabel,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isToday) fg.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                day.dayOfMonth.toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = fg,
            )
            Spacer(Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier.height(5.dp),
            ) {
                if (events.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .background(
                                if (isToday) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.primary,
                                CircleShape,
                            ),
                    )
                }
                if (events.size > 1) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .background(
                                if (isToday) MaterialTheme.colorScheme.onPrimary
                                else extras.lavender,
                                CircleShape,
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun DayView(
    selected: LocalDate,
    eventsByDate: Map<LocalDate, List<CalendarEvent>>,
    onOpenEvent: (CalendarEvent) -> Unit,
) {
    val spacing = LocalCozySpacing.current
    val shapes = LocalCozyShapes.current
    val extras = LocalCozyExtraColors.current
    val dayEvents = eventsByDate[selected].orEmpty()

    Column(modifier = Modifier.fillMaxSize()) {
        // Hero gradient card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.xxl, vertical = spacing.lg)
                .clip(shapes.card)
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            extras.surfaceSoft,
                        ),
                    ),
                )
                .padding(vertical = spacing.lg, horizontal = spacing.lg),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    WEEKDAYS_RU_FULL[(selected.dayOfWeek.ordinal) % 7],
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    selected.dayOfMonth.toString(),
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "${MONTHS_RU_GEN[selected.monthNumber - 1]} ${selected.year}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(spacing.sm))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        "${dayEvents.size} ${pluralEvents(dayEvents.size)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }

        SectionLabel(
            text = "СОБЫТИЯ ДНЯ · ${dayEvents.size}",
            modifier = Modifier.padding(horizontal = spacing.xxl),
        )
        Spacer(Modifier.height(spacing.xs))

        if (dayEvents.isEmpty()) {
            EmptyDay(modifier = Modifier.padding(horizontal = spacing.xxl, vertical = spacing.xxl))
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(dayEvents, key = { it.id }) { ev ->
                    EventRow(ev, onClick = { onOpenEvent(ev) })
                }
                item { Spacer(Modifier.height(96.dp)) }
            }
        }
    }
}

@Composable
private fun EventRow(ev: CalendarEvent, onClick: () -> Unit) {
    val spacing = LocalCozySpacing.current
    val extras = LocalCozyExtraColors.current
    val accent = eventAccent(ev)
    val accentSoft = eventAccentSoft(ev)
    val timeLabel = ev.time?.let {
        "${it.hour.toString().padStart(2, '0')}:${it.minute.toString().padStart(2, '0')}"
    } ?: "—"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.xxl, vertical = spacing.xxs),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier.width(44.dp).padding(top = 8.dp),
            contentAlignment = Alignment.TopStart,
        ) {
            Text(
                timeLabel,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        CozyCard(
            modifier = Modifier.weight(1f),
            onClick = onClick,
            contentPadding = 0.dp,
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                // Accent border-left
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(56.dp)
                        .background(accent),
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                ) {
                    Text(
                        ev.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(accentSoft)
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    ) {
                        Text(
                            sourceLabel(ev.source),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = accent,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier = modifier,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun EmptyDay(modifier: Modifier = Modifier) {
    val extras = LocalCozyExtraColors.current
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "Нет событий",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Нажмите +, чтобы назначить.",
            fontSize = 12.sp,
            color = extras.textTer,
        )
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

@Composable
private fun eventAccent(ev: CalendarEvent): Color {
    val extras = LocalCozyExtraColors.current
    return when (ev.priority) {
        "high" -> MaterialTheme.colorScheme.error
        "medium" -> extras.coral
        "low" -> extras.success
        else -> when (ev.source) {
            EventSource.CHORE -> extras.lavender
            else -> MaterialTheme.colorScheme.primary
        }
    }
}

@Composable
private fun eventAccentSoft(ev: CalendarEvent): Color {
    val extras = LocalCozyExtraColors.current
    return when (ev.priority) {
        "high" -> extras.coralSoft
        "medium" -> extras.coralSoft
        "low" -> extras.ochreSoft
        else -> when (ev.source) {
            EventSource.CHORE -> extras.lavenderSoft
            else -> MaterialTheme.colorScheme.primaryContainer
        }
    }
}

internal fun todayDate(): LocalDate {
    val tz = kotlinx.datetime.TimeZone.currentSystemDefault()
    return kotlinx.datetime.Clock.System.now().toLocalDateTime(tz).date
}
