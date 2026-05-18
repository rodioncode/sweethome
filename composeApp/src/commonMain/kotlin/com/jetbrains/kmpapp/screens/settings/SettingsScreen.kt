package com.jetbrains.kmpapp.screens.settings

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jetbrains.kmpapp.screens.profile.ProfileViewModel
import com.jetbrains.kmpapp.ui.LocalCozyShapes
import com.jetbrains.kmpapp.ui.LocalCozySpacing
import com.jetbrains.kmpapp.ui.components.CozyCard
import com.jetbrains.kmpapp.ui.components.CozyChip
import com.jetbrains.kmpapp.ui.components.CozyTopBar
import com.jetbrains.kmpapp.ui.components.MetaRow
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
    val notifyTelegram = prefs.firstOrNull { it.channel == "telegram" }?.enabled ?: false

    var darkTheme by remember { mutableStateOf(false) }
    var showWorkHoursSheet by remember { mutableStateOf(false) }
    var workDays by remember { mutableStateOf(setOf("Пн", "Вт", "Ср", "Чт", "Пт")) }
    var workStart by remember { mutableStateOf("09:00") }
    var workEnd by remember { mutableStateOf("18:00") }

    val spacing = LocalCozySpacing.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues),
            contentPadding = PaddingValues(
                start = spacing.xxl,
                end = spacing.xxl,
                top = 0.dp,
                bottom = 80.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(spacing.xl),
        ) {
            item {
                CozyTopBar(title = "Настройки", onBack = onNavigateBack)
            }

            item {
                SettingsSection(title = "ВНЕШНИЙ ВИД") {
                    MetaRow(
                        icon = "🎨",
                        title = "Тёмная тема",
                        value = if (darkTheme) "Тёмная" else "Светлая",
                        valueAdornment = {
                            CozySwitch(checked = darkTheme, onCheckedChange = { darkTheme = it })
                        },
                    )
                    Divider()
                    MetaRow(
                        icon = "🌐",
                        title = "Язык",
                        value = "Русский",
                        onClick = { },
                    )
                    Divider()
                    MetaRow(
                        icon = "⏱",
                        title = "Рабочие часы",
                        value = workHoursSubtitle(workDays, workStart, workEnd),
                        onClick = { showWorkHoursSheet = true },
                    )
                }
            }

            item {
                SettingsSection(title = "УВЕДОМЛЕНИЯ") {
                    MetaRow(
                        icon = "🔔",
                        title = "Push-уведомления",
                        valueAdornment = {
                            CozySwitch(
                                checked = notifyPush,
                                onCheckedChange = { notifVm.toggle("push", it) },
                            )
                        },
                    )
                    Divider()
                    MetaRow(
                        icon = "📧",
                        title = "Email-уведомления",
                        valueAdornment = {
                            CozySwitch(
                                checked = notifyEmail,
                                onCheckedChange = { notifVm.toggle("email", it) },
                            )
                        },
                    )
                    Divider()
                    MetaRow(
                        icon = "💬",
                        title = "Telegram-уведомления",
                        valueAdornment = {
                            CozySwitch(
                                checked = notifyTelegram,
                                onCheckedChange = { notifVm.toggle("telegram", it) },
                            )
                        },
                    )
                }
            }

            item {
                SettingsSection(title = "ПРИВАТНОСТЬ") {
                    MetaRow(
                        icon = "🔒",
                        title = "Конфиденциальность",
                        onClick = { },
                    )
                    Divider()
                    MetaRow(
                        icon = "📊",
                        title = "Аналитика",
                        value = "Включена",
                        onClick = { },
                    )
                }
            }

            item {
                SettingsSection(title = "АККАУНТ") {
                    MetaRow(
                        icon = "✏️",
                        title = "Редактировать профиль",
                        onClick = { },
                    )
                    Divider()
                    MetaRow(
                        icon = "🚪",
                        title = "Выйти из аккаунта",
                        danger = true,
                        onClick = { profileViewModel.logout() },
                    )
                }
            }

            item {
                SettingsSection(title = "О ПРИЛОЖЕНИИ") {
                    MetaRow(icon = "ℹ️", title = "Версия", value = "1.0.0")
                    Divider()
                    MetaRow(icon = "📜", title = "Условия использования", onClick = { })
                    Divider()
                    MetaRow(icon = "🛡", title = "Политика конфиденциальности", onClick = { })
                }
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "SweetHome v1.0.0 · © 2026",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
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

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = LocalCozySpacing.current.xs),
        )
        CozyCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = 0.dp,
            radius = 18.dp,
        ) {
            Column { content() }
        }
    }
}

@Composable
private fun Divider() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant,
        modifier = Modifier.padding(horizontal = 16.dp),
    )
}

@Composable
private fun CozySwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedTrackColor = MaterialTheme.colorScheme.primary,
            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
            uncheckedTrackColor = MaterialTheme.colorScheme.outline,
            uncheckedThumbColor = MaterialTheme.colorScheme.onPrimary,
            uncheckedBorderColor = MaterialTheme.colorScheme.outline,
        ),
    )
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
    val start by remember { mutableStateOf(initialStart) }
    val end by remember { mutableStateOf(initialEnd) }
    val spacing = LocalCozySpacing.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = LocalCozyShapes.current.sheet,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = spacing.xxl,
                vertical = spacing.lg,
            ),
            verticalArrangement = Arrangement.spacedBy(spacing.lg),
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
                letterSpacing = 1.2.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                modifier = Modifier.fillMaxWidth(),
            ) {
                listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс").forEach { d ->
                    val selected = d in days
                    CozyChip(
                        label = d,
                        selected = selected,
                        accent = MaterialTheme.colorScheme.primary,
                        accentContainer = MaterialTheme.colorScheme.primaryContainer,
                        onClick = {
                            days = if (selected) days - d else days + d
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Text(
                "ВРЕМЯ",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                TimeTile(label = "Начало", value = start, modifier = Modifier.weight(1f))
                TimeTile(label = "Конец", value = end, modifier = Modifier.weight(1f))
            }

            CozyCard(
                modifier = Modifier.fillMaxWidth(),
                background = MaterialTheme.colorScheme.surfaceVariant,
                radius = 18.dp,
                contentPadding = spacing.lg,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    Text("⏰", fontSize = 20.sp)
                    Column(modifier = Modifier.weight(1f)) {
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

            Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                SheetButton(
                    label = "Отмена",
                    onClick = onDismiss,
                    primary = false,
                    modifier = Modifier.weight(1f),
                )
                SheetButton(
                    label = "Сохранить",
                    onClick = { onSave(days, start, end) },
                    primary = true,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(spacing.lg))
        }
    }
}

@Composable
private fun TimeTile(label: String, value: String, modifier: Modifier = Modifier) {
    CozyCard(
        modifier = modifier,
        bordered = true,
        background = MaterialTheme.colorScheme.surface,
        radius = 14.dp,
        contentPadding = 14.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
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
private fun SheetButton(
    label: String,
    onClick: () -> Unit,
    primary: Boolean,
    modifier: Modifier = Modifier,
) {
    val bg = if (primary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val fg = if (primary) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(LocalCozyShapes.current.button)
            .background(bg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = fg)
    }
}
