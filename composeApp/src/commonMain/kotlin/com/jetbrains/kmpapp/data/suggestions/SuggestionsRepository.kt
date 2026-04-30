package com.jetbrains.kmpapp.data.suggestions

import com.jetbrains.kmpapp.data.lists.TodoItem
import com.jetbrains.kmpapp.data.lists.TodoList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SuggestionsRepository(private val api: SuggestionsApi) {

    private val _choreTemplates = MutableStateFlow<List<ChoreTemplate>>(emptyList())
    val choreTemplates: StateFlow<List<ChoreTemplate>> = _choreTemplates.asStateFlow()

    private val _allTemplates = MutableStateFlow<List<Template>>(emptyList())
    val allTemplates: StateFlow<List<Template>> = _allTemplates.asStateFlow()

    private val _frequentItems = MutableStateFlow<List<TodoItem>>(emptyList())
    val frequentItems: StateFlow<List<TodoItem>> = _frequentItems.asStateFlow()

    suspend fun loadChoreTemplates() {
        api.getChoreTemplates().onSuccess { _choreTemplates.value = it }
    }

    suspend fun loadAllTemplates(locale: String = "ru") {
        api.getAllTemplates(locale).onSuccess { _allTemplates.value = it }
    }

    suspend fun loadFrequentItems(listId: String) {
        api.getFrequentItems(listId).onSuccess { _frequentItems.value = it }
    }

    suspend fun getTemplate(id: String): Result<TemplateWithItems> = api.getTemplate(id)

    suspend fun useTemplate(id: String, workspaceId: String, title: String, locale: String = "ru"): Result<TodoList> =
        api.useTemplate(id, UseTemplateRequest(workspaceId = workspaceId, title = title, locale = locale))

    fun clear() {
        _choreTemplates.value = emptyList()
        _allTemplates.value = emptyList()
        _frequentItems.value = emptyList()
    }
}
