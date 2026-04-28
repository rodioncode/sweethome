package com.jetbrains.kmpapp.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetbrains.kmpapp.ui.DividerColor
import com.jetbrains.kmpapp.ui.PrimaryGreen
import com.jetbrains.kmpapp.ui.PrimaryGreenLight
import com.jetbrains.kmpapp.ui.SurfaceVariantCream
import com.jetbrains.kmpapp.ui.SurfaceWhite
import com.jetbrains.kmpapp.ui.TextPrimary
import com.jetbrains.kmpapp.ui.TextSecondary

private data class ChatMessage(
    val id: String,
    val senderId: String,
    val senderName: String,
    val senderInitials: String,
    val senderColor: Color,
    val text: String,
    val time: String,
)

private val initialMessages = listOf(
    ChatMessage("m1", "u2", "Дима", "ДН", Color(0xFF42A5F5), "Привет всем! Кто идёт за покупками сегодня?", "10:30"),
    ChatMessage("m2", "u1", "Аня", "АН", Color(0xFF5B7C5A), "Я пойду часа через два", "10:31"),
    ChatMessage("m3", "u3", "Соня", "СН", Color(0xFFAB47BC), "Добавила молоко и хлеб в список 🥛", "10:45"),
    ChatMessage("m4", "u2", "Дима", "ДН", Color(0xFF42A5F5), "Спасибо! Ещё нужен сыр", "10:46"),
    ChatMessage("m5", "me", "Я", "Я", Color(0xFF5B7C5A), "Окей, возьму всё с собой", "11:02"),
)

private const val MY_SENDER_ID = "me"

@Composable
fun ChatScreen(
    chatTitle: String = "Наша семья",
    memberCount: Int = 4,
    navigateBack: (() -> Unit)? = null,
) {
    var messages by remember { mutableStateOf(initialMessages) }
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceVariantCream),
    ) {
        // Header
        Surface(
            color = SurfaceWhite,
            shadowElevation = 1.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (navigateBack != null) {
                    Surface(
                        onClick = navigateBack,
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape,
                        color = SurfaceVariantCream,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("‹", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(PrimaryGreenLight, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("💬", fontSize = 18.sp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        chatTitle,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                    )
                    Text(
                        "$memberCount участника",
                        fontSize = 12.sp,
                        color = TextSecondary,
                    )
                }

                Surface(
                    onClick = {},
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    color = SurfaceVariantCream,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("⋮", fontSize = 18.sp, color = TextSecondary)
                    }
                }
            }
        }

        // Messages list
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 16.dp,
                vertical = 12.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(messages) { msg ->
                val isMe = msg.senderId == MY_SENDER_ID
                MessageBubble(msg = msg, isMe = isMe)
            }
        }

        // Input bar
        Surface(
            color = SurfaceWhite,
            shadowElevation = 4.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(22.dp),
                    color = SurfaceVariantCream,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, DividerColor),
                ) {
                    BasicTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            color = TextPrimary,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        decorationBox = { inner ->
                            if (inputText.isEmpty()) {
                                Text("Написать сообщение...", fontSize = 14.sp, color = TextSecondary)
                            }
                            inner()
                        },
                    )
                }

                val canSend = inputText.isNotBlank()
                Surface(
                    onClick = {
                        if (canSend) {
                            val newMsg = ChatMessage(
                                id = "m${System.currentTimeMillis()}",
                                senderId = MY_SENDER_ID,
                                senderName = "Я",
                                senderInitials = "Я",
                                senderColor = PrimaryGreen,
                                text = inputText.trim(),
                                time = "сейчас",
                            )
                            messages = messages + newMsg
                            inputText = ""
                        }
                    },
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = if (canSend) PrimaryGreen else DividerColor,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("↑", fontSize = 18.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(msg: ChatMessage, isMe: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        if (!isMe) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(msg.senderColor, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    msg.senderInitials.take(1),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
            Spacer(Modifier.size(8.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 280.dp),
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
        ) {
            if (!isMe) {
                Text(
                    msg.senderName,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    modifier = Modifier.padding(start = 4.dp, bottom = 3.dp),
                )
            }

            Surface(
                shape = RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = if (isMe) 18.dp else 4.dp,
                    bottomEnd = if (isMe) 4.dp else 18.dp,
                ),
                color = if (isMe) PrimaryGreen else SurfaceWhite,
                border = if (!isMe) androidx.compose.foundation.BorderStroke(1.dp, DividerColor) else null,
                shadowElevation = 1.dp,
            ) {
                Text(
                    msg.text,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    fontSize = 15.sp,
                    color = if (isMe) Color.White else TextPrimary,
                    lineHeight = 21.sp,
                )
            }

            Text(
                msg.time,
                fontSize = 10.sp,
                color = TextSecondary.copy(alpha = 0.7f),
                modifier = Modifier.padding(
                    top = 3.dp,
                    start = if (isMe) 0.dp else 4.dp,
                    end = if (isMe) 4.dp else 0.dp,
                ),
            )
        }

        if (isMe) {
            Spacer(Modifier.size(8.dp))
        }
    }
}
