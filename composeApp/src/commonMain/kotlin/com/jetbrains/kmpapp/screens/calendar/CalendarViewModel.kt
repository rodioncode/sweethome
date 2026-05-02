package com.jetbrains.kmpapp.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetbrains.kmpapp.data.calendar.CalendarEvent
import com.jetbrains.kmpapp.data.calendar.EventSource
import com.jetbrains.kmpapp.data.calendar.expandItemsToEvents
import com.jetbrains.kmpapp.data.calendar.groupByDate
import com.jetbrains.kmpapp.data.lists.ListsRepository
import com.jetbrains.kmpapp.data.lists.TodoList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

enum class CalendarView { MONTH, WEEK, AGENDA }

data class CalendarFilters(
    val sources: Set<EventSource> = setOf(EventSource.TASK, EventSource.CHORE),
    val priorities: Set<String>? = null,    // null = все
    val listIds: Set<String>? = null,       // null = все
)

class CalendarViewModel(
    private val listsRepository: ListsRepository,
) : ViewModel() {

    private val tz = TimeZone.currentSystemDefault()
    private val today: LocalDate = Clock.System.now().toLocalDateTime(tz).date

    private val _view = MutableStateFlow(CalendarView.MONTH)
    val view: StateFlow<CalendarView> = _view.asStateFlow()

    private val _cursor = MutableStateFlow(today)            // текущий месяц/период
    val cursor: StateFlow<LocalDate> = _cursor.asStateFlow()

    private val _selected = MutableStateFlow(today)
    val selected: StateFlow<LocalDate> = _selected.asStateFlow()

    private val _filters = MutableStateFlow(CalendarFilters())
    val filters: StateFlow<CalendarFilters> = _filters.asStateFlow()

    val lists: StateFlow<List<TodoList>> = listsRepository.lists
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** Все items без dueAt — кандидаты на назначение в календарь. */
    val unscheduledItems: StateFlow<List<com.jetbrains.kmpapp.data.lists.TodoItem>> = listsRepository.allItems
        .let { src ->
            kotlinx.coroutines.flow.flow {
                src.collect { items -> emit(items.filter { it.dueAt == null && !it.isDone }) }
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        // Подгружаем списки/items при первом старте, чтобы кэш Room заполнился.
        viewModelScope.launch { listsRepository.loadLists() }
    }

    /** Все события за расширенный диапазон вокруг cursor (3 месяца). */
    private val rawEvents: StateFlow<List<CalendarEvent>> = combine(
        listsRepository.allItems, _cursor,
    ) { items, cursor ->
        val from = cursor.minus(45, DateTimeUnit.DAY)
        val to = cursor.plus(75, DateTimeUnit.DAY)
        expandItemsToEvents(items, from, to, tz)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val events: StateFlow<List<CalendarEvent>> = combine(rawEvents, _filters) { evs, f ->
        evs.filter { ev ->
            ev.source in f.sources
                && (f.priorities == null || ev.priority in f.priorities)
                && (f.listIds == null || ev.listId in f.listIds)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val eventsByDate: StateFlow<Map<LocalDate, List<CalendarEvent>>> = events
        .let { src ->
            kotlinx.coroutines.flow.flow {
                src.collect { emit(groupByDate(it)) }
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    fun setView(v: CalendarView) { _view.value = v }
    fun setCursor(d: LocalDate) { _cursor.value = d }
    fun setSelected(d: LocalDate) { _selected.value = d }
    fun goToday() { _cursor.value = today; _selected.value = today }

    fun setFilters(f: CalendarFilters) { _filters.value = f }
    fun toggleSource(s: EventSource) {
        val cur = _filters.value.sources
        val next = if (s in cur) cur - s else cur + s
        if (next.isNotEmpty()) _filters.value = _filters.value.copy(sources = next)
    }
    fun togglePriority(p: String) {
        val cur = _filters.value.priorities ?: setOf("high", "medium", "low")
        val next = if (p in cur) cur - p else cur + p
        _filters.value = _filters.value.copy(
            priorities = if (next.size == 3) null else next.takeIf { it.isNotEmpty() } ?: setOf("high", "medium", "low"),
        )
    }
    fun toggleList(id: String) {
        val all = lists.value.map { it.id }.toSet()
        val cur = _filters.value.listIds ?: all
        val next = if (id in cur) cur - id else cur + id
        _filters.value = _filters.value.copy(
            listIds = if (next == all) null else next.takeIf { it.isNotEmpty() } ?: all,
        )
    }
    fun resetFilters() { _filters.value = CalendarFilters() }

    /** Назначить date+time существующему item. */
    fun assignDate(itemId: String, date: LocalDate, hour: Int? = null, minute: Int? = null) {
        viewModelScope.launch {
            val iso = if (hour != null && minute != null) {
                // ISO8601 в UTC (бэк парсит RFC3339)
                val dt = kotlinx.datetime.LocalDateTime(date, kotlinx.datetime.LocalTime(hour, minute))
                dt.toInstant(tz).toString()
            } else {
                // Весь день — полночь по локали → UTC
                kotlinx.datetime.LocalDateTime(date, kotlinx.datetime.LocalTime(0, 0))
                    .toInstant(tz).toString()
            }
            listsRepository.updateItem(itemId = itemId, dueAt = iso)
        }
    }
}
