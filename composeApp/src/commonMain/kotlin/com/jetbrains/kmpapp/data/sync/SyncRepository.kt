package com.jetbrains.kmpapp.data.sync

import com.jetbrains.kmpapp.auth.TokenStorage
import com.jetbrains.kmpapp.data.lists.ListsApi
import com.jetbrains.kmpapp.data.lists.ListsStorage
import com.jetbrains.kmpapp.data.lists.TodoItemEntity
import com.jetbrains.kmpapp.data.lists.UpdateItemRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Состояние синхронизации для отображения в UI. */
sealed class SyncStatus {
    data object Idle : SyncStatus()
    data object Syncing : SyncStatus()
    data class LastSynced(val timestamp: String) : SyncStatus()
    data class Error(val message: String) : SyncStatus()
}

/** Типы операций в исходящей очереди. */
object OperationType {
    const val UPDATE_ITEM = "UPDATE_ITEM"
    const val DELETE_ITEM = "DELETE_ITEM"
    const val CREATE_LIST = "CREATE_LIST"
    const val UPDATE_LIST = "UPDATE_LIST"
    const val DELETE_LIST = "DELETE_LIST"
}

class SyncRepository(
    private val syncApi: SyncApi,
    private val listsApi: ListsApi,
    private val listsStorage: ListsStorage,
    private val pendingDao: PendingOperationDao,
    private val tokenStorage: TokenStorage,
) {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    // ─────────────────────────────────────────────────────────────────────────
    //  Основная точка входа — полный цикл синхронизации
    // ─────────────────────────────────────────────────────────────────────────

    suspend fun sync() {
        if (_syncStatus.value is SyncStatus.Syncing) return
        _syncStatus.value = SyncStatus.Syncing

        try {
            // 1. Отправляем накопленную очередь исходящих операций
            pushPendingOperations()

            // 2. Получаем изменения с сервера
            val since = tokenStorage.getLastSyncTimestamp() ?: EPOCH_TIMESTAMP
            syncApi.sync(since)
                .onSuccess { response ->
                    mergeItems(response.items)
                    tokenStorage.saveLastSyncTimestamp(response.timestamp)
                    _syncStatus.value = SyncStatus.LastSynced(response.timestamp)
                }
                .onFailure { e ->
                    _syncStatus.value = SyncStatus.Error(e.message ?: "Ошибка синхронизации")
                }
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.Error(e.message ?: "Ошибка синхронизации")
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Pull: объединение серверных изменений с локальной БД
    // ─────────────────────────────────────────────────────────────────────────

    private suspend fun mergeItems(serverItems: List<SyncItemDTO>) {
        for (serverItem in serverItems) {
            if (serverItem.deleted) {
                // Soft delete — удаляем локально
                listsStorage.deleteItemById(serverItem.id)
                continue
            }

            val localItem = listsStorage.getItemById(serverItem.id)
            if (localItem == null) {
                // Новый элемент — вставляем
                listsStorage.upsertItem(serverItem.toEntity())
            } else if (serverItem.version > localItem.version) {
                // Сервер новее — перезаписываем (LWW: last-write-wins по version)
                listsStorage.upsertItem(serverItem.toEntity())
            }
            // Иначе локальная копия новее — пропускаем
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Push: отправка исходящей очереди
    // ─────────────────────────────────────────────────────────────────────────

    private suspend fun pushPendingOperations() {
        val pending = pendingDao.getAll()
        for (op in pending) {
            val success = try {
                when (op.operationType) {
                    OperationType.UPDATE_ITEM -> {
                        val request = json.decodeFromString<UpdateItemRequest>(op.payload)
                        listsApi.updateItem(op.entityId, request).isSuccess
                    }
                    OperationType.DELETE_ITEM -> {
                        listsApi.deleteItem(op.entityId).isSuccess
                    }
                    OperationType.DELETE_LIST -> {
                        listsApi.deleteList(op.entityId).isSuccess
                    }
                    else -> true // Остальные типы обрабатываются онлайн
                }
            } catch (e: Exception) {
                false
            }

            if (success) {
                pendingDao.deleteById(op.id)
            } else {
                pendingDao.incrementRetry(op.id)
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Добавление операций в исходящую очередь (для offline-режима)
    // ─────────────────────────────────────────────────────────────────────────

    suspend fun enqueueUpdateItem(itemId: String, request: UpdateItemRequest) {
        pendingDao.insert(
            PendingOperationEntity(
                id = generateId(),
                operationType = OperationType.UPDATE_ITEM,
                entityId = itemId,
                payload = json.encodeToString(request),
                createdAt = nowIso(),
            )
        )
    }

    suspend fun enqueueDeleteItem(itemId: String) {
        // Удаляем предыдущие pending-операции с этим элементом — они уже не актуальны
        pendingDao.deleteByEntityId(itemId)
        pendingDao.insert(
            PendingOperationEntity(
                id = generateId(),
                operationType = OperationType.DELETE_ITEM,
                entityId = itemId,
                payload = "{}",
                createdAt = nowIso(),
            )
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private fun SyncItemDTO.toEntity() = TodoItemEntity(
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
        version = version,
        shoppingJson = shoppingJson,
        choreScheduleJson = choreScheduleJson,
    )

    companion object {
        const val EPOCH_TIMESTAMP = "1970-01-01T00:00:00Z"
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Платформо-независимые утилиты
// ─────────────────────────────────────────────────────────────────────────────

expect fun nowIso(): String
expect fun generateId(): String
