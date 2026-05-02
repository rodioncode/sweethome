package com.jetbrains.kmpapp.data.gamification

interface GamificationApi {
    suspend fun getCurrency(workspaceId: String): Result<Currency>
    suspend fun patchCurrency(workspaceId: String, request: PatchCurrencyRequest): Result<Currency>

    suspend fun getLeaderboard(workspaceId: String, period: String = "all_time"): Result<List<LeaderboardEntry>>

    suspend fun getPrizes(workspaceId: String): Result<List<Prize>>
    suspend fun createPrize(workspaceId: String, request: CreatePrizeRequest): Result<Prize>
    suspend fun patchPrize(workspaceId: String, prizeId: String, request: PatchPrizeRequest): Result<Prize>
    suspend fun deletePrize(workspaceId: String, prizeId: String): Result<Unit>
    suspend fun redeemPrize(workspaceId: String, prizeId: String): Result<Unit>

    suspend fun getTransactions(
        workspaceId: String,
        limit: Int = 50,
        offset: Int = 0,
        userId: String? = null,
    ): Result<List<Transaction>>
}
