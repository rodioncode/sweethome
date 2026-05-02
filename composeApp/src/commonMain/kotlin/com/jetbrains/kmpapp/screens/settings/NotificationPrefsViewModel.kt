package com.jetbrains.kmpapp.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetbrains.kmpapp.data.profile.NotificationPreference
import com.jetbrains.kmpapp.data.profile.ProfileApi
import com.jetbrains.kmpapp.data.profile.UpdateNotificationPreferenceRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationPrefsViewModel(
    private val profileApi: ProfileApi,
) : ViewModel() {

    private val _prefs = MutableStateFlow<List<NotificationPreference>>(emptyList())
    val prefs: StateFlow<List<NotificationPreference>> = _prefs.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            profileApi.getNotificationPreferences()
                .onSuccess { _prefs.value = it }
                .onFailure { _error.value = it.message }
        }
    }

    fun isEnabled(channel: String): Boolean =
        _prefs.value.firstOrNull { it.channel == channel }?.enabled ?: true

    fun toggle(channel: String, enabled: Boolean) {
        // optimistic
        _prefs.value = _prefs.value
            .filterNot { it.channel == channel } + NotificationPreference(channel, enabled)
        viewModelScope.launch {
            profileApi.updateNotificationPreference(
                UpdateNotificationPreferenceRequest(channel = channel, enabled = enabled)
            ).onFailure {
                _error.value = it.message
                // revert
                _prefs.value = _prefs.value
                    .filterNot { p -> p.channel == channel } + NotificationPreference(channel, !enabled)
            }
        }
    }

    fun clearError() { _error.value = null }
}
