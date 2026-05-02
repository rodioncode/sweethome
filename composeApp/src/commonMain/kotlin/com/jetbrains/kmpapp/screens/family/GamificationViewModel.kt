package com.jetbrains.kmpapp.screens.family

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetbrains.kmpapp.auth.AuthRepository
import com.jetbrains.kmpapp.auth.AuthState
import com.jetbrains.kmpapp.data.achievements.Achievement
import com.jetbrains.kmpapp.data.achievements.AchievementsRepository
import com.jetbrains.kmpapp.data.gamification.Currency
import com.jetbrains.kmpapp.data.gamification.GamificationRepository
import com.jetbrains.kmpapp.data.gamification.InsufficientBalanceException
import com.jetbrains.kmpapp.data.gamification.LeaderboardEntry
import com.jetbrains.kmpapp.data.gamification.Prize
import com.jetbrains.kmpapp.data.gamification.Transaction
import com.jetbrains.kmpapp.data.groups.GroupsRepository
import com.jetbrains.kmpapp.data.groups.WorkspaceRole
import com.jetbrains.kmpapp.data.groups.WorkspaceType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GamificationViewModel(
    private val gamification: GamificationRepository,
    private val groupsRepository: GroupsRepository,
    private val authRepository: AuthRepository,
    private val achievementsRepository: AchievementsRepository,
) : ViewModel() {

    sealed class Event {
        data class Toast(val message: String) : Event()
    }

    val familyWorkspace = groupsRepository.groups
        .map { gs -> gs.firstOrNull { it.type == WorkspaceType.FAMILY } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val isOwnerOrAdmin: StateFlow<Boolean> = familyWorkspace
        .map { it?.role == WorkspaceRole.OWNER || it?.role == WorkspaceRole.ADMIN }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val currentUserId: StateFlow<String?> = authRepository.authState
        .map { (it as? AuthState.Authenticated)?.userId }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val currency: StateFlow<Currency?> = gamification.currency
    val leaderboard: StateFlow<List<LeaderboardEntry>> = gamification.leaderboard
    val prizes: StateFlow<List<Prize>> = gamification.prizes
    val transactions: StateFlow<List<Transaction>> = gamification.transactions

    val achievementsCatalog: StateFlow<List<Achievement>> = achievementsRepository.catalog
    val achievementsMine: StateFlow<List<Achievement>> = achievementsRepository.mine

    private val _events = MutableSharedFlow<Event>()
    val events: SharedFlow<Event> = _events.asSharedFlow()

    init {
        viewModelScope.launch { groupsRepository.loadGroups() }
        viewModelScope.launch { achievementsRepository.load() }
        viewModelScope.launch {
            familyWorkspace.collect { ws ->
                if (ws != null) {
                    gamification.loadAll(ws.id)
                    gamification.loadTransactions(ws.id)
                } else {
                    gamification.clearAll()
                }
            }
        }
    }

    fun refresh() {
        val ws = familyWorkspace.value ?: return
        viewModelScope.launch {
            gamification.loadAll(ws.id)
            gamification.loadTransactions(ws.id)
            achievementsRepository.load()
        }
    }

    fun updateCurrency(name: String, icon: String) {
        val ws = familyWorkspace.value ?: return
        viewModelScope.launch {
            gamification.patchCurrency(ws.id, name, icon)
                .onFailure { _events.emit(Event.Toast(it.message ?: "Не удалось обновить")) }
        }
    }

    fun createPrize(title: String, description: String?, price: Int) {
        val ws = familyWorkspace.value ?: return
        viewModelScope.launch {
            gamification.createPrize(ws.id, title, description?.ifBlank { null }, price)
                .onFailure { _events.emit(Event.Toast(it.message ?: "Не удалось создать приз")) }
        }
    }

    fun updatePrize(prizeId: String, title: String?, description: String?, price: Int?) {
        val ws = familyWorkspace.value ?: return
        viewModelScope.launch {
            gamification.patchPrize(ws.id, prizeId, title, description, price)
                .onFailure { _events.emit(Event.Toast(it.message ?: "Не удалось обновить")) }
        }
    }

    fun deletePrize(prizeId: String) {
        val ws = familyWorkspace.value ?: return
        viewModelScope.launch {
            gamification.deletePrize(ws.id, prizeId)
                .onFailure { _events.emit(Event.Toast(it.message ?: "Не удалось удалить")) }
        }
    }

    fun redeemPrize(prizeId: String) {
        val ws = familyWorkspace.value ?: return
        viewModelScope.launch {
            gamification.redeemPrize(ws.id, prizeId)
                .onSuccess { _events.emit(Event.Toast("Приз получен")) }
                .onFailure { err ->
                    _events.emit(
                        Event.Toast(
                            if (err is InsufficientBalanceException) "Недостаточно баланса"
                            else err.message ?: "Не удалось купить",
                        ),
                    )
                }
        }
    }
}
