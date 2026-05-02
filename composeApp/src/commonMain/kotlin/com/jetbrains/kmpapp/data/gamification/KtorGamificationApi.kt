package com.jetbrains.kmpapp.data.gamification

import com.jetbrains.kmpapp.auth.ApiEnvelope
import com.jetbrains.kmpapp.auth.EmptyResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class KtorGamificationApi(
    private val apiClient: HttpClient,
    private val baseUrl: String,
) : GamificationApi {

    override suspend fun getCurrency(workspaceId: String): Result<Currency> = runCatching {
        val env: ApiEnvelope<Currency> = apiClient.get("$baseUrl/workspaces/$workspaceId/currency").body()
        require(env.error == null) { env.error?.message ?: "currency_failed" }
        env.data ?: throw IllegalStateException("no_currency")
    }

    override suspend fun patchCurrency(workspaceId: String, request: PatchCurrencyRequest): Result<Currency> = runCatching {
        val env: ApiEnvelope<Currency> = apiClient.patch("$baseUrl/workspaces/$workspaceId/currency") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
        when (env.error?.code) {
            "forbidden_workspace_type" -> throw ForbiddenWorkspaceTypeException()
        }
        require(env.error == null) { env.error?.message ?: "patch_currency_failed" }
        env.data ?: throw IllegalStateException("no_currency")
    }

    override suspend fun getLeaderboard(workspaceId: String, period: String): Result<List<LeaderboardEntry>> = runCatching {
        val env: ApiEnvelope<LeaderboardWrapper> = apiClient.get("$baseUrl/workspaces/$workspaceId/leaderboard") {
            parameter("period", period)
        }.body()
        require(env.error == null) { env.error?.message ?: "leaderboard_failed" }
        env.data?.entries ?: emptyList()
    }

    override suspend fun getPrizes(workspaceId: String): Result<List<Prize>> = runCatching {
        val env: ApiEnvelope<PrizesWrapper> = apiClient.get("$baseUrl/workspaces/$workspaceId/prizes").body()
        require(env.error == null) { env.error?.message ?: "prizes_failed" }
        env.data?.prizes ?: emptyList()
    }

    override suspend fun createPrize(workspaceId: String, request: CreatePrizeRequest): Result<Prize> = runCatching {
        val env: ApiEnvelope<Prize> = apiClient.post("$baseUrl/workspaces/$workspaceId/prizes") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
        require(env.error == null) { env.error?.message ?: "create_prize_failed" }
        env.data ?: throw IllegalStateException("no_prize")
    }

    override suspend fun patchPrize(workspaceId: String, prizeId: String, request: PatchPrizeRequest): Result<Prize> = runCatching {
        val env: ApiEnvelope<Prize> = apiClient.patch("$baseUrl/workspaces/$workspaceId/prizes/$prizeId") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
        require(env.error == null) { env.error?.message ?: "patch_prize_failed" }
        env.data ?: throw IllegalStateException("no_prize")
    }

    override suspend fun deletePrize(workspaceId: String, prizeId: String): Result<Unit> = runCatching {
        val env: ApiEnvelope<EmptyResponse> = apiClient.delete("$baseUrl/workspaces/$workspaceId/prizes/$prizeId").body()
        require(env.error == null) { env.error?.message ?: "delete_prize_failed" }
        Unit
    }

    override suspend fun redeemPrize(workspaceId: String, prizeId: String): Result<Unit> = runCatching {
        val env: ApiEnvelope<EmptyResponse> = apiClient.post("$baseUrl/workspaces/$workspaceId/prizes/$prizeId/redeem").body()
        when (env.error?.code) {
            "insufficient_balance" -> throw InsufficientBalanceException()
        }
        require(env.error == null) { env.error?.message ?: "redeem_failed" }
        Unit
    }

    override suspend fun getTransactions(
        workspaceId: String,
        limit: Int,
        offset: Int,
        userId: String?,
    ): Result<List<Transaction>> = runCatching {
        val env: ApiEnvelope<TransactionsWrapper> = apiClient.get("$baseUrl/workspaces/$workspaceId/transactions") {
            parameter("limit", limit)
            parameter("offset", offset)
            userId?.let { parameter("userId", it) }
        }.body()
        require(env.error == null) { env.error?.message ?: "transactions_failed" }
        env.data?.transactions ?: emptyList()
    }
}
