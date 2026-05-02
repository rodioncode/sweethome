package com.jetbrains.kmpapp.data.gamification

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GamificationRepository(
    private val api: GamificationApi,
) {
    private val _currency = MutableStateFlow<Currency?>(null)
    val currency: StateFlow<Currency?> = _currency.asStateFlow()

    private val _leaderboard = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    val leaderboard: StateFlow<List<LeaderboardEntry>> = _leaderboard.asStateFlow()

    private val _prizes = MutableStateFlow<List<Prize>>(emptyList())
    val prizes: StateFlow<List<Prize>> = _prizes.asStateFlow()

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var loadedWorkspaceId: String? = null

    suspend fun loadAll(workspaceId: String) {
        loadedWorkspaceId = workspaceId
        api.getCurrency(workspaceId).onSuccess { _currency.value = it }
        api.getLeaderboard(workspaceId).onSuccess { _leaderboard.value = it }
        api.getPrizes(workspaceId).onSuccess { _prizes.value = it }
    }

    suspend fun loadTransactions(workspaceId: String, userId: String? = null) {
        api.getTransactions(workspaceId, userId = userId)
            .onSuccess { _transactions.value = it }
            .onFailure { _error.value = it.message }
    }

    suspend fun patchCurrency(workspaceId: String, name: String?, icon: String?): Result<Currency> {
        val res = api.patchCurrency(workspaceId, PatchCurrencyRequest(name = name, icon = icon))
        res.onSuccess { _currency.value = it }
        res.onFailure { _error.value = it.message }
        return res
    }

    suspend fun createPrize(workspaceId: String, title: String, description: String?, price: Int): Result<Prize> {
        val res = api.createPrize(workspaceId, CreatePrizeRequest(title, description, price))
        res.onSuccess { p -> _prizes.value = listOf(p) + _prizes.value }
        res.onFailure { _error.value = it.message }
        return res
    }

    suspend fun patchPrize(workspaceId: String, prizeId: String, title: String?, description: String?, price: Int?): Result<Prize> {
        val res = api.patchPrize(workspaceId, prizeId, PatchPrizeRequest(title, description, price))
        res.onSuccess { p -> _prizes.value = _prizes.value.map { if (it.id == prizeId) p else it } }
        res.onFailure { _error.value = it.message }
        return res
    }

    suspend fun deletePrize(workspaceId: String, prizeId: String): Result<Unit> {
        val res = api.deletePrize(workspaceId, prizeId)
        res.onSuccess { _prizes.value = _prizes.value.filter { it.id != prizeId } }
        res.onFailure { _error.value = it.message }
        return res
    }

    suspend fun redeemPrize(workspaceId: String, prizeId: String): Result<Unit> {
        val res = api.redeemPrize(workspaceId, prizeId)
        res.onSuccess {
            // Обновим leaderboard и transactions, чтобы UI показал новый баланс/историю.
            api.getLeaderboard(workspaceId).onSuccess { _leaderboard.value = it }
        }
        res.onFailure { if (it !is InsufficientBalanceException) _error.value = it.message }
        return res
    }

    fun clearError() { _error.value = null }
    fun clearAll() {
        _currency.value = null
        _leaderboard.value = emptyList()
        _prizes.value = emptyList()
        _transactions.value = emptyList()
        _error.value = null
        loadedWorkspaceId = null
    }
}
