package com.jetbrains.kmpapp.data.suggestions

import com.jetbrains.kmpapp.data.lists.TodoItem
import com.jetbrains.kmpapp.data.lists.TodoList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Suppress("DEPRECATION")
class SuggestionsRepository(private val api: SuggestionsApi) {

    private val _choreTemplates = MutableStateFlow<List<ChoreTemplate>>(emptyList())
    val choreTemplates: StateFlow<List<ChoreTemplate>> = _choreTemplates.asStateFlow()

    private val _allTemplates = MutableStateFlow<List<Template>>(emptyList())

    @Deprecated("Templates v2: используйте TemplatesRepository.publicListByScope / myList / favoriteList.")
    val allTemplates: StateFlow<List<Template>> = _allTemplates.asStateFlow()

    private val _frequentItems = MutableStateFlow<List<TodoItem>>(emptyList())
    val frequentItems: StateFlow<List<TodoItem>> = _frequentItems.asStateFlow()

    private val _favoriteItems = MutableStateFlow<List<TodoItem>>(emptyList())
    val favoriteItems: StateFlow<List<TodoItem>> = _favoriteItems.asStateFlow()

    @Deprecated(
        "Templates v2: используйте TemplatesRepository.loadPublicTaskTemplates(scope=\"home_chores\").",
    )
    suspend fun loadChoreTemplates() {
        api.getChoreTemplates().onSuccess { _choreTemplates.value = it }
    }

    @Deprecated(
        "Templates v2: используйте TemplatesRepository.loadPublicListTemplates / loadMyListTemplates.",
    )
    suspend fun loadAllTemplates(locale: String = "ru") {
        api.getAllTemplates(locale).onSuccess { _allTemplates.value = it }
    }

    suspend fun loadFrequentItems(listId: String) {
        api.getFrequentItems(listId).onSuccess { _frequentItems.value = it }
    }

    suspend fun loadFavoriteItems() {
        api.getFavorites().onSuccess { _favoriteItems.value = it }
    }

    @Deprecated(
        "Templates v2: используйте TemplatesRepository.getListTemplateDetail(id).",
        ReplaceWith("templatesRepository.getListTemplateDetail(id)"),
    )
    suspend fun getTemplate(id: String): Result<TemplateWithItems> = api.getTemplate(id)

    @Deprecated(
        "Templates v2: используйте TemplatesRepository.useListTemplate — с overrides.",
    )
    suspend fun useTemplate(id: String, workspaceId: String, title: String, locale: String = "ru"): Result<TodoList> =
        api.useTemplate(id, UseTemplateRequest(workspaceId = workspaceId, title = title, locale = locale))

    fun clear() {
        _choreTemplates.value = emptyList()
        _allTemplates.value = emptyList()
        _frequentItems.value = emptyList()
        _favoriteItems.value = emptyList()
    }
}
