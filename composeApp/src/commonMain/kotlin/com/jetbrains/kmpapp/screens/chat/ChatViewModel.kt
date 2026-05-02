package com.jetbrains.kmpapp.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetbrains.kmpapp.auth.AuthRepository
import com.jetbrains.kmpapp.auth.AuthState
import com.jetbrains.kmpapp.data.chat.ChatApi
import com.jetbrains.kmpapp.data.chat.ChatMessage
import com.jetbrains.kmpapp.data.chat.ChatStreamEvent
import com.jetbrains.kmpapp.data.chat.SendMessageRequest
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel(
    private val chatApi: ChatApi,
    private val authRepository: AuthRepository,
) : ViewModel() {

    enum class Connection { Idle, Connecting, Online, Reconnecting }

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId

    private val _connection = MutableStateFlow(Connection.Idle)
    val connection: StateFlow<Connection> = _connection

    private var workspaceId: String = ""
    private var streamJob: Job? = null

    fun init(workspaceId: String) {
        if (this.workspaceId == workspaceId) return
        this.workspaceId = workspaceId
        viewModelScope.launch {
            val state = authRepository.authState.first()
            _currentUserId.update { (state as? AuthState.Authenticated)?.userId }
            chatApi.markRead(workspaceId)
        }
        startStream()
    }

    private fun startStream() {
        streamJob?.cancel()
        streamJob = viewModelScope.launch {
            chatApi.streamMessages(workspaceId).collect { ev ->
                when (ev) {
                    is ChatStreamEvent.Snapshot -> {
                        _messages.update { ev.messages }
                        _connection.value = Connection.Online
                    }
                    ChatStreamEvent.Connecting -> _connection.value = Connection.Connecting
                    is ChatStreamEvent.Disconnected -> _connection.value = Connection.Reconnecting
                }
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

    override fun onCleared() {
        streamJob?.cancel()
        super.onCleared()
    }
}
