package com.jetbrains.kmpapp.data.gamification

import kotlinx.serialization.Serializable

@Serializable
data class Currency(
    val name: String,
    val icon: String,
)

@Serializable
data class PatchCurrencyRequest(
    val name: String? = null,
    val icon: String? = null,
)

@Serializable
data class LeaderboardEntry(
    val userId: String,
    val displayName: String? = null,
    val balance: Int,
    val totalEarned: Int,
)

@Serializable
data class LeaderboardWrapper(val entries: List<LeaderboardEntry>)

@Serializable
data class Prize(
    val id: String,
    val title: String,
    val description: String? = null,
    val price: Int,
    val createdAt: String? = null,
)

@Serializable
data class PrizesWrapper(val prizes: List<Prize>)

@Serializable
data class CreatePrizeRequest(
    val title: String,
    val description: String? = null,
    val price: Int,
)

@Serializable
data class PatchPrizeRequest(
    val title: String? = null,
    val description: String? = null,
    val price: Int? = null,
)

@Serializable
data class Transaction(
    val id: String,
    val userId: String,
    val amount: Int,                 // положительный — кредит, отрицательный — списание
    val sourceType: String,          // "item" | "prize_redeem" | ...
    val sourceId: String? = null,
    val createdAt: String,
)

@Serializable
data class TransactionsWrapper(val transactions: List<Transaction>)

class InsufficientBalanceException : Exception("insufficient_balance")
class ForbiddenWorkspaceTypeException : Exception("forbidden_workspace_type")
