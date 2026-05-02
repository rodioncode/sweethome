package com.jetbrains.kmpapp.screens.wishlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetbrains.kmpapp.auth.AuthRepository
import com.jetbrains.kmpapp.auth.AuthState
import com.jetbrains.kmpapp.data.wishlist.AlreadyClaimedException
import com.jetbrains.kmpapp.data.wishlist.ClaimRequest
import com.jetbrains.kmpapp.data.wishlist.PublicWishlist
import com.jetbrains.kmpapp.data.wishlist.PublicWishlistApi
import com.jetbrains.kmpapp.data.wishlist.WishlistNotFoundException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PublicWishlistViewModel(
    private val api: PublicWishlistApi,
    private val authRepository: AuthRepository,
) : ViewModel() {

    sealed class State {
        data object Loading : State()
        data class Loaded(val wishlist: PublicWishlist) : State()
        data object NotFound : State()
        data class Error(val message: String) : State()
    }

    private val _state = MutableStateFlow<State>(State.Loading)
    val state: StateFlow<State> = _state.asStateFlow()

    private val _toast = MutableStateFlow<String?>(null)
    val toast: StateFlow<String?> = _toast.asStateFlow()

    private var token: String = ""

    fun load(token: String) {
        this.token = token
        viewModelScope.launch {
            _state.value = State.Loading
            api.getPublic(token)
                .onSuccess { _state.value = State.Loaded(it) }
                .onFailure {
                    _state.value = if (it is WishlistNotFoundException) State.NotFound
                    else State.Error(it.message ?: "Не удалось открыть список")
                }
        }
    }

    fun claim(itemId: String, name: String, isAnonymous: Boolean) {
        val userId = (authRepository.authState.value as? AuthState.Authenticated)?.userId
        viewModelScope.launch {
            api.claim(token, itemId, ClaimRequest(name, isAnonymous, userId))
                .onSuccess {
                    _toast.value = "Подарок забронирован"
                    load(token) // reload to reflect isClaimed
                }
                .onFailure {
                    _toast.value = if (it is AlreadyClaimedException) "Уже забронировано"
                    else it.message ?: "Не удалось забронировать"
                }
        }
    }

    fun clearToast() { _toast.value = null }
}
