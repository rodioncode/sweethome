package com.jetbrains.kmpapp.screens.tasks

import com.jetbrains.kmpapp.ui.models.ListType

object AddTaskFieldsSpec {

    fun titleLabelFor(type: ListType): String = when (type) {
        ListType.SHOPPING      -> "ЧТО КУПИТЬ"
        ListType.HOME_CHORES   -> "ЧТО СДЕЛАТЬ"
        ListType.GENERAL_TODOS -> "ЗАДАЧА"
        ListType.STUDY         -> "ЧТО ВЫУЧИТЬ"
        ListType.TRAVEL        -> "ЧТО ВЗЯТЬ"
        ListType.MEDIA         -> "НАЗВАНИЕ"
        ListType.WISHLIST      -> "ЧЕГО ХОЧУ"
        ListType.CUSTOM        -> "НАЗВАНИЕ КАРТОЧКИ"
    }

    fun titlePlaceholderFor(type: ListType): String = when (type) {
        ListType.SHOPPING      -> "Молоко 2.5%"
        ListType.HOME_CHORES   -> "Пропылесосить"
        ListType.GENERAL_TODOS -> "Записаться к стоматологу"
        ListType.STUDY         -> "Параграф 14, упр. 3–5"
        ListType.TRAVEL        -> "Паспорта детей"
        ListType.MEDIA         -> "Сто лет одиночества"
        ListType.WISHLIST      -> "Велосипед «Стелс»"
        ListType.CUSTOM        -> "Новый пункт"
    }

    fun fieldsFor(type: ListType): List<FieldBlock> = when (type) {
        ListType.SHOPPING      -> shoppingFields
        ListType.HOME_CHORES   -> homeChoresFields
        ListType.GENERAL_TODOS -> generalFields
        ListType.STUDY         -> studyFields
        ListType.TRAVEL        -> travelFields
        ListType.MEDIA         -> mediaFields
        ListType.WISHLIST      -> wishlistFields
        ListType.CUSTOM        -> customFields
    }

    private val shoppingFields = listOf(
        FieldBlock.Pair(
            "qty", label = "КОЛИЧЕСТВО",
            left = FieldBlock.Pair.Field("amount", "СКОЛЬКО", "2"),
            right = FieldBlock.Pair.Field("unit", "ЕД. ИЗМ.", "л"),
        ),
        FieldBlock.Chips(
            "category", label = "КАТЕГОРИЯ",
            options = listOf(
                "🥛 Молочное" to true, "🥩 Мясо" to false,
                "🥦 Овощи" to false, "🍎 Фрукты" to false, "🥖 Хлеб" to false,
            ),
        ),
        FieldBlock.TextArea(
            "details", label = "БРЕНД / ДЕТАЛИ",
            placeholder = "Бренд, жирность, упаковка…",
            value = "",
        ),
        FieldBlock.Pair(
            "store", label = "МАГАЗИН И ЦЕНА",
            left = FieldBlock.Pair.Field("where", "ГДЕ", ""),
            right = FieldBlock.Pair.Field("price", "~ЦЕНА, ₽", ""),
        ),
    )

    private val homeChoresFields = listOf(
        FieldBlock.Chips("zone", "ЗОНА ДОМА", listOf(
            "🍳 Кухня" to false, "🛋 Гостиная" to false, "🛏 Спальня" to false,
            "🚿 Ванная" to false, "🏞 Балкон" to false,
        )),
        FieldBlock.Chips("repeat", "ПОВТОР", listOf(
            "Один раз" to true, "Ежедневно" to false,
            "2 раза в неделю" to false, "Еженедельно" to false, "Раз в месяц" to false,
        )),
        FieldBlock.Pair("effort", "ВРЕМЯ И УСИЛИЯ",
            left = FieldBlock.Pair.Field("duration", "ДЛИТЕЛЬНОСТЬ", ""),
            right = FieldBlock.Pair.Field("points", "ОЧКИ", ""),
        ),
    )

    private val generalFields = listOf(
        FieldBlock.Chips("prio", "ПРИОРИТЕТ", listOf(
            "🔴 Срочно" to false, "🟡 Важно" to false, "🟢 Можно отложить" to false,
        )),
        FieldBlock.Pair("deadline", "СРОК",
            left = FieldBlock.Pair.Field("date", "ДЕДЛАЙН", ""),
            right = FieldBlock.Pair.Field("notify", "НАПОМНИТЬ", "За день"),
        ),
    )

    private val studyFields = listOf(
        FieldBlock.Chips("subject", "ПРЕДМЕТ", listOf(
            "📐 Математика" to false, "📖 Литература" to false,
            "🌍 География" to false, "🧪 Химия" to false,
        )),
        FieldBlock.Chips("kind", "ТИП ЗАДАНИЯ", listOf(
            "Домашка" to false, "Контрольная" to false,
            "Экзамен" to false, "Проект" to false,
        )),
        FieldBlock.Pair("term", "СРОКИ",
            left = FieldBlock.Pair.Field("due", "СДАТЬ К", ""),
            right = FieldBlock.Pair.Field("grade", "ОЦЕНКА ЗА", ""),
        ),
    )

    private val travelFields = listOf(
        FieldBlock.Chips("category", "КАТЕГОРИЯ", listOf(
            "📄 Документы" to false, "👕 Одежда" to false, "💊 Аптечка" to false,
            "🔌 Техника" to false, "🧴 Гигиена" to false,
        )),
        FieldBlock.Pair("qty", "СКОЛЬКО",
            left = FieldBlock.Pair.Field("amount", "КОЛИЧЕСТВО", ""),
            right = FieldBlock.Pair.Field("unit", "ЕД. ИЗМ.", "шт"),
        ),
    )

    private val mediaFields = listOf(
        FieldBlock.Chips("type", "ТИП", listOf(
            "📖 Книга" to false, "🎬 Фильм" to false, "📺 Сериал" to false,
            "🎮 Игра" to false, "🎧 Подкаст" to false,
        )),
        FieldBlock.Pair("origin", "АВТОР И ГОД",
            left = FieldBlock.Pair.Field("author", "АВТОР", ""),
            right = FieldBlock.Pair.Field("year", "ГОД", ""),
        ),
        FieldBlock.Segmented("status", "СТАТУС",
            options = listOf("Хочу" to true, "Сейчас" to false, "Прочитано" to false),
        ),
        FieldBlock.Rating("rating", "РЕЙТИНГ", value = 0),
    )

    private val wishlistFields = listOf(
        FieldBlock.Pair("price", "ЦЕНА",
            left = FieldBlock.Pair.Field("min", "ОТ", ""),
            right = FieldBlock.Pair.Field("max", "ДО", ""),
        ),
        FieldBlock.TextArea(
            "link", label = "ССЫЛКА",
            placeholder = "Вставьте URL магазина",
            value = "",
        ),
        FieldBlock.Chips("occasion", "ПОВОД", listOf(
            "🎂 День рождения" to false, "🎄 Новый год" to false,
            "🎓 За оценки" to false, "💝 Просто так" to false,
        )),
    )

    private val customFields = listOf(
        FieldBlock.CustomFields(
            "fields", label = "ВАШИ ПОЛЯ",
            fields = emptyList(),
        ),
        FieldBlock.AddField,
    )
}

sealed interface FieldBlock {
    data class Pair(
        val key: String,
        val label: String,
        val left: Field,
        val right: Field,
    ) : FieldBlock {
        data class Field(val key: String, val label: String, val value: String)
    }

    data class Chips(
        val key: String,
        val label: String,
        val options: List<kotlin.Pair<String, Boolean>>,
    ) : FieldBlock

    data class Segmented(
        val key: String,
        val label: String,
        val options: List<kotlin.Pair<String, Boolean>>,
    ) : FieldBlock

    data class TextArea(
        val key: String,
        val label: String,
        val placeholder: String,
        val value: String,
    ) : FieldBlock

    data class Rating(val key: String, val label: String, val value: Int) : FieldBlock

    data class CustomFields(
        val key: String,
        val label: String,
        val fields: List<kotlin.Pair<String, String>>,
    ) : FieldBlock

    data object AddField : FieldBlock
}
