package com.jetbrains.kmpapp.data.suggestions

import com.jetbrains.kmpapp.data.lists.TodoItem

interface SuggestionsApi {
    suspend fun getChoreTemplates(locale: String = "ru"): Result<List<ChoreTemplate>>
    suspend fun getFrequentItems(listId: String): Result<List<TodoItem>>
    suspend fun getFavorites(): Result<List<TodoItem>>
}
