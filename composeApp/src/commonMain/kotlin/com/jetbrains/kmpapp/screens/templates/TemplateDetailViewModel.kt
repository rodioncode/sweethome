package com.jetbrains.kmpapp.screens.templates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetbrains.kmpapp.auth.AuthRepository
import com.jetbrains.kmpapp.auth.AuthState
import com.jetbrains.kmpapp.data.groups.Group
import com.jetbrains.kmpapp.data.groups.GroupsRepository
import com.jetbrains.kmpapp.data.lists.TodoList
import com.jetbrains.kmpapp.data.templates.ListTemplate
import com.jetbrains.kmpapp.data.templates.ListTemplateDetail
import com.jetbrains.kmpapp.data.templates.TemplatesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TemplateDetailViewModel(
    private val templatesRepository: TemplatesRepository,
    private val groupsRepository: GroupsRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _detail = MutableStateFlow<ListTemplateDetail?>(null)
    val detail: StateFlow<ListTemplateDetail?> = _detail.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val workspaces: StateFlow<List<Group>> = groupsRepository.groups
        .map { gs -> gs.filter { it.archivedAt == null } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val currentUserId: StateFlow<String?> = authRepository.authState
        .map { (it as? AuthState.Authenticated)?.userId }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /** Является ли текущий user владельцем шаблона. */
    val isMine: StateFlow<Boolean> = combine(_detail, currentUserId) { d, uid ->
        val ownerId = d?.userId
        ownerId != null && uid != null && ownerId == uid && d?.isSystem != true
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val error: StateFlow<String?> = templatesRepository.error

    fun load(templateId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            templatesRepository.getListTemplateDetail(templateId)
                .onSuccess { _detail.value = it }
            // Workspaces могли ещё не подгрузиться (например, при cold launch).
            if (groupsRepository.groups.value.isEmpty()) {
                groupsRepository.loadGroups()
            }
            _isLoading.value = false
        }
    }

    fun toggleFavorite() {
        val d = _detail.value ?: return
        viewModelScope.launch {
            val asTemplate = ListTemplate(
                id = d.id, scope = d.scope, category = d.category, isSystem = d.isSystem,
                userId = d.userId, visibility = d.visibility, title = d.title,
                description = d.description, isFavorite = d.isFavorite,
            )
            templatesRepository.setListFavorite(asTemplate, !d.isFavorite)
                .onSuccess { _detail.value = d.copy(isFavorite = !d.isFavorite) }
        }
    }

    suspend fun use(workspaceId: String, title: String): Result<TodoList> {
        val d = _detail.value ?: return Result.failure(IllegalStateException("Шаблон не загружен"))
        return templatesRepository.useListTemplate(
            templateId = d.id,
            workspaceId = workspaceId,
            title = title,
        )
    }

    suspend fun requestPublication(): Result<Unit> {
        val d = _detail.value ?: return Result.failure(IllegalStateException("Шаблон не загружен"))
        val asTemplate = d.toSummary()
        return templatesRepository.requestListPublication(asTemplate)
            .onSuccess { _detail.value = d.copy(visibility = "pending") }
    }

    suspend fun withdrawPublication(): Result<Unit> {
        val d = _detail.value ?: return Result.failure(IllegalStateException("Шаблон не загружен"))
        val asTemplate = d.toSummary()
        return templatesRepository.withdrawListPublication(asTemplate)
            .onSuccess { _detail.value = d.copy(visibility = "private") }
    }

    suspend fun delete(): Result<Unit> {
        val d = _detail.value ?: return Result.failure(IllegalStateException("Шаблон не загружен"))
        return templatesRepository.deleteListTemplate(d.id)
    }

    fun clearError() {
        templatesRepository.clearError()
    }
}

private fun ListTemplateDetail.toSummary(): ListTemplate = ListTemplate(
    id = id, scope = scope, category = category, isSystem = isSystem,
    userId = userId, visibility = visibility, title = title,
    description = description, isFavorite = isFavorite,
)
