package com.jetbrains.kmpapp.screens.family

import com.jetbrains.kmpapp.data.categories.Category

enum class RoomStatusFilter { ACTIVE, OVERDUE, DONE }

/**
 * Активные фильтры в FamilyHome → разделе «Комнаты».
 * - priorities/assignees: пустое множество = «не фильтровать».
 * - statuses: всегда применяется; default = только Активные.
 * - "<none>" — приоритет null; "<unassigned>" — assignedTo пустой.
 */
data class RoomFilters(
    val priorities: Set<String> = emptySet(),
    val assignees: Set<String> = emptySet(),
    val statuses: Set<RoomStatusFilter> = setOf(RoomStatusFilter.ACTIVE),
) {
    fun isDefault(): Boolean =
        priorities.isEmpty() &&
            assignees.isEmpty() &&
            statuses == setOf(RoomStatusFilter.ACTIVE)
}

const val PRIORITY_NONE_KEY = "<none>"
const val ASSIGNEE_UNASSIGNED_KEY = "<unassigned>"

/**
 * UI-модель вкладки комнаты. id = name категории (stable как ключ TabRow),
 * id == null соответствует псевдо-вкладке «Все».
 */
data class RoomUi(
    val id: String?,
    val name: String,
    val emoji: String,
)

/** Эвристический клиентский маппинг название комнаты → emoji. */
fun roomEmojiFor(name: String): String {
    val lower = name.lowercase().trim()
    return when {
        "кухн" in lower || "kitchen" in lower -> "🍳"
        "спальн" in lower || "bedroom" in lower -> "🛏"
        "гостин" in lower || "living" in lower -> "🛋"
        "ванн" in lower || "санузел" in lower || "bath" in lower || "toilet" in lower -> "🚿"
        "прихож" in lower || "коридор" in lower || "entry" in lower || "hall" in lower -> "🚪"
        "балкон" in lower || "лоджи" in lower || "balcony" in lower -> "🪴"
        "гараж" in lower || "garage" in lower -> "🚗"
        "детск" in lower || "kid" in lower || "child" in lower -> "🧸"
        "кабинет" in lower || "офис" in lower || "office" in lower -> "💻"
        "сад" in lower || "garden" in lower || "yard" in lower -> "🌿"
        else -> "📁"
    }
}

/** 8 пресетов для выбора иконки при создании комнаты (визуально, на бэкенде не сохраняется). */
val RoomEmojiPresets = listOf("🍳", "🛏", "🛋", "🚿", "🚪", "🪴", "🚗", "🧸", "💻", "📁")

fun List<Category>.toRoomUi(): List<RoomUi> = map { c ->
    RoomUi(id = c.name, name = c.name, emoji = roomEmojiFor(c.name))
}
