package com.jetbrains.kmpapp.screens.kid

data class KidHomeState(
    val kidName: String = "Дима",
    val dateLabel: String = "СРЕДА · 5 МАЯ",
    val stars: Int = 57,
    val petGreeting: String = "Сегодня у нас 4 точки на пути! Уже прошли две — ты молодец 🌟",
    val petEmoji: String = "🦔",
    val stops: List<KidStop> = emptyList(),
    val prizeName: String = "Прогулка с Бимом",
    val prizeRemaining: Int = 8,
)

data class KidStop(
    val id: String,
    val emoji: String,
    val title: String,
    val reward: Int,
    val done: Boolean = false,
    val current: Boolean = false,
)

enum class KidTab(val id: String, val emoji: String, val label: String) {
    HOME ("home", "☀️", "Сегодня"),
    PET  ("pet",  "🦔", "Колючка"),
    SHOP ("shop", "🎁", "Призы"),
}

sealed interface KidHomeIntent {
    data class OpenStop(val stopId: String) : KidHomeIntent
    data class NavTab(val tab: KidTab) : KidHomeIntent
    data object OpenShop : KidHomeIntent
    data object OpenPet  : KidHomeIntent
    data object OpenLetters : KidHomeIntent
    data object OpenStreak : KidHomeIntent
}
