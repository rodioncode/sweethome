package com.jetbrains.kmpapp.data.templates

import com.jetbrains.kmpapp.data.lists.TodoItem
import com.jetbrains.kmpapp.data.lists.TodoList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Кеш Templates v2 в памяти. Шаблоны не критичны для оффлайна — Room намеренно не используем.
 *
 * Для каждого scope ключевые источники держим раздельно:
 *  - publicListByScope[scope]   — публичный каталог list-templates
 *  - publicTaskByScope[scope]   — публичный каталог task-templates
 *  - myList / myTask            — все свои (без сегментации по scope, т.к. их обычно мало)
 *  - favoriteList / favoriteTask
 *
 * Оптимистичные обновления для favorite/visibility — repo патчит локальные коллекции до ответа сервера,
 * откатывает на ошибку.
 */
class TemplatesRepository(
    private val api: TemplatesApi,
) {

    // ─── State ────────────────────────────────────────────────────────────

    private val _publicListByScope = MutableStateFlow<Map<String, List<ListTemplate>>>(emptyMap())
    val publicListByScope: StateFlow<Map<String, List<ListTemplate>>> = _publicListByScope.asStateFlow()

    private val _publicTaskByScope = MutableStateFlow<Map<String, List<TaskTemplate>>>(emptyMap())
    val publicTaskByScope: StateFlow<Map<String, List<TaskTemplate>>> = _publicTaskByScope.asStateFlow()

    private val _myList = MutableStateFlow<List<ListTemplate>>(emptyList())
    val myList: StateFlow<List<ListTemplate>> = _myList.asStateFlow()

    private val _myTask = MutableStateFlow<List<TaskTemplate>>(emptyList())
    val myTask: StateFlow<List<TaskTemplate>> = _myTask.asStateFlow()

    private val _favoriteList = MutableStateFlow<List<ListTemplate>>(emptyList())
    val favoriteList: StateFlow<List<ListTemplate>> = _favoriteList.asStateFlow()

    private val _favoriteTask = MutableStateFlow<List<TaskTemplate>>(emptyList())
    val favoriteTask: StateFlow<List<TaskTemplate>> = _favoriteTask.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // ─── Loaders ──────────────────────────────────────────────────────────

    suspend fun loadPublicListTemplates(scope: String?, locale: String = "ru"): Result<List<ListTemplate>> {
        val key = scope ?: ALL_SCOPES_KEY
        return api.getPublicListTemplates(scope, locale).also { result ->
            result.onSuccess { _publicListByScope.value = _publicListByScope.value + (key to it) }
                .onFailure { _error.value = it.message }
        }
    }

    suspend fun loadPublicTaskTemplates(scope: String?, locale: String = "ru"): Result<List<TaskTemplate>> {
        val key = scope ?: ALL_SCOPES_KEY
        return api.getPublicTaskTemplates(scope, locale).also { result ->
            result.onSuccess { _publicTaskByScope.value = _publicTaskByScope.value + (key to it) }
                .onFailure { _error.value = it.message }
        }
    }

    suspend fun loadMyListTemplates(locale: String = "ru"): Result<List<ListTemplate>> {
        return api.getMyListTemplates(scope = null, locale = locale).also { result ->
            result.onSuccess { _myList.value = it }
                .onFailure { _error.value = it.message }
        }
    }

    suspend fun loadMyTaskTemplates(locale: String = "ru"): Result<List<TaskTemplate>> {
        return api.getMyTaskTemplates(scope = null, locale = locale).also { result ->
            result.onSuccess { _myTask.value = it }
                .onFailure { _error.value = it.message }
        }
    }

    suspend fun loadFavoriteListTemplates(locale: String = "ru"): Result<List<ListTemplate>> {
        return api.getFavoriteListTemplates(locale).also { result ->
            result.onSuccess { _favoriteList.value = it }
                .onFailure { _error.value = it.message }
        }
    }

    suspend fun loadFavoriteTaskTemplates(locale: String = "ru"): Result<List<TaskTemplate>> {
        return api.getFavoriteTaskTemplates(locale).also { result ->
            result.onSuccess { _favoriteTask.value = it }
                .onFailure { _error.value = it.message }
        }
    }

    suspend fun getListTemplateDetail(id: String, locale: String = "ru"): Result<ListTemplateDetail> =
        api.getListTemplate(id, locale).also { it.onFailure { e -> _error.value = e.message } }

    suspend fun getTaskTemplateDetail(id: String, locale: String = "ru"): Result<TaskTemplateDetail> =
        api.getTaskTemplate(id, locale).also { it.onFailure { e -> _error.value = e.message } }

    // ─── Use ──────────────────────────────────────────────────────────────

    suspend fun useListTemplate(
        templateId: String,
        workspaceId: String,
        title: String,
        overrides: ListTemplateOverrides? = null,
        locale: String = "ru",
    ): Result<TodoList> = api.useListTemplate(
        templateId,
        UseListTemplateRequest(workspaceId = workspaceId, title = title, locale = locale, overrides = overrides),
    ).also { it.onFailure { e -> _error.value = e.message } }

    suspend fun useTaskTemplate(
        templateId: String,
        listId: String,
        overrides: TaskTemplateOverrides? = null,
        locale: String = "ru",
    ): Result<TodoItem> = api.useTaskTemplate(
        templateId,
        UseTaskTemplateRequest(listId = listId, locale = locale, overrides = overrides),
    ).also { it.onFailure { e -> _error.value = e.message } }

    // ─── Save list as template ────────────────────────────────────────────

    suspend fun saveListAsTemplate(
        listId: String,
        category: String,
        title: String,
        description: String? = null,
        includeItemIds: List<String>? = null,
        locale: String = "ru",
    ): Result<ListTemplate> = api.saveListAsTemplate(
        listId,
        SaveAsTemplateRequest(
            category = category,
            locale = locale,
            title = title,
            description = description,
            includeItemIds = includeItemIds,
        ),
    ).also { result ->
        result.onSuccess { _myList.value = listOf(it) + _myList.value }
            .onFailure { _error.value = it.message }
    }

    // ─── Favorites (оптимистично) ─────────────────────────────────────────

    suspend fun setListFavorite(template: ListTemplate, favorite: Boolean): Result<Unit> {
        patchListTemplateFavorite(template.id, favorite)
        val result = if (favorite) api.favoriteListTemplate(template.id) else api.unfavoriteListTemplate(template.id)
        result.onSuccess {
            if (favorite) {
                if (_favoriteList.value.none { it.id == template.id }) {
                    _favoriteList.value = listOf(template.copy(isFavorite = true)) + _favoriteList.value
                }
            } else {
                _favoriteList.value = _favoriteList.value.filter { it.id != template.id }
            }
        }.onFailure {
            patchListTemplateFavorite(template.id, !favorite)  // откат
            _error.value = it.message
        }
        return result
    }

    suspend fun setTaskFavorite(template: TaskTemplate, favorite: Boolean): Result<Unit> {
        patchTaskTemplateFavorite(template.id, favorite)
        val result = if (favorite) api.favoriteTaskTemplate(template.id) else api.unfavoriteTaskTemplate(template.id)
        result.onSuccess {
            if (favorite) {
                if (_favoriteTask.value.none { it.id == template.id }) {
                    _favoriteTask.value = listOf(template.copy(isFavorite = true)) + _favoriteTask.value
                }
            } else {
                _favoriteTask.value = _favoriteTask.value.filter { it.id != template.id }
            }
        }.onFailure {
            patchTaskTemplateFavorite(template.id, !favorite)
            _error.value = it.message
        }
        return result
    }

    private fun patchListTemplateFavorite(id: String, favorite: Boolean) {
        _publicListByScope.value = _publicListByScope.value.mapValues { (_, list) ->
            list.map { if (it.id == id) it.copy(isFavorite = favorite) else it }
        }
        _myList.value = _myList.value.map { if (it.id == id) it.copy(isFavorite = favorite) else it }
    }

    private fun patchTaskTemplateFavorite(id: String, favorite: Boolean) {
        _publicTaskByScope.value = _publicTaskByScope.value.mapValues { (_, list) ->
            list.map { if (it.id == id) it.copy(isFavorite = favorite) else it }
        }
        _myTask.value = _myTask.value.map { if (it.id == id) it.copy(isFavorite = favorite) else it }
    }

    // ─── Publication ──────────────────────────────────────────────────────

    suspend fun requestListPublication(template: ListTemplate): Result<Unit> {
        return api.requestListPublication(template.id).also { result ->
            result.onSuccess { patchMyListVisibility(template.id, TemplateVisibility.PENDING) }
                .onFailure { _error.value = it.message }
        }
    }

    suspend fun withdrawListPublication(template: ListTemplate): Result<Unit> {
        return api.withdrawListPublication(template.id).also { result ->
            result.onSuccess { patchMyListVisibility(template.id, TemplateVisibility.PRIVATE) }
                .onFailure { _error.value = it.message }
        }
    }

    suspend fun requestTaskPublication(template: TaskTemplate): Result<Unit> {
        return api.requestTaskPublication(template.id).also { result ->
            result.onSuccess { patchMyTaskVisibility(template.id, TemplateVisibility.PENDING) }
                .onFailure { _error.value = it.message }
        }
    }

    suspend fun withdrawTaskPublication(template: TaskTemplate): Result<Unit> {
        return api.withdrawTaskPublication(template.id).also { result ->
            result.onSuccess { patchMyTaskVisibility(template.id, TemplateVisibility.PRIVATE) }
                .onFailure { _error.value = it.message }
        }
    }

    private fun patchMyListVisibility(id: String, visibility: String) {
        _myList.value = _myList.value.map { if (it.id == id) it.copy(visibility = visibility) else it }
    }

    private fun patchMyTaskVisibility(id: String, visibility: String) {
        _myTask.value = _myTask.value.map { if (it.id == id) it.copy(visibility = visibility) else it }
    }

    // ─── Delete ───────────────────────────────────────────────────────────

    suspend fun deleteListTemplate(id: String): Result<Unit> {
        return api.deleteListTemplate(id).also { result ->
            result.onSuccess {
                _myList.value = _myList.value.filter { it.id != id }
                _favoriteList.value = _favoriteList.value.filter { it.id != id }
                _publicListByScope.value = _publicListByScope.value.mapValues { (_, list) ->
                    list.filter { it.id != id }
                }
            }.onFailure { _error.value = it.message }
        }
    }

    suspend fun deleteTaskTemplate(id: String): Result<Unit> {
        return api.deleteTaskTemplate(id).also { result ->
            result.onSuccess {
                _myTask.value = _myTask.value.filter { it.id != id }
                _favoriteTask.value = _favoriteTask.value.filter { it.id != id }
                _publicTaskByScope.value = _publicTaskByScope.value.mapValues { (_, list) ->
                    list.filter { it.id != id }
                }
            }.onFailure { _error.value = it.message }
        }
    }

    // ─── House-keeping ────────────────────────────────────────────────────

    fun clearError() {
        _error.value = null
    }

    fun clearAll() {
        _publicListByScope.value = emptyMap()
        _publicTaskByScope.value = emptyMap()
        _myList.value = emptyList()
        _myTask.value = emptyList()
        _favoriteList.value = emptyList()
        _favoriteTask.value = emptyList()
        _error.value = null
    }

    companion object {
        const val ALL_SCOPES_KEY = "__all__"
    }
}
