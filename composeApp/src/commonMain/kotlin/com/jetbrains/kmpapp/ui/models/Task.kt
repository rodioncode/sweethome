package com.jetbrains.kmpapp.ui.models

data class Task(
    val id: String,
    val title: String,
    val listType: ListType,
    val listId: String,
    val isDone: Boolean = false,
    val isOverdue: Boolean = false,
    val priority: Priority = Priority.MEDIUM,
    val assignee: FamilyMember? = null,
    val emoji: String? = null,
    val note: String? = null,
    val reward: Int = 0,
    val subtasks: List<Subtask> = emptyList(),
    val typedFields: Map<String, String> = emptyMap(),
)

data class Subtask(val id: String, val title: String, val isDone: Boolean = false)

enum class Priority(val displayName: String) {
    LOW("Можно отложить"),
    MEDIUM("Средний"),
    HIGH("Важно"),
    URGENT("Срочно");
}
