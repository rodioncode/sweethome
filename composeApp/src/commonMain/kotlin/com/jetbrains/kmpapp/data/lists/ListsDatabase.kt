package com.jetbrains.kmpapp.data.lists

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [TodoListEntity::class, TodoItemEntity::class],
    version = 2,
    exportSchema = true
)
abstract class ListsDatabase : RoomDatabase() {
    abstract fun todoListDao(): TodoListDao
    abstract fun todoItemDao(): TodoItemDao
}

expect fun getListsDatabaseBuilder(platformContext: Any?): androidx.room.RoomDatabase.Builder<ListsDatabase>
