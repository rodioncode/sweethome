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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import com.jetbrains.kmpapp.ui.SweetHomeShapes
import com.jetbrains.kmpapp.ui.SweetHomeSpacing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetbrains.kmpapp.screens.profile.ProfileViewModel
import com.jetbrains.kmpapp.ui.SecondaryPeach
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
) {
    val profileViewModel = koinViewModel<ProfileViewModel>()
    val notifVm = koinViewModel<NotificationPrefsViewModel>()
    val prefs by notifVm.prefs.collectAsStateWithLifecycle()
    val notifyPush = prefs.firstOrNull { it.channel == "push" }?.enabled ?: true
    val notifyEmail = prefs.firstOrNull { it.channel == "email" }?.enabled ?: true
    var darkTheme by remember { mutableStateOf(false) }
    var showWorkHoursSheet by remember { mutableStateOf(false) }
    // Work hours state — defaults to Mon-Fri 09:00-18:00. Persisted by repository in real impl.
    var workDays by remember { mutableStateOf(setOf("Пн", "Вт", "Ср", "Чт", "Пт")) }
    var workStart by remember { mutableStateOf("09:00") }
    var workEnd by remember { mutableStateOf("18:00") }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                    emojiBgColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                    title = "Push-уведомления",
                    subtitle = "Напоминания и активность",
                    checked = notifyPush,
                    onCheckedChange = { notifVm.toggle("push", it) },
                )
                HorizontalDivider(modifier = Modifier.padding(start = 64.dp))
                SettingsToggleRow(
                    emoji = "✉️",
                    emojiBgColor = SecondaryPeach.copy(alpha = 0.2f),
                    title = "Email-уведомления",
                    subtitle = "Сводки и важные события",
                    checked = notifyEmail,
                    onCheckedChange = { notifVm.toggle("email", it) },
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
                    emojiBgColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    title = "Язык",
                    value = "Русский",
                    onClick = { },
                )
                HorizontalDivider(modifier = Modifier.padding(start = 64.dp))
                SettingsNavRow(
                    emoji = "💼",
                    emojiBgColor = MaterialTheme.colorScheme.secondaryContainer,
                    title = "Рабочие часы",
                    value = workHoursSubtitle(workDays, workStart, workEnd),
                    onClick = { showWorkHoursSheet = true },
                )
            }

            // Account section
            SettingsSection(title = "Аккаунт") {
                SettingsNavRow(
                    emoji = "✏️",
                    emojiBgColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
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
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Выйти из аккаунта",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
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

    if (showWorkHoursSheet) {
        WorkHoursSheet(
            initialDays = workDays,
            initialStart = workStart,
            initialEnd = workEnd,
            onSave = { d, s, e ->
                workDays = d
                workStart = s
                workEnd = e
                showWorkHoursSheet = false
            },
            onDismiss = { showWorkHoursSheet = false },
        )
    }
}

private fun workHoursSubtitle(days: Set<String>, start: String, end: String): String {
    val daysShort = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс").filter { it in days }
    return if (daysShort.isEmpty()) "Не задано" else "${daysShort.joinToString(" ")} · $start–$end"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkHoursSheet(
    initialDays: Set<String>,
    initialStart: String,
    initialEnd: String,
    onSave: (Set<String>, String, String) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var days by remember { mutableStateOf(initialDays) }
    var start by remember { mutableStateOf(initialStart) }
    var end by remember { mutableStateOf(initialEnd) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = SweetHomeShapes.BottomSheet,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = SweetHomeSpacing.bottomSheetPaddingH,
                vertical = SweetHomeSpacing.xl,
            ),
            verticalArrangement = Arrangement.spacedBy(SweetHomeSpacing.xl),
        ) {
            Text(
                "Рабочие часы",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                "В это время Dashboard будет показывать рабочий контекст.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                "ДНИ НЕДЕЛИ",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(SweetHomeSpacing.xs),
                modifier = Modifier.fillMaxWidth(),
            ) {
                listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс").forEach { d ->
                    val selected = d in days
                    FilterChip(
                        selected = selected,
                        onClick = {
                            days = if (selected) days - d else days + d
                        },
                        label = { Text(d, fontSize = 13.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Text(
                "ВРЕМЯ",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(SweetHomeSpacing.sm)) {
                TimeSelectorButton(
                    label = "Начало",
                    value = start,
                    onClick = { /* TODO: open M3 TimePicker */ },
                    modifier = Modifier.weight(1f),
                )
                TimeSelectorButton(
                    label = "Конец",
                    value = end,
                    onClick = { /* TODO: open M3 TimePicker */ },
                    modifier = Modifier.weight(1f),
                )
            }

            // Preview
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = SweetHomeShapes.Card,
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Row(
                    modifier = Modifier.padding(SweetHomeSpacing.xl),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(SweetHomeSpacing.md),
                ) {
                    Icon(
                        Icons.Outlined.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Column {
                        Text(
                            "Предпросмотр",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            workHoursSubtitle(days, start, end),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(SweetHomeSpacing.sm)) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = SweetHomeShapes.Button,
                ) { Text("Отмена") }
                Button(
                    onClick = { onSave(days, start, end) },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = SweetHomeShapes.Button,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) { Text("Сохранить") }
            }

            Spacer(Modifier.height(SweetHomeSpacing.xl))
        }
    }
}

@Composable
private fun TimeSelectorButton(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = SweetHomeShapes.Button,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
            )
            Text(
                value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
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
            colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary),
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
