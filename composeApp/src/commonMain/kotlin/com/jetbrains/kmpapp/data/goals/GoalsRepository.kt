package com.jetbrains.kmpapp.data.goals

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GoalsRepository(
    private val api: GoalsApi,
) {
    private val _goals = MutableStateFlow<List<Goal>>(emptyList())
    val goals: StateFlow<List<Goal>> = _goals.asStateFlow()

    private val _currentGoal = MutableStateFlow<Goal?>(null)
    val currentGoal: StateFlow<Goal?> = _currentGoal.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    suspend fun loadGoals(workspaceId: String) {
        api.listGoals(workspaceId)
            .onSuccess { _goals.value = it }
            .onFailure { _error.value = it.message }
    }

    suspend fun loadGoal(goalId: String) {
        api.getGoal(goalId)
            .onSuccess { _currentGoal.value = it }
            .onFailure { _error.value = it.message }
    }

    suspend fun createGoal(workspaceId: String, title: String, description: String?, deadline: String?): Result<Goal> {
        val res = api.createGoal(workspaceId, CreateGoalRequest(title, description?.ifBlank { null }, deadline?.ifBlank { null }))
        res.onSuccess { g -> _goals.value = listOf(g) + _goals.value }
        res.onFailure { _error.value = it.message }
        return res
    }

    suspend fun patchGoal(goalId: String, title: String? = null, description: String? = null, deadline: String? = null, isDone: Boolean? = null): Result<Goal> {
        val res = api.patchGoal(goalId, PatchGoalRequest(title, description, deadline, isDone))
        res.onSuccess { g ->
            _goals.value = _goals.value.map { if (it.id == goalId) g else it }
            if (_currentGoal.value?.id == goalId) _currentGoal.value = g
        }
        res.onFailure { _error.value = it.message }
        return res
    }

    suspend fun archiveGoal(goalId: String): Result<Unit> {
        val res = api.archiveGoal(goalId)
        res.onSuccess { _goals.value = _goals.value.filter { it.id != goalId } }
        res.onFailure { _error.value = it.message }
        return res
    }

    suspend fun createStep(goalId: String, title: String, sortOrder: Int): Result<GoalStep> {
        val res = api.createStep(goalId, CreateStepRequest(title, sortOrder))
        res.onSuccess { step -> mutateCurrentGoalSteps { it + step } }
        res.onFailure { _error.value = it.message }
        return res
    }

    suspend fun patchStep(goalId: String, stepId: String, title: String? = null, sortOrder: Int? = null, isDone: Boolean? = null): Result<GoalStep> {
        val res = api.patchStep(goalId, stepId, PatchStepRequest(title, sortOrder, isDone))
        res.onSuccess { updated -> mutateCurrentGoalSteps { steps -> steps.map { if (it.id == stepId) updated else it } } }
        res.onFailure { _error.value = it.message }
        return res
    }

    suspend fun deleteStep(goalId: String, stepId: String): Result<Unit> {
        val res = api.deleteStep(goalId, stepId)
        res.onSuccess { mutateCurrentGoalSteps { it.filter { s -> s.id != stepId } } }
        res.onFailure { _error.value = it.message }
        return res
    }

    suspend fun completeStep(goalId: String, stepId: String): Result<GoalStep> {
        val res = api.completeStep(goalId, stepId)
        res.onSuccess { updated -> mutateCurrentGoalSteps { steps -> steps.map { if (it.id == stepId) updated else it } } }
        res.onFailure { _error.value = it.message }
        return res
    }

    private fun mutateCurrentGoalSteps(update: (List<GoalStep>) -> List<GoalStep>) {
        val cur = _currentGoal.value ?: return
        _currentGoal.value = cur.copy(steps = update(cur.steps))
    }

    fun clearError() { _error.value = null }
    fun clearAll() {
        _goals.value = emptyList()
        _currentGoal.value = null
        _error.value = null
    }
}
