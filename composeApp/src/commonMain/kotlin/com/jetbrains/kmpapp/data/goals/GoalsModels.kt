package com.jetbrains.kmpapp.data.goals

import kotlinx.serialization.Serializable

@Serializable
data class GoalStep(
    val id: String,
    val goalId: String,
    val title: String,
    val sortOrder: Int = 0,
    val isDone: Boolean = false,
    val completedAt: String? = null,
)

@Serializable
data class Goal(
    val id: String,
    val workspaceId: String,
    val title: String,
    val description: String? = null,
    val deadline: String? = null,    // YYYY-MM-DD
    val isDone: Boolean = false,
    val archivedAt: String? = null,
    val createdAt: String? = null,
    val steps: List<GoalStep> = emptyList(),
)

@Serializable
data class GoalsWrapper(val goals: List<Goal>)

@Serializable
data class CreateGoalRequest(
    val title: String,
    val description: String? = null,
    val deadline: String? = null,
)

@Serializable
data class PatchGoalRequest(
    val title: String? = null,
    val description: String? = null,
    val deadline: String? = null,    // "" = очистить
    val isDone: Boolean? = null,
)

@Serializable
data class CreateStepRequest(
    val title: String,
    val sortOrder: Int = 0,
)

@Serializable
data class PatchStepRequest(
    val title: String? = null,
    val sortOrder: Int? = null,
    val isDone: Boolean? = null,
)
