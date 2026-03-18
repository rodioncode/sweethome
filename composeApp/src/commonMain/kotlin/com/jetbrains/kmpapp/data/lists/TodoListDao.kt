package com.jetbrains.kmpapp.data.lists

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoListDao {
    @Query("SELECT * FROM todo_lists ORDER BY createdAt DESC")
    fun getAllLists(): Flow<List<TodoListEntity>>

    @Query("SELECT * FROM todo_lists WHERE id = :listId")
    suspend fun getListById(listId: String): TodoListEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(lists: List<TodoListEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(list: TodoListEntity)

    @Query("DELETE FROM todo_lists")
    suspend fun deleteAll()

    @Query("DELETE FROM todo_lists WHERE id = :listId")
    suspend fun deleteById(listId: String)
}
