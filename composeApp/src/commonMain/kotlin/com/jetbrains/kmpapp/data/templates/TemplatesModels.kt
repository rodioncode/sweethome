package com.jetbrains.kmpapp.data.templates

import com.jetbrains.kmpapp.data.lists.ChoreSchedule
import com.jetbrains.kmpapp.data.lists.MediaItemFields
import com.jetbrains.kmpapp.data.lists.ShoppingItemFields
import kotlinx.serialization.Serializable

object TemplateVisibility {
    const val PRIVATE = "private"
    const val PENDING = "pending"
    const val PUBLIC = "public"
}

object TemplateKind {
    const val LIST = "list"
    const val TASK = "task"
}

// ─── List templates ────────────────────────────────────────────────────────

@Serializable
data class ListTemplate(
    val id: String,
    val scope: String,                      // shopping, home_chores, general_todos, study, travel, custom, media
    val category: String = "",
    val isSystem: Boolean = false,
    val userId: String? = null,             // null = system или скрыто
    val visibility: String = TemplateVisibility.PUBLIC,
    val title: String,
    val description: String? = null,
    val isFavorite: Boolean = false,
)

@Serializable
data class TemplateListItem(
    val id: String,
    val sortOrder: Int = 0,
    val title: String,
    val note: String? = null,
    val priority: String? = null,           // high | medium | low
    val reward: Int? = null,
    val shoppingDetails: ShoppingItemFields? = null,
    val choreSchedule: ChoreSchedule? = null,
    val mediaDetails: MediaItemFields? = null,
)

@Serializable
data class ListTemplateDetail(
    val id: String,
    val scope: String,
    val category: String = "",
    val isSystem: Boolean = false,
    val userId: String? = null,
    val visibility: String = TemplateVisibility.PUBLIC,
    val title: String,
    val description: String? = null,
    val isFavorite: Boolean = false,
    val items: List<TemplateListItem> = emptyList(),
)

// ─── Task templates ────────────────────────────────────────────────────────

@Serializable
data class TaskTemplate(
    val id: String,
    val scope: String,
    val isSystem: Boolean = false,
    val userId: String? = null,
    val visibility: String = TemplateVisibility.PUBLIC,
    val title: String,
    val description: String? = null,
    val isFavorite: Boolean = false,
)

/**
 * Деталь шаблона задачи. Это «одна задача» — поля параллельны полям TodoItem,
 * за вычетом статусных (assignedTo, isDone, dueAt, version).
 */
@Serializable
data class TaskTemplateDetail(
    val id: String,
    val scope: String,
    val isSystem: Boolean = false,
    val userId: String? = null,
    val visibility: String = TemplateVisibility.PUBLIC,
    val title: String,
    val description: String? = null,
    val isFavorite: Boolean = false,
    val note: String? = null,
    val priority: String? = null,
    val reward: Int? = null,
    val shoppingDetails: ShoppingItemFields? = null,
    val choreSchedule: ChoreSchedule? = null,
    val mediaDetails: MediaItemFields? = null,
)

// ─── Wrappers ──────────────────────────────────────────────────────────────

@Serializable
data class ListTemplatesWrapper(val templates: List<ListTemplate> = emptyList())

@Serializable
data class TaskTemplatesWrapper(val templates: List<TaskTemplate> = emptyList())

// ─── Use requests ──────────────────────────────────────────────────────────

@Serializable
data class TemplateItemPatch(
    val title: String? = null,
    val note: String? = null,
    val priority: String? = null,
    val reward: Int? = null,
    val shoppingDetails: ShoppingItemFields? = null,
    val choreSchedule: ChoreSchedule? = null,
    val mediaDetails: MediaItemFields? = null,
)

@Serializable
data class TemplateListItemInput(
    val sortOrder: Int = 0,
    val title: String,
    val note: String? = null,
    val priority: String? = null,
    val reward: Int? = null,
    val shoppingDetails: ShoppingItemFields? = null,
    val choreSchedule: ChoreSchedule? = null,
    val mediaDetails: MediaItemFields? = null,
)

@Serializable
data class ListTemplateOverrides(
    val title: String? = null,
    val excludeItemIds: List<String> = emptyList(),
    val perItemEdits: Map<String, TemplateItemPatch> = emptyMap(),
    val appendItems: List<TemplateListItemInput> = emptyList(),
)

@Serializable
data class UseListTemplateRequest(
    val workspaceId: String,
    val title: String,
    val locale: String = "ru",
    val overrides: ListTemplateOverrides? = null,
)

@Serializable
data class TaskTemplateOverrides(
    val title: String? = null,
    val note: String? = null,
    val priority: String? = null,
    val reward: Int? = null,
    val assignedTo: String? = null,
    val dueAt: String? = null,
    val shoppingDetails: ShoppingItemFields? = null,
    val choreSchedule: ChoreSchedule? = null,
    val mediaDetails: MediaItemFields? = null,
)

@Serializable
data class UseTaskTemplateRequest(
    val listId: String,
    val locale: String = "ru",
    val overrides: TaskTemplateOverrides? = null,
)

// ─── Save list as template ─────────────────────────────────────────────────

@Serializable
data class SaveAsTemplateRequest(
    val category: String,
    val locale: String = "ru",
    val title: String,
    val description: String? = null,
    val includeItemIds: List<String>? = null,
)
