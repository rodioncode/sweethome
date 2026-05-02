package com.jetbrains.kmpapp.data.goals

interface GoalsApi {
    suspend fun listGoals(workspaceId: String): Result<List<Goal>>
    suspend fun createGoal(workspaceId: String, request: CreateGoalRequest): Result<Goal>
    suspend fun getGoal(goalId: String): Result<Goal>
    suspend fun patchGoal(goalId: String, request: PatchGoalRequest): Result<Goal>
    suspend fun archiveGoal(goalId: String): Result<Unit>

    suspend fun createStep(goalId: String, request: CreateStepRequest): Result<GoalStep>
    suspend fun patchStep(goalId: String, stepId: String, request: PatchStepRequest): Result<GoalStep>
    suspend fun deleteStep(goalId: String, stepId: String): Result<Unit>
    suspend fun completeStep(goalId: String, stepId: String): Result<GoalStep>
}
