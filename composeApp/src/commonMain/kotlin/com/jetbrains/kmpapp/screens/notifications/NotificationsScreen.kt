package com.jetbrains.kmpapp.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetbrains.kmpapp.ui.DividerColor
import com.jetbrains.kmpapp.ui.PrimaryGreen
import com.jetbrains.kmpapp.ui.PrimaryGreenLight
import com.jetbrains.kmpapp.ui.SurfaceVariantCream
import com.jetbrains.kmpapp.ui.SurfaceWhite
import com.jetbrains.kmpapp.ui.TextPrimary
import com.jetbrains.kmpapp.ui.TextSecondary

private data class AppNotification(
    val id: String,
    val type: String,
    val text: String,
    val sub: String?,
    val timeAgo: String,
    var isRead: Boolean,
)

private val sampleNotifications = listOf(
    AppNotification("n1", "item_done", "Аня выполнила «Купить молоко»", "Список покупок", "5 мин назад", false),
    AppNotification("n2", "assigned", "Дима назначил тебе задачу «Убрать кухню»", null, "32 мин назад", false),
    AppNotification("n3", "joined", "Соня присоединилась к вашей группе", "Наша семья", "2 ч назад", true),
    AppNotification("n4", "due", "Дедлайн через 1 час: «Оплата аренды»", "Задачи", "3 ч назад", true),
    AppNotification("n5", "item_added", "Гриша добавил 3 задачи в «Список покупок»", null, "Вчера", true),
)

private fun typeIcon(type: String) = when (type) {
    "item_done" -> "✅"
    "assigned" -> "👤"
    "joined" -> "👥"
    "due" -> "⏰"
    "item_added" -> "➕"
    else -> "🔔"
}

@Composable
fun NotificationsScreen() {
    var notifications by remember { mutableStateOf(sampleNotifications) }
    val unreadCount = notifications.count { !it.isRead }

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
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Уведомления",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
                if (unreadCount > 0) {
                    TextButton(
                        onClick = {
                            notifications = notifications.map { it.copy(isRead = true) }
                        }
                    ) {
                        Text(
                            "Прочитать все",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen,
                        )
                    }
                }
            }
        }

        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🔔", fontSize = 56.sp)
                    Spacer(Modifier.height(16.dp))
                    Text("Тихо и спокойно", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(Modifier.height(8.dp))
                    Text("Здесь появятся обновления", fontSize = 14.sp, color = TextSecondary, textAlign = TextAlign.Center)
                }
            }
        } else {
            val today = notifications.take(2)
            val earlier = notifications.drop(2)

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            ) {
                if (today.isNotEmpty()) {
                    item {
                        Text(
                            "СЕГОДНЯ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(bottom = 10.dp),
                        )
                    }
                    items(today.size) { i ->
                        NotifRow(
                            notif = today[i],
                            onRead = {
                                val id = today[i].id
                                notifications = notifications.map { if (it.id == id) it.copy(isRead = true) else it }
                            },
                        )
                    }
                }
                if (earlier.isNotEmpty()) {
                    item {
                        Text(
                            "РАНЕЕ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(top = 16.dp, bottom = 10.dp),
                        )
                    }
                    items(earlier.size) { i ->
                        NotifRow(
                            notif = earlier[i],
                            onRead = {
                                val id = earlier[i].id
                                notifications = notifications.map { if (it.id == id) it.copy(isRead = true) else it }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotifRow(notif: AppNotification, onRead: () -> Unit) {
    Box(modifier = Modifier.padding(bottom = 8.dp)) {
        Surface(
            onClick = onRead,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = SurfaceWhite,
            border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            if (notif.isRead) SurfaceVariantCream else PrimaryGreenLight,
                            CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(typeIcon(notif.type), fontSize = 18.sp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        notif.text,
                        fontSize = 14.sp,
                        fontWeight = if (notif.isRead) FontWeight.Normal else FontWeight.SemiBold,
                        color = TextPrimary,
                        lineHeight = 20.sp,
                    )
                    notif.sub?.let { sub ->
                        Text(
                            sub,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                    Text(
                        notif.timeAgo,
                        fontSize = 11.sp,
                        color = TextSecondary.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }

        if (!notif.isRead) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(PrimaryGreen, CircleShape)
                    .align(Alignment.TopStart)
                    .offset(x = (-4).dp, y = 14.dp),
            )
        }
    }
}
