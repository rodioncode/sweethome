package com.jetbrains.kmpapp.screens.settings

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetbrains.kmpapp.screens.profile.ProfileViewModel
import com.jetbrains.kmpapp.ui.ErrorRed
import com.jetbrains.kmpapp.ui.PrimaryGreen
import com.jetbrains.kmpapp.ui.SecondaryPeach
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
) {
    val profileViewModel = koinViewModel<ProfileViewModel>()
    var notifyPush by remember { mutableStateOf(true) }
    var notifyReminders by remember { mutableStateOf(true) }
    var notifyActivity by remember { mutableStateOf(true) }
    var darkTheme by remember { mutableStateOf(false) }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp),
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                }
                Text("Настройки", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            // Notifications section
            SettingsSection(title = "Уведомления") {
                SettingsToggleRow(
                    emoji = "🔔",
                    emojiBgColor = ErrorRed.copy(alpha = 0.15f),
                    title = "Push-уведомления",
                    subtitle = "Напоминания о задачах",
                    checked = notifyPush,
                    onCheckedChange = { notifyPush = it },
                )
                HorizontalDivider(modifier = Modifier.padding(start = 64.dp))
                SettingsToggleRow(
                    emoji = "📅",
                    emojiBgColor = SecondaryPeach.copy(alpha = 0.2f),
                    title = "Напоминания",
                    subtitle = "За 30 минут до дедлайна",
                    checked = notifyReminders,
                    onCheckedChange = { notifyReminders = it },
                )
                HorizontalDivider(modifier = Modifier.padding(start = 64.dp))
                SettingsToggleRow(
                    emoji = "👥",
                    emojiBgColor = PrimaryGreen.copy(alpha = 0.15f),
                    title = "Активность в пространстве",
                    subtitle = "Новые задачи от участников",
                    checked = notifyActivity,
                    onCheckedChange = { notifyActivity = it },
                )
            }

            // Appearance section
            SettingsSection(title = "Внешний вид") {
                SettingsToggleRow(
                    emoji = "🌙",
                    emojiBgColor = Color(0xFF6366F1).copy(alpha = 0.15f),
                    title = "Тёмная тема",
                    subtitle = if (darkTheme) "Сейчас: Тёмная" else "Сейчас: Светлая",
                    checked = darkTheme,
                    onCheckedChange = { darkTheme = it },
                )
                HorizontalDivider(modifier = Modifier.padding(start = 64.dp))
                SettingsNavRow(
                    emoji = "🌐",
                    emojiBgColor = Color(0xFF3B82F6).copy(alpha = 0.15f),
                    title = "Язык",
                    value = "Русский",
                    onClick = { },
                )
            }

            // Account section
            SettingsSection(title = "Аккаунт") {
                SettingsNavRow(
                    emoji = "✏️",
                    emojiBgColor = PrimaryGreen.copy(alpha = 0.15f),
                    title = "Редактировать профиль",
                    onClick = { },
                )
                HorizontalDivider(modifier = Modifier.padding(start = 64.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { profileViewModel.logout() }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape,
                        color = ErrorRed.copy(alpha = 0.15f),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = null,
                                tint = ErrorRed,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Выйти из аккаунта",
                        style = MaterialTheme.typography.bodyLarge,
                        color = ErrorRed,
                    )
                }
            }

            // Footer
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "SweetHome v1.0.0 · © 2025",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsToggleRow(
    emoji: String,
    emojiBgColor: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(36.dp),
            shape = CircleShape,
            color = emojiBgColor,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = emoji, fontSize = 16.sp)
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedTrackColor = PrimaryGreen),
        )
    }
}

@Composable
private fun SettingsNavRow(
    emoji: String,
    emojiBgColor: Color,
    title: String,
    value: String? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(36.dp),
            shape = CircleShape,
            color = emojiBgColor,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = emoji, fontSize = 16.sp)
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
        )
        if (value != null) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(4.dp))
        }
        Text(
            text = "›",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
