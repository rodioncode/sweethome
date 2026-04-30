package com.jetbrains.kmpapp.screens.templates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetbrains.kmpapp.data.suggestions.ChoreTemplate
import com.jetbrains.kmpapp.data.suggestions.SuggestionsRepository
import com.jetbrains.kmpapp.data.suggestions.Template
import com.jetbrains.kmpapp.data.suggestions.TemplateWithItems
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TemplatesViewModel(
    private val suggestionsRepository: SuggestionsRepository,
) : ViewModel() {

    val templates: StateFlow<List<ChoreTemplate>> = suggestionsRepository.choreTemplates
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allTemplates: StateFlow<List<Template>> = suggestionsRepository.allTemplates
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadTemplates()
    }

    private fun loadTemplates() {
        viewModelScope.launch {
            _isLoading.value = true
            suggestionsRepository.loadChoreTemplates()
            suggestionsRepository.loadAllTemplates()
            _isLoading.value = false
        }
    }

    suspend fun getTemplate(id: String): Result<TemplateWithItems> =
        suggestionsRepository.getTemplate(id)

    fun useTemplate(id: String, workspaceId: String, title: String) {
        viewModelScope.launch {
            suggestionsRepository.useTemplate(id, workspaceId, title)
        }
    }
}
