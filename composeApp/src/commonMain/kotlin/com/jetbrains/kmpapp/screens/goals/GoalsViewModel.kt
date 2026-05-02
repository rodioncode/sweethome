package com.jetbrains.kmpapp.screens.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetbrains.kmpapp.data.goals.Goal
import com.jetbrains.kmpapp.data.goals.GoalStep
import com.jetbrains.kmpapp.data.goals.GoalsRepository
import com.jetbrains.kmpapp.data.groups.GroupsRepository
import com.jetbrains.kmpapp.data.groups.WorkspaceType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GoalsViewModel(
    private val repo: GoalsRepository,
    private val groupsRepository: GroupsRepository,
) : ViewModel() {

    val familyWorkspaceId: StateFlow<String?> = groupsRepository.groups
        .map { gs -> gs.firstOrNull { it.type == WorkspaceType.FAMILY }?.id }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val goals: StateFlow<List<Goal>> = repo.goals

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        viewModelScope.launch { groupsRepository.loadGroups() }
        viewModelScope.launch {
            familyWorkspaceId.collect { id -> if (id != null) refresh() }
        }
    }

    fun refresh() {
        val ws = familyWorkspaceId.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            repo.loadGoals(ws)
            _isLoading.value = false
        }
    }

    fun create(title: String, description: String?, deadline: String?) {
        val ws = familyWorkspaceId.value ?: return
        viewModelScope.launch { repo.createGoal(ws, title, description, deadline) }
    }

    fun archive(goalId: String) { viewModelScope.launch { repo.archiveGoal(goalId) } }
}

class GoalDetailViewModel(
    private val repo: GoalsRepository,
) : ViewModel() {

    private val _goalId = MutableStateFlow<String?>(null)

    val goal: StateFlow<Goal?> = repo.currentGoal

    fun load(goalId: String) {
        _goalId.value = goalId
        viewModelScope.launch { repo.loadGoal(goalId) }
    }

    fun setDone(done: Boolean) {
        val gid = _goalId.value ?: return
        viewModelScope.launch { repo.patchGoal(gid, isDone = done) }
    }

    fun edit(title: String?, description: String?, deadline: String?) {
        val gid = _goalId.value ?: return
        viewModelScope.launch { repo.patchGoal(gid, title = title, description = description, deadline = deadline) }
    }

    fun addStep(title: String) {
        val gid = _goalId.value ?: return
        val sortOrder = (goal.value?.steps?.maxOfOrNull { it.sortOrder } ?: -1) + 1
        viewModelScope.launch { repo.createStep(gid, title, sortOrder) }
    }

    fun toggleStep(step: GoalStep) {
        val gid = _goalId.value ?: return
        viewModelScope.launch {
            if (step.isDone) repo.patchStep(gid, step.id, isDone = false)
            else repo.completeStep(gid, step.id)
        }
    }

    fun deleteStep(step: GoalStep) {
        val gid = _goalId.value ?: return
        viewModelScope.launch { repo.deleteStep(gid, step.id) }
    }

    fun archive(onDone: () -> Unit) {
        val gid = _goalId.value ?: return
        viewModelScope.launch {
            repo.archiveGoal(gid).onSuccess { onDone() }
        }
    }
}
