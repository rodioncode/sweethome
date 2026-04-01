package com.jetbrains.kmpapp.data.suggestions

import kotlinx.serialization.Serializable

@Serializable
data class ChoreTemplate(
    val id: String,
    val category: String,
    val intervalDays: Int,
    val title: String,
    val description: String? = null,
)

@Serializable
data class ChoreTemplatesWrapper(
    val templates: List<ChoreTemplate>,
)
