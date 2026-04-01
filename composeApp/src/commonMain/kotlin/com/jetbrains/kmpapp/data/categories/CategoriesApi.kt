package com.jetbrains.kmpapp.data.categories

interface CategoriesApi {
    suspend fun getCategories(scope: String): Result<List<Category>>
    suspend fun createCategory(request: CreateCategoryRequest): Result<Category>
}
