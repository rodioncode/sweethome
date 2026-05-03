package com.jetbrains.kmpapp.data.lists

import kotlinx.serialization.Serializable

@Serializable
data class TodoList(
    val id: String,
    val workspaceId: String,
    val type: String, // shopping, home_chores, general_todos, study, travel, custom, media, wishlist
    val customTypeLabel: String? = null,
    val title: String,
    val icon: String? = null,
    val color: String? = null,
    val description: String? = null,
    val createdBy: String,
    val createdAt: String,
    val archivedAt: String? = null,
    val isPublic: Boolean = false,
    val publicToken: String? = null,
    val totalCount: Int? = null,
    val doneCount: Int? = null,
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
    val shopping: ShoppingItemFields? = null,
    val choreSchedule: ChoreSchedule? = null,
    val media: MediaItemFields? = null,
    val wishlist: WishlistItemFields? = null,
    val assignedTo: String? = null,
    val dueAt: String? = null,
    val isFavorite: Boolean = false,
    val priority: String? = null, // "high" | "medium" | "low" | null
    val reward: String? = null,
    val version: Int = 0,
)

@Serializable
data class ShoppingItemFields(
    val quantity: Double? = null,
    val unit: String? = null,
    val category: String? = null,
    val brand: String? = null,
    val productUrl: String? = null,
    val imageUrl: String? = null,
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
data class MediaItemFields(
    val mediaType: String? = null, // book, movie, series, game, article, podcast, other
    val status: String? = null,    // want, in_progress, done
    val url: String? = null,
    val rating: Int? = null,
    val author: String? = null,
)

@Serializable
data class WishlistItemFields(
    val url: String? = null,
    val imageUrl: String? = null,
    val price: String? = null,
    val isClaimed: Boolean = false,
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
    val workspaceId: String,
    val type: String,
    val title: String,
    val icon: String? = null,
    val color: String? = null,
    val description: String? = null,
    val customTypeLabel: String? = null,
)

@Serializable
data class UpdateListRequest(
    val title: String? = null,
    val icon: String? = null,
    val color: String? = null,
    val description: String? = null,
    val customTypeLabel: String? = null,
    val archived: Boolean? = null,
    val isPublic: Boolean? = null,
)

@Serializable
data class CreateItemRequest(
    val title: String,
    val note: String? = null,
    val sortOrder: Double? = null,
    val shopping: ShoppingItemFields? = null,
    val choreSchedule: ChoreSchedule? = null,
    val media: MediaItemFields? = null,
    val wishlist: WishlistItemFields? = null,
    val assignedTo: String? = null,
    val dueAt: String? = null,
    val isFavorite: Boolean? = null,
    val priority: String? = null,
    val reward: String? = null,
)

@Serializable
data class UpdateItemRequest(
    val title: String? = null,
    val note: String? = null,
    val sortOrder: Double? = null,
    val isDone: Boolean? = null,
    val shopping: ShoppingItemFields? = null,
    val choreSchedule: ChoreSchedule? = null,
    val media: MediaItemFields? = null,
    val wishlist: WishlistItemFields? = null,
    // null = не менять; "" (пустая строка) = сбросить в NULL на сервере
    val assignedTo: String? = null,
    val dueAt: String? = null,
    val isFavorite: Boolean? = null,
    val priority: String? = null,
    val reward: String? = null,
)
