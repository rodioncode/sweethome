package com.jetbrains.kmpapp.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetbrains.kmpapp.auth.AuthRepository
import com.jetbrains.kmpapp.auth.AuthState
import com.jetbrains.kmpapp.data.lists.ListsRepository
import com.jetbrains.kmpapp.data.lists.TodoList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    private val authRepository: AuthRepository,
    private val listsRepository: ListsRepository,
) : ViewModel() {

    val isGuest: StateFlow<Boolean> = authRepository.authState
        .map { (it as? AuthState.Authenticated)?.isGuest ?: false }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val userId: StateFlow<String?> = authRepository.authState
        .map { (it as? AuthState.Authenticated)?.userId }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val recentLists: StateFlow<List<TodoList>> = listsRepository.lists
        .map { it.take(3) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
}
