package com.jetbrains.kmpapp.data.sync

import kotlinx.serialization.Serializable

/**
 * Элемент из ответа GET /sync — полный itemDTO + флаг удаления.
 */
@Serializable
data class SyncItemDTO(
    val id: String,
    val listId: String,
    val title: String,
    val note: String? = null,
    val sortOrder: Double? = null,
    val isDone: Boolean = false,
    val doneAt: String? = null,
    val createdBy: String,
    val createdAt: String,
    val updatedAt: String,
    val assignedTo: String? = null,
    val dueAt: String? = null,
    val isFavorite: Boolean = false,
    val version: Int = 1,
    val shoppingJson: String? = null,
    val choreScheduleJson: String? = null,
    // true = элемент удалён на сервере (soft delete)
    val deleted: Boolean = false,
)

@Serializable
data class SyncResponse(
    val items: List<SyncItemDTO>,
    // Использовать как `since` при следующем вызове
    val timestamp: String,
)

@Serializable
data class SyncWrapper(
    val items: List<SyncItemDTO> = emptyList(),
    val timestamp: String,
)
