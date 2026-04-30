package com.jetbrains.kmpapp.data.sync

import com.jetbrains.kmpapp.data.lists.ChoreSchedule
import com.jetbrains.kmpapp.data.lists.MediaItemFields
import com.jetbrains.kmpapp.data.lists.ShoppingItemFields
import com.jetbrains.kmpapp.data.lists.TodoItem
import com.jetbrains.kmpapp.data.lists.WishlistItemFields
import kotlinx.serialization.Serializable

@Serializable
data class SyncItem(
    val id: String,
    val listId: String,
    val title: String,
    val note: String? = null,
    val sortOrder: Double? = null,
    val isDone: Boolean = false,
    val doneAt: String? = null,
    val createdBy: String = "",
    val createdAt: String = "",
    val updatedAt: String = "",
    val assignedTo: String? = null,
    val dueAt: String? = null,
    val isFavorite: Boolean = false,
    val priority: String? = null,
    val reward: String? = null,
    val version: Int = 0,
    val shopping: ShoppingItemFields? = null,
    val choreSchedule: ChoreSchedule? = null,
    val media: MediaItemFields? = null,
    val wishlist: WishlistItemFields? = null,
    val deleted: Boolean = false,
)

fun SyncItem.toTodoItem() = TodoItem(
    id = id,
    listId = listId,
    title = title,
    note = note,
    sortOrder = sortOrder,
    isDone = isDone,
    doneAt = doneAt,
    createdBy = createdBy,
    createdAt = createdAt,
    updatedAt = updatedAt,
    assignedTo = assignedTo,
    dueAt = dueAt,
    isFavorite = isFavorite,
    priority = priority,
    reward = reward,
    version = version,
    shopping = shopping,
    choreSchedule = choreSchedule,
    media = media,
    wishlist = wishlist,
)

@Serializable
data class SyncWrapper(
    val items: List<SyncItem>,
    val timestamp: String,
)
