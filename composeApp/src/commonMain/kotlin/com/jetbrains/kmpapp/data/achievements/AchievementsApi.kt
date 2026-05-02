package com.jetbrains.kmpapp.data.achievements

interface AchievementsApi {
    suspend fun catalog(): Result<List<Achievement>>
    suspend fun mine(): Result<List<Achievement>>
}
