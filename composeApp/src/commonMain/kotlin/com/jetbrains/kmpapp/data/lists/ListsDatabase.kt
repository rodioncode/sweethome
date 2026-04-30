package com.jetbrains.kmpapp.data.lists

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

@Database(
    entities = [TodoListEntity::class, TodoItemEntity::class],
    version = 3,
    exportSchema = true
)
@ConstructedBy(ListsDatabaseConstructor::class)
abstract class ListsDatabase : RoomDatabase() {
    abstract fun todoListDao(): TodoListDao
    abstract fun todoItemDao(): TodoItemDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object ListsDatabaseConstructor : RoomDatabaseConstructor<ListsDatabase> {
    override fun initialize(): ListsDatabase
}

expect fun getListsDatabaseBuilder(platformContext: Any?): androidx.room.RoomDatabase.Builder<ListsDatabase>
