package com.jetbrains.kmpapp.data.achievements

import com.jetbrains.kmpapp.auth.ApiEnvelope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class KtorAchievementsApi(
    private val apiClient: HttpClient,
    private val baseUrl: String,
) : AchievementsApi {

    override suspend fun catalog(): Result<List<Achievement>> = runCatching {
        val env: ApiEnvelope<AchievementsWrapper> = apiClient.get("$baseUrl/achievements").body()
        require(env.error == null) { env.error?.message ?: "achievements_failed" }
        env.data?.achievements ?: emptyList()
    }

    override suspend fun mine(): Result<List<Achievement>> = runCatching {
        val env: ApiEnvelope<AchievementsWrapper> = apiClient.get("$baseUrl/achievements/me").body()
        require(env.error == null) { env.error?.message ?: "my_achievements_failed" }
        env.data?.achievements ?: emptyList()
    }
}
