package com.jetbrains.kmpapp.data.lists

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "todo_items",
    foreignKeys = [
        ForeignKey(
            entity = TodoListEntity::class,
            parentColumns = ["id"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("listId")]
)
data class TodoItemEntity(
    @PrimaryKey val id: String,
    val listId: String,
    val title: String,
    val note: String?,
    val sortOrder: Double?,
    val isDone: Boolean,
    val doneAt: String?,
    val createdBy: String,
    val createdAt: String,
    val updatedAt: String,
    val shoppingJson: String?,
    val choreScheduleJson: String?,
)
