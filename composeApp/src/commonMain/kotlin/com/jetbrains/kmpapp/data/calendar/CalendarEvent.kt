package com.jetbrains.kmpapp.data.calendar

import com.jetbrains.kmpapp.data.lists.TodoItem
import com.jetbrains.kmpapp.data.lists.TodoList
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.Instant

enum class EventSource { TASK, CHORE }

data class CalendarEvent(
    val id: String,         // <itemId>@<dateIso> — стабилен в рамках экспансии
    val itemId: String,
    val listId: String,
    val date: LocalDate,
    val time: LocalDateTime?, // null = весь день
    val title: String,
    val priority: String?,    // "high" | "medium" | "low" | null
    val source: EventSource,
    val isDone: Boolean,
    val assignedTo: String?,
)

private fun parseIsoDate(iso: String?): LocalDate? = try {
    if (iso == null) null
    else if (iso.length >= 10) LocalDate.parse(iso.substring(0, 10))
    else null
} catch (_: Throwable) { null }

/**
 * Парсит ISO-инстант ("2026-05-04T15:00:00Z") или date-only ("2026-05-04") в Instant.
 * Date-only трактуется как полночь в указанном часовом поясе.
 */
fun String?.toDueInstantOrNull(tz: TimeZone = TimeZone.currentSystemDefault()): Instant? {
    if (this == null) return null
    return try {
        Instant.parse(this)
    } catch (_: Throwable) {
        parseIsoDate(this)?.atStartOfDayIn(tz)
    }
}

private fun parseInstant(iso: String?, tz: TimeZone): Instant? = iso.toDueInstantOrNull(tz)

private val DAY_OF_WEEK_MAP = mapOf(
    "mon" to DayOfWeek.MONDAY, "tue" to DayOfWeek.TUESDAY, "wed" to DayOfWeek.WEDNESDAY,
    "thu" to DayOfWeek.THURSDAY, "fri" to DayOfWeek.FRIDAY, "sat" to DayOfWeek.SATURDAY, "sun" to DayOfWeek.SUNDAY,
)

/**
 * Раскрывает items в события календаря для диапазона [from..to] (включительно).
 *
 * Источники:
 *  - dueAt → одиночное событие (TASK).
 *  - choreSchedule → серия событий (CHORE), генерим по `intervalDays` от lastDoneAt/startDate
 *    или по `daysOfWeek` (ближайшие совпадения внутри диапазона).
 */
fun expandItemsToEvents(
    items: List<TodoItem>,
    from: LocalDate,
    to: LocalDate,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): List<CalendarEvent> {
    val out = mutableListOf<CalendarEvent>()

    for (item in items) {
        // Single dueAt
        parseInstant(item.dueAt, timeZone)?.let { instant ->
            val dt = instant.toLocalDateTime(timeZone)
            val date = dt.date
            if (date in from..to) {
                out += CalendarEvent(
                    id = "${item.id}@$date",
                    itemId = item.id,
                    listId = item.listId,
                    date = date,
                    time = dt,
                    title = item.title,
                    priority = item.priority,
                    source = EventSource.TASK,
                    isDone = item.isDone,
                    assignedTo = item.assignedTo,
                )
            }
        }

        // Chore schedule
        item.choreSchedule?.let { sched ->
            val schedStart = parseIsoDate(sched.startDate)
            val schedEnd = parseIsoDate(sched.endDate)
            val lastDone = parseInstant(sched.lastDoneAt, timeZone)?.toLocalDateTime(timeZone)?.date

            // intervalDays — генерируем серию от base даты
            sched.intervalDays?.takeIf { it > 0 }?.let { interval ->
                val base = lastDone ?: schedStart ?: from
                // Найти первое событие на/после from
                var d = base
                if (d < from) {
                    val daysGap = from.toEpochDays() - d.toEpochDays()
                    val steps = (daysGap + interval - 1) / interval
                    d = LocalDate.fromEpochDays(d.toEpochDays() + steps * interval)
                }
                while (d <= to) {
                    if ((schedEnd == null || d <= schedEnd) && (schedStart == null || d >= schedStart)) {
                        out += CalendarEvent(
                            id = "${item.id}@$d",
                            itemId = item.id,
                            listId = item.listId,
                            date = d,
                            time = null,
                            title = item.title,
                            priority = item.priority,
                            source = EventSource.CHORE,
                            isDone = false,
                            assignedTo = item.assignedTo,
                        )
                    }
                    d = LocalDate.fromEpochDays(d.toEpochDays() + interval)
                }
            }

            // daysOfWeek — все дни недели в диапазоне
            sched.daysOfWeek?.mapNotNull { DAY_OF_WEEK_MAP[it] }?.toSet()?.takeIf { it.isNotEmpty() }?.let { dows ->
                var d = from
                while (d <= to) {
                    if (d.dayOfWeek in dows
                        && (schedEnd == null || d <= schedEnd)
                        && (schedStart == null || d >= schedStart)
                    ) {
                        out += CalendarEvent(
                            id = "${item.id}@$d",
                            itemId = item.id,
                            listId = item.listId,
                            date = d,
                            time = null,
                            title = item.title,
                            priority = item.priority,
                            source = EventSource.CHORE,
                            isDone = false,
                            assignedTo = item.assignedTo,
                        )
                    }
                    d = LocalDate.fromEpochDays(d.toEpochDays() + 1)
                }
            }
        }
    }

    return out.sortedWith(compareBy({ it.date }, { it.time?.hour ?: -1 }, { it.title }))
}

fun groupByDate(events: List<CalendarEvent>): Map<LocalDate, List<CalendarEvent>> =
    events.groupBy { it.date }
