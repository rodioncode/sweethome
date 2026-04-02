package com.jetbrains.kmpapp.data.sync

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PendingOperationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(operation: PendingOperationEntity)

    /** Все ожидающие операции, отсортированные по времени создания (FIFO). */
    @Query("SELECT * FROM pending_operations ORDER BY createdAt ASC")
    suspend fun getAll(): List<PendingOperationEntity>

    @Query("DELETE FROM pending_operations WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE pending_operations SET retryCount = retryCount + 1 WHERE id = :id")
    suspend fun incrementRetry(id: String)

    /** Удалить все операции для конкретного entity (например, после успешного удаления элемента). */
    @Query("DELETE FROM pending_operations WHERE entityId = :entityId")
    suspend fun deleteByEntityId(entityId: String)

    @Query("DELETE FROM pending_operations")
    suspend fun deleteAll()
}
