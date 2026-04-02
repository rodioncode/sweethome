package com.jetbrains.kmpapp.data.lists

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jetbrains.kmpapp.data.sync.PendingOperationDao
import com.jetbrains.kmpapp.data.sync.PendingOperationEntity

@Database(
    entities = [
        TodoListEntity::class,
        TodoItemEntity::class,
        PendingOperationEntity::class,
    ],
    version = 2,          // Bump: новые поля TodoItemEntity + таблица pending_operations
    exportSchema = true,
)
abstract class ListsDatabase : RoomDatabase() {
    abstract fun todoListDao(): TodoListDao
    abstract fun todoItemDao(): TodoItemDao
    abstract fun pendingOperationDao(): PendingOperationDao
}

expect fun getListsDatabaseBuilder(platformContext: Any?): androidx.room.RoomDatabase.Builder<ListsDatabase>
