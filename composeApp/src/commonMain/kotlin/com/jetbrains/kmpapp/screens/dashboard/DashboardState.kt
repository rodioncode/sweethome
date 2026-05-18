package com.jetbrains.kmpapp.screens.dashboard

import com.jetbrains.kmpapp.ui.components.CozyTab
import com.jetbrains.kmpapp.ui.models.FamilyMember
import com.jetbrains.kmpapp.ui.models.Pet
import com.jetbrains.kmpapp.ui.models.Task

data class DashboardState(
    val me: FamilyMember? = null,
    val greetingFirstLine: String = "",
    val greetingSecondLine: String = "",
    val dateLabel: String = "",
    val context: DashboardContext = DashboardContext.FAMILY,
    val todayTasks: List<Task> = emptyList(),
    val pet: Pet? = null,
    val petQuote: String? = null,
    val lastActivity: FamilyActivity? = null,
    val isLoading: Boolean = false,
)

enum class DashboardContext(val id: String, val label: String) {
    PERSONAL("personal", "Личное"),
    FAMILY  ("family",   "Семья"),
    WORK    ("work",     "Работа"),
}

data class FamilyActivity(
    val message: String,
    val when_: String,
    val members: List<FamilyMember>,
)

sealed interface DashboardIntent {
    data class OpenTask(val taskId: String) : DashboardIntent
    data class NavTab(val tab: CozyTab) : DashboardIntent
    data object OpenPet : DashboardIntent
    data object OpenProfile : DashboardIntent
    data object Add : DashboardIntent
    data class SwitchContext(val context: DashboardContext) : DashboardIntent
}
