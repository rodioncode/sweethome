package com.jetbrains.kmpapp.data.suggestions

import com.jetbrains.kmpapp.data.lists.TodoItem

interface SuggestionsApi {
    suspend fun getChoreTemplates(locale: String = "ru"): Result<List<ChoreTemplate>>
    suspend fun getFrequentItems(listId: String): Result<List<TodoItem>>
    suspend fun getFavorites(): Result<List<TodoItem>>
    suspend fun getAllTemplates(locale: String = "ru"): Result<List<Template>>
    suspend fun getTemplate(id: String): Result<TemplateWithItems>
    suspend fun useTemplate(id: String, request: UseTemplateRequest): Result<com.jetbrains.kmpapp.data.lists.TodoList>
}
