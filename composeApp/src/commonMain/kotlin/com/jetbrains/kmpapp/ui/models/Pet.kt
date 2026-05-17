package com.jetbrains.kmpapp.ui.models

data class Pet(
    val id: String,
    val name: String,
    val species: Species,
    val stage: Stage,
    val level: Int,
    val mood: Mood,
    val accessories: List<String> = emptyList(),
) {
    val emoji: String get() = species.emoji
}

enum class Species(val emoji: String, val displayName: String, val habitat: String) {
    RACCOON("🦝", "Енотик",   "Чердак с банками малинового варенья"),
    FOX    ("🦊", "Лисёнок",  "Опушка с осенними листьями"),
    CAT    ("🐱", "Котёнок",  "Подоконник с пледом и солнцем"),
    HEDGIE ("🦔", "Ёжик",     "Норка под яблоней с грибочками"),
    PANDA  ("🐼", "Панда",    "Бамбуковая поляна с фонариками"),
    BUNNY  ("🐰", "Зайчонок", "Грядка с морковкой и тёплым закатом"),
}

enum class Stage(val level: Int, val emoji: String, val displayName: String) {
    BABY      (1,  "🥚", "Малыш"),
    CHILD     (3,  "🐾", "Детёныш"),
    TEEN      (5,  "🌿", "Подросток"),
    SCHOOLKID (7,  "🎒", "Школьник"),
    YOUNG     (10, "⭐", "Юный"),
    ADULT     (12, "✨", "Взрослый");
}

enum class Mood(val emoji: String, val displayName: String) {
    HAPPY  ("😊", "Доволен"),
    SLEEPY ("😴", "Сонный"),
    EXCITED("🤩", "В восторге"),
    SAD    ("🥺", "Грустит"),
}
