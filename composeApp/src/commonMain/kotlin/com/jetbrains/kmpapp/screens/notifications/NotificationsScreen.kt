package com.jetbrains.kmpapp.screens.notifications

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jetbrains.kmpapp.data.notifications.Notification
import com.jetbrains.kmpapp.ui.LocalCozyExtraColors
import com.jetbrains.kmpapp.ui.components.CozyCard
import com.jetbrains.kmpapp.ui.components.CozyTopBar
import com.jetbrains.kmpapp.ui.components.EmptyHero
import org.koin.compose.viewmodel.koinViewModel

private data class IconStyle(val icon: String, val bg: Color, val fg: Color)

@Composable
private fun iconForType(type: String): IconStyle {
    val extras = LocalCozyExtraColors.current
    val primary = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    return when (type) {
        "item_done", "task_completed" -> IconStyle("✓", primaryContainer, primary)
        "assigned", "task_assigned" -> IconStyle("👤", extras.lavenderSoft, extras.lavender)
        "joined", "member_joined" -> IconStyle("🎉", primaryContainer, primary)
        "due", "task_due" -> IconStyle("📅", extras.coralSoft, extras.coral)
        "item_added", "task_created" -> IconStyle("➕", extras.lavenderSoft, extras.lavender)
        "streak" -> IconStyle("🔥", extras.ochreSoft, extras.ochre)
        else -> IconStyle("🔔", primaryContainer, primary)
    }
}

@Composable
fun NotificationsScreen() {
    val viewModel = koinViewModel<NotificationsViewModel>()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val unreadCount = notifications.count { !it.isRead }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            CozyTopBar(
                title = "Уведомления",
                action = {
                    if (unreadCount > 0) {
                        Text(
                            text = "Отметить всё",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable { viewModel.markAllRead() }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                        )
                    }
                },
            )

            if (notifications.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    EmptyHero(emoji = "🔔", decor = listOf("✨", "🌿", "☁️", "🍃"))
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Тихо и спокойно",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Здесь появятся обновления",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                val today = notifications.take(2)
                val earlier = notifications.drop(2)

                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (today.isNotEmpty()) {
                        item { SectionLabel(label = "СЕГОДНЯ") }
                        items(today.size) { i ->
                            NotifRow(
                                notif = today[i],
                                onRead = { viewModel.markRead(today[i].id) },
                            )
                        }
                    }
                    if (earlier.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(8.dp))
                            SectionLabel(label = "РАНЕЕ")
                        }
                        items(earlier.size) { i ->
                            NotifRow(
                                notif = earlier[i],
                                onRead = { viewModel.markRead(earlier[i].id) },
                            )
                        }
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(label: String) {
    Text(
        text = label,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 1.2.sp,
        modifier = Modifier.padding(bottom = 8.dp),
    )
}

@Composable
private fun NotifRow(notif: Notification, onRead: () -> Unit) {
    val style = iconForType(notif.type)
    CozyCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onRead,
        bordered = !notif.isRead,
        background = if (notif.isRead) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.surface,
        contentPadding = 12.dp,
        radius = 16.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(style.bg, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(style.icon, fontSize = 16.sp, color = style.fg)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    notif.title,
                    fontSize = 13.sp,
                    fontWeight = if (notif.isRead) FontWeight.Normal else FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    lineHeight = 17.sp,
                )
                if (notif.body.isNotBlank()) {
                    Text(
                        notif.body,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 17.sp,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                Text(
                    notif.createdAt,
                    fontSize = 11.sp,
                    color = LocalCozyExtraColors.current.textTer,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            if (!notif.isRead) {
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .size(8.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                )
            }
        }
    }
}
