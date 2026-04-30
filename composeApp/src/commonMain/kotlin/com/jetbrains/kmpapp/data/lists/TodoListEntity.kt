package com.jetbrains.kmpapp.data.lists

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todo_lists")
data class TodoListEntity(
    @PrimaryKey val id: String,
    val workspaceId: String,
    val type: String,
    val title: String,
    val icon: String?,
    val color: String?,
    @ColumnInfo(defaultValue = "NULL")
    val description: String?,
    @ColumnInfo(defaultValue = "NULL")
    val customTypeLabel: String?,
    val createdBy: String,
    val createdAt: String,
    val archivedAt: String?,
    @ColumnInfo(defaultValue = "0")
    val isPublic: Boolean,
    @ColumnInfo(defaultValue = "NULL")
    val publicToken: String?,
)
