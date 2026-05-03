package com.jetbrains.kmpapp.data.suggestions

import com.jetbrains.kmpapp.data.lists.TodoItem

interface SuggestionsApi {
    @Deprecated(
        "Используйте TemplatesApi.getPublicTaskTemplates(scope=\"home_chores\"). " +
            "Backend помечает /v1/suggestions/chore-templates как deprecated.",
        ReplaceWith("templatesApi.getPublicTaskTemplates(scope = \"home_chores\", locale = locale)"),
    )
    suspend fun getChoreTemplates(locale: String = "ru"): Result<List<ChoreTemplate>>

    suspend fun getFrequentItems(listId: String): Result<List<TodoItem>>

    suspend fun getFavorites(): Result<List<TodoItem>>

    @Deprecated(
        "Templates v2 разделены на list-templates и task-templates. " +
            "См. TemplatesApi.getPublicListTemplates / getMyListTemplates.",
    )
    suspend fun getAllTemplates(locale: String = "ru"): Result<List<Template>>

    @Deprecated(
        "Используйте TemplatesApi.getListTemplate(id) — возвращает полный TemplateDetail.",
        ReplaceWith("templatesApi.getListTemplate(id, locale)"),
    )
    suspend fun getTemplate(id: String): Result<TemplateWithItems>

    @Deprecated(
        "Используйте TemplatesApi.useListTemplate(id, request) — поддерживает overrides.",
        ReplaceWith("templatesApi.useListTemplate(id, request)"),
    )
    suspend fun useTemplate(id: String, request: UseTemplateRequest): Result<com.jetbrains.kmpapp.data.lists.TodoList>
}
