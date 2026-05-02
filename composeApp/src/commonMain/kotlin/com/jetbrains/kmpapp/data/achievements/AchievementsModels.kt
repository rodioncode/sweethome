package com.jetbrains.kmpapp.data.achievements

import kotlinx.serialization.Serializable

@Serializable
data class Achievement(
    val id: String,
    val title: String,
    val description: String? = null,
    val icon: String? = null,
    val earnedAt: String? = null,    // присутствует только в /me
)

@Serializable
data class AchievementsWrapper(val achievements: List<Achievement>)
