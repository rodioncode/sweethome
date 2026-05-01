package com.jetbrains.kmpapp.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    val authState = authRepository.authState

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.login(email, password)
                .onSuccess { _uiState.value = AuthUiState.Success }
                .onFailure { _uiState.value = AuthUiState.Error(it.message ?: "Ошибка входа") }
        }
    }

    fun register(email: String, password: String, displayName: String, acceptedTerms: Boolean) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.register(email, password, displayName, acceptedTerms)
                .onSuccess { _uiState.value = AuthUiState.Success }
                .onFailure { _uiState.value = AuthUiState.Error(it.message ?: "Ошибка регистрации") }
        }
    }

    fun loginAsGuest() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.loginAsGuest()
                .onSuccess { _uiState.value = AuthUiState.Success }
                .onFailure { _uiState.value = AuthUiState.Error(it.message ?: "Ошибка") }
        }
    }

    fun linkEmail(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.linkEmail(email, password, displayName)
                .onSuccess { _uiState.value = AuthUiState.Success }
                .onFailure { _uiState.value = AuthUiState.Error(it.message ?: "Ошибка привязки") }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.logout()
            _uiState.value = AuthUiState.Idle
        }
    }

    fun requestPasswordReset(email: String, onSent: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.requestPasswordReset(email)
                .onSuccess { _uiState.value = AuthUiState.Idle; onSent() }
                .onFailure { _uiState.value = AuthUiState.Error(it.message ?: "Ошибка") }
        }
    }

    fun confirmPasswordReset(token: String, newPassword: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.confirmPasswordReset(token, newPassword)
                .onSuccess { _uiState.value = AuthUiState.Idle; onSuccess() }
                .onFailure { _uiState.value = AuthUiState.Error(it.message ?: "Ошибка") }
        }
    }

    fun clearError() {
        _uiState.value = AuthUiState.Idle
    }
}

sealed class AuthUiState {
    data object Idle : AuthUiState()
    data object Loading : AuthUiState()
    data object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}
