package com.jetbrains.kmpapp.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetbrains.kmpapp.auth.AuthRepository
import com.jetbrains.kmpapp.auth.AuthState
import com.jetbrains.kmpapp.data.chat.ChatApi
import com.jetbrains.kmpapp.data.chat.ChatMessage
import com.jetbrains.kmpapp.data.chat.SendMessageRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel(
    private val chatApi: ChatApi,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId

    private var workspaceId: String = ""

    fun init(workspaceId: String) {
        if (this.workspaceId == workspaceId) return
        this.workspaceId = workspaceId
        viewModelScope.launch {
            val state = authRepository.authState.first()
            _currentUserId.update { (state as? AuthState.Authenticated)?.userId }
            loadMessages()
            chatApi.markRead(workspaceId)
        }
    }

    fun loadMessages() {
        viewModelScope.launch {
            chatApi.getMessages(workspaceId).onSuccess { msgs ->
                _messages.update { msgs }
            }
        }
    }

    fun sendMessage(content: String) {
        val trimmed = content.trim()
        if (trimmed.isBlank() || workspaceId.isBlank()) return
        viewModelScope.launch {
            chatApi.sendMessage(workspaceId, SendMessageRequest(trimmed)).onSuccess { msg ->
                _messages.update { it + msg }
            }
        }
    }
}
