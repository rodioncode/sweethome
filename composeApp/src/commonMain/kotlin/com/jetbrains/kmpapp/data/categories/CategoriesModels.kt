package com.jetbrains.kmpapp.data.categories

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: String,
    val scope: String,
    val name: String,
    val isSystem: Boolean,
    val userId: String? = null,
)

@Serializable
data class CategoriesWrapper(
    val categories: List<Category>,
)

@Serializable
data class CreateCategoryRequest(
    val scope: String,
    val name: String,
)
