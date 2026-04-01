package com.jetbrains.kmpapp.data.categories

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CategoriesRepository(private val api: CategoriesApi) {

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    suspend fun loadCategories(scope: String) {
        api.getCategories(scope).onSuccess { _categories.value = it }
    }

    suspend fun createCategory(scope: String, name: String): Result<Category> {
        val result = api.createCategory(CreateCategoryRequest(scope, name))
        result.onSuccess { _categories.value = _categories.value + it }
        return result
    }

    fun clear() {
        _categories.value = emptyList()
    }
}
