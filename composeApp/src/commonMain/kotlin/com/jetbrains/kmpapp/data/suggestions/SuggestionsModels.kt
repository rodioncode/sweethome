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

@Serializable
data class Template(
    val id: String,
    val category: String,
    val isSystem: Boolean = true,
    val title: String,
    val description: String? = null,
)

@Serializable
data class TemplateItem(
    val id: String,
    val sortOrder: Int = 0,
    val title: String,
)

@Serializable
data class TemplateWithItems(
    val id: String,
    val category: String,
    val isSystem: Boolean = true,
    val title: String,
    val description: String? = null,
    val items: List<TemplateItem> = emptyList(),
)

@Serializable
data class TemplatesListWrapper(
    val templates: List<Template>,
)

@Serializable
data class UseTemplateRequest(
    val workspaceId: String,
    val title: String,
    val locale: String = "ru",
)
