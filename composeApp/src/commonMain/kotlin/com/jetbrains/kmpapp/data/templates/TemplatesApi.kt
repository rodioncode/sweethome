package com.jetbrains.kmpapp.data.templates

import com.jetbrains.kmpapp.data.lists.TodoItem
import com.jetbrains.kmpapp.data.lists.TodoList

/**
 * Контракт Templates v2 (см. backend `docs/CLIENT_GUIDE.md` §9).
 * Две сущности — list templates и task templates — с зеркальным набором эндпоинтов
 * + операция `save list as template` + favorites.
 *
 * Admin-модерация (`/v1/admin/templates/...`) намеренно опущена — пользовательский клиент.
 */
interface TemplatesApi {

    // ─── List templates ───────────────────────────────────────────────────

    suspend fun getPublicListTemplates(scope: String? = null, locale: String = "ru"): Result<List<ListTemplate>>
    suspend fun getMyListTemplates(scope: String? = null, locale: String = "ru"): Result<List<ListTemplate>>
    suspend fun getFavoriteListTemplates(locale: String = "ru"): Result<List<ListTemplate>>
    suspend fun getListTemplate(id: String, locale: String = "ru"): Result<ListTemplateDetail>
    suspend fun useListTemplate(id: String, request: UseListTemplateRequest): Result<TodoList>
    suspend fun requestListPublication(id: String): Result<Unit>
    suspend fun withdrawListPublication(id: String): Result<Unit>
    suspend fun favoriteListTemplate(id: String): Result<Unit>
    suspend fun unfavoriteListTemplate(id: String): Result<Unit>
    suspend fun deleteListTemplate(id: String): Result<Unit>

    // ─── Task templates ───────────────────────────────────────────────────

    suspend fun getPublicTaskTemplates(scope: String? = null, locale: String = "ru"): Result<List<TaskTemplate>>
    suspend fun getMyTaskTemplates(scope: String? = null, locale: String = "ru"): Result<List<TaskTemplate>>
    suspend fun getFavoriteTaskTemplates(locale: String = "ru"): Result<List<TaskTemplate>>
    suspend fun getTaskTemplate(id: String, locale: String = "ru"): Result<TaskTemplateDetail>
    suspend fun useTaskTemplate(id: String, request: UseTaskTemplateRequest): Result<TodoItem>
    suspend fun requestTaskPublication(id: String): Result<Unit>
    suspend fun withdrawTaskPublication(id: String): Result<Unit>
    suspend fun favoriteTaskTemplate(id: String): Result<Unit>
    suspend fun unfavoriteTaskTemplate(id: String): Result<Unit>
    suspend fun deleteTaskTemplate(id: String): Result<Unit>

    // ─── Save existing list as template ───────────────────────────────────

    suspend fun saveListAsTemplate(listId: String, request: SaveAsTemplateRequest): Result<ListTemplate>
}
