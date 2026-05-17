package com.jetbrains.kmpapp.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetbrains.kmpapp.auth.AuthRepository
import com.jetbrains.kmpapp.auth.AuthState
import com.jetbrains.kmpapp.data.calendar.toDueInstantOrNull
import com.jetbrains.kmpapp.data.groups.GroupsRepository
import com.jetbrains.kmpapp.data.groups.WorkspaceType
import com.jetbrains.kmpapp.data.lists.ListsRepository
import com.jetbrains.kmpapp.data.profile.ProfileApi
import com.jetbrains.kmpapp.ui.listEmojiForType
import com.jetbrains.kmpapp.ui.models.FamilyMember
import com.jetbrains.kmpapp.ui.models.ListType
import com.jetbrains.kmpapp.ui.models.Palette
import com.jetbrains.kmpapp.ui.models.Role
import com.jetbrains.kmpapp.ui.models.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

class DashboardViewModel(
    private val authRepository: AuthRepository,
    private val listsRepository: ListsRepository,
    private val groupsRepository: GroupsRepository,
    private val profileApi: ProfileApi,
) : ViewModel() {

    private val _displayName = MutableStateFlow<String?>(null)
    private val _context = MutableStateFlow(DashboardContext.FAMILY)

    private val contextWorkspaceIds: StateFlow<Set<String>> =
        combine(groupsRepository.groups, _context) { groups, ctx ->
            groups.filter { ctx.matches(it.type) }.map { it.id }.toSet()
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    private val todayTasks: StateFlow<List<Task>> = combine(
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
        val startOfTomorrow = today.plus(1, DateTimeUnit.DAY).atStartOfDayIn(tz)

        items.asSequence()
            .filter { !it.isDone && it.listId in activeListIds }
            .mapNotNull { item ->
                val due = item.dueAt.toDueInstantOrNull(tz) ?: return@mapNotNull null
                if (due >= startOfTomorrow) return@mapNotNull null
                val list = listsById[item.listId] ?: return@mapNotNull null
                Task(
                    id = item.id,
                    title = item.title,
                    listType = ListType.fromId(list.type),
                    listId = item.listId,
                    isDone = false,
                    isOverdue = due < now,
                    emoji = list.icon ?: listEmojiForType(list.type),
                )
            }
            .sortedBy { it.isOverdue }
            .take(7)
            .toList()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val lastActivity: StateFlow<FamilyActivity?> = combine(
        groupsRepository.groups,
        listsRepository.allItems,
        listsRepository.lists,
    ) { groups, items, lists ->
        val family = groups.firstOrNull {
            it.type == WorkspaceType.FAMILY && it.archivedAt == null
        } ?: return@combine null

        val familyListIds = lists.filter { it.workspaceId == family.id }.map { it.id }.toSet()
        if (familyListIds.isEmpty()) return@combine null

        val recentDone = items.filter { it.isDone && it.listId in familyListIds }
            .maxByOrNull { it.id } ?: return@combine null

        val initials = family.title.split(' ', '-').take(3).mapNotNull { word ->
            word.firstOrNull()?.uppercase()
        }

        FamilyActivity(
            message = "✓ ${recentDone.title}",
            when_ = "недавно",
            members = initials.mapIndexed { i, initial ->
                FamilyMember(
                    id = "m$i",
                    name = initial,
                    initial = initial,
                    role = Role.PARENT,
                    avatarPalette = Palette.entries[i % Palette.entries.size],
                )
            },
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val state: StateFlow<DashboardState> = combine(
        _displayName,
        _context,
        todayTasks,
        lastActivity,
    ) { name, ctx, tasks, activity ->
        val tz = TimeZone.currentSystemDefault()
        val now = Clock.System.now().toLocalDateTime(tz)
        val hour = now.hour
        val greeting = when {
            hour < 6 -> "Доброй ночи"
            hour < 12 -> "Доброе утро"
            hour < 18 -> "Добрый день"
            else -> "Добрый вечер"
        }
        val initial = name?.firstOrNull()?.uppercase() ?: "·"

        DashboardState(
            me = FamilyMember(
                id = "me",
                name = name ?: "",
                initial = initial,
                role = Role.PARENT,
                avatarPalette = Palette.PRIMARY,
            ),
            greetingFirstLine = greeting + ",",
            greetingSecondLine = name ?: "",
            dateLabel = formatDate(now.date),
            context = ctx,
            todayTasks = tasks,
            pet = null,
            petQuote = null,
            lastActivity = activity,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, DashboardState())

    init {
        viewModelScope.launch {
            profileApi.getProfile().onSuccess { _displayName.value = it.displayName }
        }
        viewModelScope.launch {
            if (groupsRepository.groups.value.isEmpty()) groupsRepository.loadGroups()
        }
        viewModelScope.launch {
            if (listsRepository.lists.value.isEmpty()) listsRepository.loadLists()
        }
    }

    fun onIntent(intent: DashboardIntent) {
        when (intent) {
            is DashboardIntent.SwitchContext -> _context.value = intent.context
            else -> {}
        }
    }

    private fun DashboardContext.matches(type: String) = when (this) {
        DashboardContext.PERSONAL -> type == WorkspaceType.PERSONAL
        DashboardContext.FAMILY -> type == WorkspaceType.FAMILY
        DashboardContext.WORK -> type == WorkspaceType.WORK ||
            type == WorkspaceType.MENTORING ||
            type == WorkspaceType.GROUP
    }

    private fun formatDate(date: kotlinx.datetime.LocalDate): String {
        val months = listOf(
            "января", "февраля", "марта", "апреля", "мая", "июня",
            "июля", "августа", "сентября", "октября", "ноября", "декабря"
        )
        val days = listOf("пн", "вт", "ср", "чт", "пт", "сб", "вс")
        val dow = days[date.dayOfWeek.ordinal]
        return "$dow, ${date.dayOfMonth} ${months[date.monthNumber - 1]}"
    }
}
