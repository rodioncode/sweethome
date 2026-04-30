package com.jetbrains.kmpapp.screens.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetbrains.kmpapp.data.notifications.Notification
import com.jetbrains.kmpapp.data.notifications.NotificationsApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotificationsViewModel(
    private val notificationsApi: NotificationsApi,
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications

    val unreadCount: StateFlow<Int> get() = MutableStateFlow(_notifications.value.count { !it.isRead })

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            notificationsApi.getNotifications().onSuccess { wrapper ->
                _notifications.update { wrapper.notifications }
            }
        }
    }

    fun markRead(id: String) {
        viewModelScope.launch {
            notificationsApi.markRead(id).onSuccess {
                _notifications.update { list -> list.map { if (it.id == id) it.copy(isRead = true) else it } }
            }
        }
    }

    fun markAllRead() {
        viewModelScope.launch {
            notificationsApi.markAllRead().onSuccess {
                _notifications.update { list -> list.map { it.copy(isRead = true) } }
            }
        }
    }
}
