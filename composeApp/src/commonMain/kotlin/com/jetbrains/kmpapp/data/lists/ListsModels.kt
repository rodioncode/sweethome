package com.jetbrains.kmpapp.data.lists

import kotlinx.serialization.Serializable

@Serializable
data class TodoList(
    val id: String,
    val type: String, // shopping, home_chores, general_todos
    val title: String,
    val icon: String? = null,
    val scope: String, // personal, group
    val ownerUserId: String? = null,
    val ownerGroupId: String? = null,
    val createdBy: String,
    val createdAt: String,
    val archivedAt: String? = null,
)

@Serializable
data class TodoItem(
    val id: String,
    val listId: String,
    val title: String,
    val note: String? = null,
    val sortOrder: Double? = null,
    val isDone: Boolean,
    val doneAt: String? = null,
    val createdBy: String,
    val createdAt: String,
    val updatedAt: String,
    val assignedTo: String? = null,
    val dueAt: String? = null,
    val isFavorite: Boolean = false,
    val version: Int = 1,
    val shopping: ShoppingItemFields? = null,
    val choreSchedule: ChoreSchedule? = null,
)

@Serializable
data class ShoppingItemFields(
    val quantity: Double? = null,
    val unit: String? = null,
    val category: String? = null,
)

@Serializable
data class ChoreSchedule(
    val intervalDays: Int? = null,
    val daysOfWeek: List<String>? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val lastDoneAt: String? = null,
    val category: String? = null,
)

@Serializable
data class ListsWrapper(
    val lists: List<TodoList>,
)

@Serializable
data class ListWithItemsWrapper(
    val list: TodoList,
    val items: List<TodoItem>,
)

@Serializable
data class CreateListRequest(
    val type: String,
    val title: String,
    val icon: String? = null,
    val scope: String,
    val groupId: String? = null,
)

@Serializable
data class UpdateListRequest(
    val title: String? = null,
    val icon: String? = null,
    val archived: Boolean? = null,
)

@Serializable
data class CreateItemRequest(
    val title: String,
    val note: String? = null,
    val sortOrder: Double? = null,
    val assignedTo: String? = null,
    val dueAt: String? = null,
    val isFavorite: Boolean? = null,
    val shopping: ShoppingItemFields? = null,
    val choreSchedule: ChoreSchedule? = null,
)

@Serializable
data class UpdateItemRequest(
    val title: String? = null,
    val note: String? = null,
    val sortOrder: Double? = null,
    val isDone: Boolean? = null,
    // null = не менять, "" = сбросить в null, uuid = назначить
    val assignedTo: String? = null,
    // null = не менять, "" = сбросить дедлайн
    val dueAt: String? = null,
    val isFavorite: Boolean? = null,
    val shopping: ShoppingItemFields? = null,
    val choreSchedule: ChoreSchedule? = null,
)
