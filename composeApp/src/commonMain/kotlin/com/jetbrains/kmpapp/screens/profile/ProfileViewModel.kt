package com.jetbrains.kmpapp.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetbrains.kmpapp.auth.AuthRepository
import com.jetbrains.kmpapp.auth.AuthState
import com.jetbrains.kmpapp.data.groups.Group
import com.jetbrains.kmpapp.data.groups.GroupsRepository
import com.jetbrains.kmpapp.data.profile.ProfileApi
import com.jetbrains.kmpapp.data.profile.TooManyRequestsException
import com.jetbrains.kmpapp.data.profile.UserProfile
import com.jetbrains.kmpapp.data.telegram.TelegramApi
import com.jetbrains.kmpapp.data.telegram.TelegramLinkStatusResponse
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val groupsRepository: GroupsRepository,
    private val profileApi: ProfileApi,
    private val telegramApi: TelegramApi,
) : ViewModel() {

    val isGuest: StateFlow<Boolean> = authRepository.authState
        .map { (it as? AuthState.Authenticated)?.isGuest ?: false }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val userId: StateFlow<String?> = authRepository.authState
        .map { (it as? AuthState.Authenticated)?.userId }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val groups: StateFlow<List<Group>> = groupsRepository.groups
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile

    val listCount: StateFlow<Int> = _profile
        .map { it?.stats?.totalLists ?: 0 }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val groupCount: StateFlow<Int> = _profile
        .map { it?.stats?.workspacesCount ?: 0 }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    init {
        loadProfile()
        loadTelegramStatus()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            profileApi.getProfile().onSuccess { _profile.update { _ -> it } }
        }
    }

    fun updateDisplayName(name: String) {
        viewModelScope.launch {
            profileApi.updateProfile(
                com.jetbrains.kmpapp.data.profile.UpdateProfileRequest(displayName = name)
            ).onSuccess { _profile.update { _ -> it } }
        }
    }

    fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }

    sealed class DeleteState {
        data object Idle : DeleteState()
        data object InProgress : DeleteState()
        data object Done : DeleteState()
        data class Error(val message: String) : DeleteState()
    }

    private val _deleteState = MutableStateFlow<DeleteState>(DeleteState.Idle)
    val deleteState: StateFlow<DeleteState> = _deleteState

    fun deleteAccount() {
        if (_deleteState.value is DeleteState.InProgress) return
        _deleteState.value = DeleteState.InProgress
        viewModelScope.launch {
            profileApi.deleteAccount()
                .onSuccess {
                    _deleteState.value = DeleteState.Done
                    authRepository.logout()
                }
                .onFailure {
                    _deleteState.value = DeleteState.Error(
                        if (it is TooManyRequestsException) "Слишком много попыток. Попробуйте завтра."
                        else it.message ?: "Не удалось удалить аккаунт"
                    )
                }
        }
    }

    fun resetDeleteState() { _deleteState.value = DeleteState.Idle }

    // ----- Telegram link -----

    sealed class TelegramLinkState {
        data object Idle : TelegramLinkState()
        data object Starting : TelegramLinkState()
        data class Pending(val code: String, val expiresAt: String, val deeplink: String) : TelegramLinkState()
        data class Error(val message: String) : TelegramLinkState()
    }

    private val _telegramStatus = MutableStateFlow<TelegramLinkStatusResponse?>(null)
    val telegramStatus: StateFlow<TelegramLinkStatusResponse?> = _telegramStatus

    private val _telegramLinkState = MutableStateFlow<TelegramLinkState>(TelegramLinkState.Idle)
    val telegramLinkState: StateFlow<TelegramLinkState> = _telegramLinkState

    private var pollingJob: Job? = null

    private fun loadTelegramStatus() {
        viewModelScope.launch {
            telegramApi.getStatus().onSuccess { _telegramStatus.value = it }
        }
    }

    fun startTelegramLink() {
        if (_telegramLinkState.value is TelegramLinkState.Starting) return
        _telegramLinkState.value = TelegramLinkState.Starting
        viewModelScope.launch {
            telegramApi.startLink()
                .onSuccess { resp ->
                    _telegramLinkState.value = TelegramLinkState.Pending(
                        code = resp.code,
                        expiresAt = resp.expiresAt,
                        deeplink = resp.deeplink,
                    )
                    startPolling(resp.expiresAt)
                }
                .onFailure {
                    _telegramLinkState.value = TelegramLinkState.Error(it.message ?: "Не удалось получить код")
                }
        }
    }

    private fun startPolling(expiresAtIso: String) {
        pollingJob?.cancel()
        val expiresAt = parseInstantOrNull(expiresAtIso)
        pollingJob = viewModelScope.launch {
            while (isActive && _telegramLinkState.value is TelegramLinkState.Pending) {
                if (expiresAt != null && Clock.System.now() >= expiresAt) break
                delay(3_000)
                if (_telegramLinkState.value !is TelegramLinkState.Pending) break
                checkOnce()
            }
        }
    }

    fun checkLinkNow() {
        viewModelScope.launch { checkOnce() }
    }

    private suspend fun checkOnce() {
        telegramApi.getStatus().onSuccess { status ->
            _telegramStatus.value = status
            if (status.linked) {
                pollingJob?.cancel()
                _telegramLinkState.value = TelegramLinkState.Idle
            }
        }
    }

    fun cancelTelegramLink() {
        pollingJob?.cancel()
        pollingJob = null
        _telegramLinkState.value = TelegramLinkState.Idle
    }

    fun unlinkTelegram() {
        viewModelScope.launch {
            telegramApi.unlink().onSuccess {
                _telegramStatus.value = TelegramLinkStatusResponse(linked = false)
            }
        }
    }

    override fun onCleared() {
        pollingJob?.cancel()
        super.onCleared()
    }

    private fun parseInstantOrNull(iso: String?): Instant? = try {
        if (iso == null) null else Instant.parse(iso)
    } catch (_: Throwable) {
        null
    }
}
