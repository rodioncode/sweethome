package com.jetbrains.kmpapp.data.lists

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todo_lists")
data class TodoListEntity(
    @PrimaryKey val id: String,
    val type: String,
    val title: String,
    val icon: String?,
    val scope: String,
    val ownerUserId: String?,
    val ownerGroupId: String?,
    val createdBy: String,
    val createdAt: String,
    val archivedAt: String?,
)
