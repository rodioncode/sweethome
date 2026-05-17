package com.jetbrains.kmpapp.ui.models

enum class Palette { PRIMARY, CORAL, OCHRE, LAVENDER }

enum class ListType(
    val id: String,
    val emoji: String,
    val displayName: String,
    val palette: Palette,
) {
    SHOPPING       ("shopping",       "🛒", "Покупки",       Palette.CORAL),
    HOME_CHORES    ("home_chores",    "🏡", "Домашние дела", Palette.PRIMARY),
    GENERAL_TODOS  ("general_todos",  "✅", "Задачи",        Palette.LAVENDER),
    STUDY          ("study",          "📚", "Учёба",         Palette.OCHRE),
    TRAVEL         ("travel",         "✈️", "Путешествие",   Palette.LAVENDER),
    MEDIA          ("media",          "🎬", "Медиа",         Palette.CORAL),
    WISHLIST       ("wishlist",       "🎁", "Вишлист",       Palette.PRIMARY),
    CUSTOM         ("custom",         "✨", "Свой тип",      Palette.OCHRE);

    companion object {
        fun fromId(id: String) = entries.firstOrNull { it.id == id } ?: CUSTOM
    }
}
