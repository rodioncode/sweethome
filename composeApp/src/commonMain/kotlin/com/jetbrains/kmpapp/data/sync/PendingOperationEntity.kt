package com.jetbrains.kmpapp.data.sync

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Исходящая очередь операций для offline-режима.
 * Операции накапливаются при отсутствии связи и отправляются при следующей синхронизации.
 */
@Entity(tableName = "pending_operations")
data class PendingOperationEntity(
    @PrimaryKey val id: String,        // UUID операции
    val operationType: String,          // CREATE_ITEM | UPDATE_ITEM | DELETE_ITEM | CREATE_LIST | UPDATE_LIST | DELETE_LIST
    val entityId: String,               // ID элемента/списка
    val listId: String? = null,         // Для операций с items
    val payload: String,                // JSON-тело запроса
    val createdAt: String,              // RFC3339 — для сортировки при отправке
    val retryCount: Int = 0,            // Кол-во неудачных попыток
)
