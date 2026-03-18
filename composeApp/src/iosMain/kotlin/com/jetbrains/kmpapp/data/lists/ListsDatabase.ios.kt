package com.jetbrains.kmpapp.data.lists

import androidx.room.Room
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask
import platform.Foundation.URLForDirectory

actual fun getListsDatabaseBuilder(platformContext: Any?): androidx.room.RoomDatabase.Builder<ListsDatabase> {
    val dbFilePath = documentDirectory() + "/lists.db"
    return Room.databaseBuilder<ListsDatabase>(
        name = dbFilePath
    )
}

private fun documentDirectory(): String {
    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null
    )
    return requireNotNull(documentDirectory?.path)
}
