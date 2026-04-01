package com.jetbrains.kmpapp.data.lists

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoItemDao {
    @Query("SELECT * FROM todo_items WHERE id = :itemId LIMIT 1")
    suspend fun getById(itemId: String): TodoItemEntity?

    @Query("SELECT * FROM todo_items WHERE listId = :listId ORDER BY sortOrder ASC, createdAt ASC")
    fun getItemsByListId(listId: String): Flow<List<TodoItemEntity>>

    @Query("SELECT * FROM todo_items WHERE listId = :listId ORDER BY sortOrder ASC, createdAt ASC")
    suspend fun getItemsByListIdSync(listId: String): List<TodoItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<TodoItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TodoItemEntity)

    @Query("DELETE FROM todo_items WHERE listId = :listId")
    suspend fun deleteByListId(listId: String)

    @Query("DELETE FROM todo_items WHERE id = :itemId")
    suspend fun deleteById(itemId: String)

    @Query("DELETE FROM todo_items")
    suspend fun deleteAll()
}
