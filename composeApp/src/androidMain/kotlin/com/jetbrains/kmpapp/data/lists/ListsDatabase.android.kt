package com.jetbrains.kmpapp.data.lists

import android.content.Context
import androidx.room.Room

actual fun getListsDatabaseBuilder(platformContext: Any?): androidx.room.RoomDatabase.Builder<ListsDatabase> {
    val context = (platformContext as Context).applicationContext
    val dbFile = context.getDatabasePath("lists.db")
    return Room.databaseBuilder<ListsDatabase>(
        context = context,
        name = dbFile.absolutePath
    )
}
