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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val groupsRepository: GroupsRepository,
    private val profileApi: ProfileApi,
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
}
