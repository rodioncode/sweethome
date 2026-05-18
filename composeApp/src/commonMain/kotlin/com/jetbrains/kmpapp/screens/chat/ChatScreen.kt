package com.jetbrains.kmpapp.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jetbrains.kmpapp.data.chat.ChatMessage
import com.jetbrains.kmpapp.ui.LocalCozyExtraColors
import com.jetbrains.kmpapp.ui.LocalCozyShapes
import com.jetbrains.kmpapp.ui.components.CozyAvatar
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ChatScreen(
    workspaceId: String,
    chatTitle: String = "Чат",
    memberCount: Int = 0,
    navigateBack: (() -> Unit)? = null,
) {
    val viewModel = koinViewModel<ChatViewModel>()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val currentUserId by viewModel.currentUserId.collectAsStateWithLifecycle()
    val connection by viewModel.connection.collectAsStateWithLifecycle()

    LaunchedEffect(workspaceId) {
        viewModel.init(workspaceId)
    }

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            ChatHeader(
                title = chatTitle,
                memberCount = memberCount,
                isOnline = connection == ChatViewModel.Connection.Online,
                navigateBack = navigateBack,
            )

            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(messages) { msg ->
                    val isMe = msg.senderId == currentUserId
                    MessageBubble(msg = msg, isMe = isMe)
                }
            }

            ChatInputBar(
                value = inputText,
                onValueChange = { inputText = it },
                onSend = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendMessage(inputText)
                        inputText = ""
                    }
                },
            )
        }
    }
}

@Composable
private fun ChatHeader(
    title: String,
    memberCount: Int,
    isOnline: Boolean,
    navigateBack: (() -> Unit)?,
) {
    val extras = LocalCozyExtraColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (navigateBack != null) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable(onClick = navigateBack),
                contentAlignment = Alignment.Center,
            ) {
                Text("←", fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)
            }
        }

        CozyAvatar(
            letter = title.firstOrNull()?.uppercase() ?: "С",
            color = MaterialTheme.colorScheme.primary,
            size = 36.dp,
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            val subtitleColor = if (isOnline) extras.success else MaterialTheme.colorScheme.onSurfaceVariant
            val subtitle = when {
                isOnline && memberCount > 0 -> "● $memberCount онлайн"
                memberCount > 0 -> "$memberCount участника"
                isOnline -> "● онлайн"
                else -> ""
            }
            if (subtitle.isNotEmpty()) {
                Text(subtitle, fontSize = 11.sp, color = subtitleColor)
            }
        }

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable {},
            contentAlignment = Alignment.Center,
        ) {
            Text("⋯", fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
        }
    }
}

@Composable
private fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    val shapes = LocalCozyShapes.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable {},
            contentAlignment = Alignment.Center,
        ) {
            Text("+", fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
                .clip(shapes.pill)
                .background(MaterialTheme.colorScheme.background)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shapes.pill)
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { inner ->
                    if (value.isEmpty()) {
                        Text(
                            "Написать сообщение…",
                            fontSize = 13.sp,
                            color = LocalCozyExtraColors.current.textTer,
                        )
                    }
                    inner()
                },
            )
        }

        val canSend = value.isNotBlank()
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (canSend) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
                .clickable(enabled = canSend, onClick = onSend),
            contentAlignment = Alignment.Center,
        ) {
            Text("↑", fontSize = 16.sp, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun MessageBubble(msg: ChatMessage, isMe: Boolean) {
    val shapes = LocalCozyShapes.current
    val extras = LocalCozyExtraColors.current
    val initial = msg.senderName.firstOrNull()?.uppercase() ?: "?"
    val senderColor = remember(msg.senderId) {
        val palette = listOf(extras.lavender, extras.coral, extras.ochre, extras.primaryLight)
        palette[(msg.senderId.hashCode() and 0x7FFFFFFF) % palette.size]
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        if (!isMe) {
            CozyAvatar(letter = initial, color = senderColor, size = 28.dp)
            Spacer(Modifier.size(8.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 280.dp),
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
        ) {
            Box(
                modifier = Modifier
                    .clip(if (isMe) shapes.chatBubbleMine else shapes.chatBubbleTheirs)
                    .background(if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Column {
                    if (!isMe) {
                        Text(
                            msg.senderName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = senderColor,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                    }
                    Text(
                        msg.content,
                        fontSize = 14.sp,
                        color = if (isMe) Color.White else MaterialTheme.colorScheme.onBackground,
                        lineHeight = 19.sp,
                    )
                    Text(
                        msg.createdAt,
                        fontSize = 10.sp,
                        color = if (isMe) Color.White.copy(alpha = 0.7f) else extras.textTer,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }

        if (isMe) {
            Spacer(Modifier.size(8.dp))
        }
    }
}
