package com.jetbrains.kmpapp.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetbrains.kmpapp.auth.AuthRepository
import com.jetbrains.kmpapp.auth.AuthState
import com.jetbrains.kmpapp.data.goals.Goal
import com.jetbrains.kmpapp.data.goals.GoalsApi
import com.jetbrains.kmpapp.data.groups.GroupsRepository
import com.jetbrains.kmpapp.data.groups.WorkspaceType
import com.jetbrains.kmpapp.data.lists.ListsRepository
import com.jetbrains.kmpapp.data.lists.TodoList
import com.jetbrains.kmpapp.data.profile.ProfileApi
import com.jetbrains.kmpapp.ui.listEmojiForType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

enum class DashboardContext { PERSONAL, WORK }

data class TodayTaskUi(
    val id: String,
    val listId: String,
    val title: String,
    val listTitle: String,
    val listIcon: String,
    val listType: String,
    val listColorHex: String?,
    val isOverdue: Boolean,
    val dueLabel: String,
    val priority: String?,
)

/** UI-карточка ближайшей цели для виджета на Dashboard. */
data class NearestGoalUi(
    val id: String,
    val workspaceId: String,
    val title: String,
    val deadlineLabel: String?,
    val isOverdue: Boolean,
    val totalSteps: Int,
    val doneSteps: Int,
)

class HomeViewModel(
    private val authRepository: AuthRepository,
    private val listsRepository: ListsRepository,
    private val groupsRepository: GroupsRepository,
    private val profileApi: ProfileApi,
    private val goalsApi: GoalsApi,
) : ViewModel() {

    val isGuest: StateFlow<Boolean> = authRepository.authState
        .map { (it as? AuthState.Authenticated)?.isGuest ?: false }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val userId: StateFlow<String?> = authRepository.authState
        .map { (it as? AuthState.Authenticated)?.userId }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _displayName = MutableStateFlow<String?>(null)
    val displayName: StateFlow<String?> = _displayName.asStateFlow()

    private val _context = MutableStateFlow(DashboardContext.PERSONAL)
    val context: StateFlow<DashboardContext> = _context.asStateFlow()

    fun setContext(value: DashboardContext) {
        _context.value = value
    }

    /** workspaceId-ы, попадающие в текущий контекст (PERSONAL/WORK). */
    private val contextWorkspaceIds: StateFlow<Set<String>> =
        combine(groupsRepository.groups, _context) { groups, ctx ->
            groups.filter { ctx.matches(it.type) }.map { it.id }.toSet()
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    val recentLists: StateFlow<List<TodoList>> =
        combine(listsRepository.lists, contextWorkspaceIds) { lists, ids ->
            val visible = lists.filter { it.archivedAt == null }
            val scoped = if (ids.isEmpty()) visible else visible.filter { it.workspaceId in ids }
            scoped.take(3)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val todayTasks: StateFlow<List<TodayTaskUi>> = combine(
        listsRepository.lists,
        listsRepository.allItems,
        contextWorkspaceIds,
    ) { lists, items, ctxIds ->
        val activeLists = lists.filter { it.archivedAt == null }
        val listsById = activeLists.associateBy { it.id }
        val activeListIds = if (ctxIds.isEmpty()) {
            activeLists.map { it.id }.toSet()
        } else {
            activeLists.filter { it.workspaceId in ctxIds }.map { it.id }.toSet()
        }
        if (activeListIds.isEmpty()) return@combine emptyList()

        val tz = TimeZone.currentSystemDefault()
        val now = Clock.System.now()
        val today = now.toLocalDateTime(tz).date
        val startOfTomorrow: Instant = today.plus(1, DateTimeUnit.DAY).atStartOfDayIn(tz)

        items.asSequence()
            .filter { !it.isDone && it.listId in activeListIds }
            .mapNotNull { item ->
                val due = item.dueAt?.toInstantOrNull() ?: return@mapNotNull null
                if (due >= startOfTomorrow) return@mapNotNull null
                val list = listsById[item.listId] ?: return@mapNotNull null
                Triple(item, due, list)
            }
            .sortedBy { it.second }
            .take(7)
            .map { (item, due, list) ->
                TodayTaskUi(
                    id = item.id,
                    listId = item.listId,
                    title = item.title,
                    listTitle = list.title,
                    listIcon = list.icon ?: listEmojiForType(list.type),
                    listType = list.type,
                    listColorHex = list.color,
                    isOverdue = due < now,
                    dueLabel = formatDueLabel(due, today, tz),
                    priority = item.priority,
                )
            }
            .toList()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /**
     * Все активные цели (не isDone, не архив) по всем доступным workspaces.
     * Кеш HomeVM локальный — не трогает GoalsRepository (которая управляется GoalsScreen).
     */
    private val _allGoals = MutableStateFlow<List<Goal>>(emptyList())

    /** Ближайшая цель в текущем контексте. */
    val nearestGoal: StateFlow<NearestGoalUi?> = combine(
        _allGoals, contextWorkspaceIds,
    ) { goals, ctxIds ->
        val tz = TimeZone.currentSystemDefault()
        val today = Clock.System.now().toLocalDateTime(tz).date
        goals.asSequence()
            .filter { !it.isDone && it.archivedAt == null }
            .filter { ctxIds.isEmpty() || it.workspaceId in ctxIds }
            .mapNotNull { goal ->
                val deadlineDate = goal.deadline?.toLocalDateOrNull()
                goal to deadlineDate
            }
            .sortedWith(
                // Сначала с ближайшим дедлайном, потом без дедлайна (deadline = null уходит в конец).
                compareBy(nullsLast()) { it.second },
            )
            .firstOrNull()
            ?.let { (goal, deadlineDate) ->
                val isOverdue = deadlineDate != null && deadlineDate < today
                NearestGoalUi(
                    id = goal.id,
                    workspaceId = goal.workspaceId,
                    title = goal.title,
                    deadlineLabel = deadlineDate?.let { formatGoalDeadline(it, today) },
                    isOverdue = isOverdue,
                    totalSteps = goal.steps.size,
                    doneSteps = goal.steps.count { it.isDone },
                )
            }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    init {
        viewModelScope.launch {
            profileApi.getProfile().onSuccess { _displayName.value = it.displayName }
        }
        viewModelScope.launch {
            // Workspace-список нужен для фильтрации по контексту; подгружаем, если ещё не загружен.
            if (groupsRepository.groups.value.isEmpty()) {
                groupsRepository.loadGroups()
            }
            // После загрузки workspaces — тянем цели по каждому параллельно, агрегируем локально.
            val ids = groupsRepository.groups.value.filter { it.archivedAt == null }.map { it.id }
            val collected = mutableListOf<Goal>()
            ids.forEach { id ->
                goalsApi.listGoals(id).onSuccess { collected += it }
            }
            _allGoals.value = collected
        }
        viewModelScope.launch {
            // Items для секции «Сегодня» агрегируются из локального кеша Room — заполним его, если пуст.
            if (listsRepository.lists.value.isEmpty()) {
                listsRepository.loadLists()
            }
        }
    }

    private fun DashboardContext.matches(type: String) = when (this) {
        DashboardContext.PERSONAL -> type == WorkspaceType.PERSONAL || type == WorkspaceType.FAMILY
        DashboardContext.WORK -> type == WorkspaceType.WORK ||
            type == WorkspaceType.MENTORING ||
            type == WorkspaceType.GROUP
    }
}

private fun String.toInstantOrNull(): Instant? = try {
    Instant.parse(this)
} catch (_: Throwable) {
    null
}

private fun formatDueLabel(due: Instant, today: LocalDate, tz: TimeZone): String {
    val ldt = due.toLocalDateTime(tz)
    val time = "${ldt.hour.toString().padStart(2, '0')}:${ldt.minute.toString().padStart(2, '0')}"
    val daysLate = ldt.date.daysUntil(today)
    return when {
        daysLate >= 2 -> "Просрочено ${daysLate}д"
        daysLate == 1 -> "Вчера, $time"
        ldt.date == today -> "Сегодня, $time"
        else -> time
    }
}

private fun String.toLocalDateOrNull(): LocalDate? = try {
    LocalDate.parse(this.take(10))
} catch (_: Throwable) {
    null
}

private fun formatGoalDeadline(deadline: LocalDate, today: LocalDate): String {
    val days = today.daysUntil(deadline)
    return when {
        days < 0 -> "Просрочено ${-days}д"
        days == 0 -> "Сегодня"
        days == 1 -> "Завтра"
        days < 7 -> "Через ${days}д"
        days < 30 -> "Через ${days / 7}нед"
        else -> "До $deadline"
    }
}
