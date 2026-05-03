package com.jetbrains.kmpapp.screens.templates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetbrains.kmpapp.data.templates.ListTemplate
import com.jetbrains.kmpapp.data.templates.TemplatesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TemplatesTab { PUBLIC, MINE, FAVORITES }

class TemplatesViewModel(
    private val templatesRepository: TemplatesRepository,
) : ViewModel() {

    private val _tab = MutableStateFlow(TemplatesTab.PUBLIC)
    val tab: StateFlow<TemplatesTab> = _tab.asStateFlow()

    /** Опциональный фильтр по типу списка (`shopping`, `home_chores`, …). null = «все типы». */
    private val _scope = MutableStateFlow<String?>(null)
    val scope: StateFlow<String?> = _scope.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /** Видимые шаблоны для текущего таба и скоупа. */
    val templates: StateFlow<List<ListTemplate>> = combine(
        templatesRepository.publicListByScope,
        templatesRepository.myList,
        templatesRepository.favoriteList,
        _tab,
        _scope,
    ) { publicByScope, mine, favorites, currentTab, currentScope ->
        val source = when (currentTab) {
            TemplatesTab.PUBLIC -> {
                if (currentScope != null) publicByScope[currentScope].orEmpty()
                else publicByScope.values.flatten().distinctBy { it.id }
            }
            TemplatesTab.MINE -> mine
            TemplatesTab.FAVORITES -> favorites
        }
        if (currentScope != null && currentTab != TemplatesTab.PUBLIC) {
            source.filter { it.scope == currentScope }
        } else source
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        refresh()
    }

    fun setTab(value: TemplatesTab) {
        _tab.value = value
        refresh()
    }

    fun setScope(value: String?) {
        _scope.value = value
        if (_tab.value == TemplatesTab.PUBLIC) refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            when (_tab.value) {
                TemplatesTab.PUBLIC -> templatesRepository.loadPublicListTemplates(scope = _scope.value)
                TemplatesTab.MINE -> templatesRepository.loadMyListTemplates()
                TemplatesTab.FAVORITES -> templatesRepository.loadFavoriteListTemplates()
            }
            _isLoading.value = false
        }
    }

    fun toggleFavorite(template: ListTemplate) {
        viewModelScope.launch {
            templatesRepository.setListFavorite(template, !template.isFavorite)
        }
    }
}
