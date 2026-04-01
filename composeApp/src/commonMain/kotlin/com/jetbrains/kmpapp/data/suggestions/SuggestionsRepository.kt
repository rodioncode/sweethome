package com.jetbrains.kmpapp.data.suggestions

import com.jetbrains.kmpapp.data.lists.TodoItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SuggestionsRepository(private val api: SuggestionsApi) {

    private val _choreTemplates = MutableStateFlow<List<ChoreTemplate>>(emptyList())
    val choreTemplates: StateFlow<List<ChoreTemplate>> = _choreTemplates.asStateFlow()

    private val _frequentItems = MutableStateFlow<List<TodoItem>>(emptyList())
    val frequentItems: StateFlow<List<TodoItem>> = _frequentItems.asStateFlow()

    suspend fun loadChoreTemplates() {
        api.getChoreTemplates().onSuccess { _choreTemplates.value = it }
    }

    suspend fun loadFrequentItems(listId: String) {
        api.getFrequentItems(listId).onSuccess { _frequentItems.value = it }
    }

    fun clear() {
        _choreTemplates.value = emptyList()
        _frequentItems.value = emptyList()
    }
}
