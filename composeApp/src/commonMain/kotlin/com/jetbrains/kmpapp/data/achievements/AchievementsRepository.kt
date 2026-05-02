package com.jetbrains.kmpapp.data.achievements

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AchievementsRepository(
    private val api: AchievementsApi,
) {
    private val _catalog = MutableStateFlow<List<Achievement>>(emptyList())
    val catalog: StateFlow<List<Achievement>> = _catalog.asStateFlow()

    private val _mine = MutableStateFlow<List<Achievement>>(emptyList())
    val mine: StateFlow<List<Achievement>> = _mine.asStateFlow()

    suspend fun load() {
        api.catalog().onSuccess { _catalog.value = it }
        api.mine().onSuccess { _mine.value = it }
    }

    fun clearAll() {
        _catalog.value = emptyList()
        _mine.value = emptyList()
    }
}
