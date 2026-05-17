package com.jetbrains.kmpapp.screens.menu

import kotlinx.datetime.LocalDate

enum class MealSlot(val emoji: String, val displayName: String) {
    BREAKFAST("🥐", "Завтрак"),
    LUNCH("🍲", "Обед"),
    SNACK("🍎", "Перекус"),
    DINNER("🥘", "Ужин"),
    LATE("🌙", "Поздний"),
}

sealed interface MenuCell {
    val emoji: String
    val name: String

    data class Recipe(
        override val emoji: String,
        override val name: String,
        val author: String,
    ) : MenuCell

    data class Free(
        override val emoji: String,
        override val name: String,
    ) : MenuCell

    data class Out(
        override val emoji: String,
        override val name: String,
        val note: String? = null,
    ) : MenuCell

    data class Delivery(
        override val emoji: String,
        override val name: String,
        val price: Int? = null,
    ) : MenuCell
}

data class MenuDay(
    val date: LocalDate,
    val label: String,
    val slots: Map<MealSlot, MenuCell?> = emptyMap(),
)

data class MenuWeek(
    val days: List<MenuDay> = emptyList(),
)

enum class MenuFilter { ALL, RECIPE, FREE, OUT, DELIVERY, EMPTY }

data class MenuWeekState(
    val week: MenuWeek? = null,
    val filter: MenuFilter = MenuFilter.ALL,
    val recipesPlanned: Int = 0,
    val outsidePlanned: Int = 0,
    val deliveriesPlanned: Int = 0,
    val emptySlots: Int = 0,
    val totalIngredients: Int = 0,
    val isLoading: Boolean = false,
)

sealed interface MenuWeekIntent {
    data object PrevWeek : MenuWeekIntent
    data object NextWeek : MenuWeekIntent
    data class SetFilter(val filter: MenuFilter) : MenuWeekIntent
    data class OpenDay(val dayId: String) : MenuWeekIntent
    data class OpenSlot(val dayId: String, val slot: MealSlot) : MenuWeekIntent
    data object GenerateShoppingList : MenuWeekIntent
}
