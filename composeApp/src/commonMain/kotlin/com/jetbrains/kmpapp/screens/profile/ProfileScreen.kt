package com.jetbrains.kmpapp.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jetbrains.kmpapp.ui.SweetHomeSpacing
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProfileContent(
    navigateToLinkEmail: () -> Unit,
    navigateToSettings: () -> Unit = {},
    navigateToTemplates: () -> Unit = {},
) {
    val viewModel = koinViewModel<ProfileViewModel>()
    val isGuest by viewModel.isGuest.collectAsStateWithLifecycle()
    val userId by viewModel.userId.collectAsStateWithLifecycle()
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val listCount by viewModel.listCount.collectAsStateWithLifecycle()
    val groupCount by viewModel.groupCount.collectAsStateWithLifecycle()
    val groups by viewModel.groups.collectAsStateWithLifecycle()
    val deleteState by viewModel.deleteState.collectAsStateWithLifecycle()
    val telegramStatus by viewModel.telegramStatus.collectAsStateWithLifecycle()
    val telegramLinkState by viewModel.telegramLinkState.collectAsStateWithLifecycle()
    var showDeleteDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    Scaffold { paddingValues ->
    LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

        // --- Green hero header ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary),
            ) {
                // Decorative circle
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .align(Alignment.TopEnd)
                        .padding(top = 0.dp)
                        .background(Color.White.copy(alpha = 0.06f), CircleShape),
                )

                Column(modifier = Modifier.fillMaxWidth().padding(20.dp).padding(top = 10.dp)) {
                    // Top row: title + logout
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "Профиль",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                        Surface(
                            onClick = { viewModel.logout() },
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.15f),
                        ) {
                            Text(
                                "Выйти",
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Avatar row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.25f))
                                .border(3.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            val displayName = profile?.displayName ?: userId ?: ""
                            val initial = displayName.firstOrNull()?.uppercase() ?: "?"
                            Text(
                                initial,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                        Column {
                            Text(
                                if (isGuest) "Гостевой аккаунт" else (profile?.displayName ?: userId ?: "Пользователь"),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                            if (!isGuest && profile?.email != null) {
                                Text(
                                    profile!!.email!!.take(30),
                                    fontSize = 13.sp,
                                    color = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(top = 3.dp),
                                )
                            }
                            if (isGuest) {
                                Surface(
                                    onClick = navigateToLinkEmail,
                                    modifier = Modifier.padding(top = 8.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White.copy(alpha = 0.15f),
                                ) {
                                    Text(
                                        "Привязать email",
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                    )
                                }
                            } else {
                                Surface(
                                    onClick = navigateToSettings,
                                    modifier = Modifier.padding(top = 8.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White.copy(alpha = 0.15f),
                                ) {
                                    Text(
                                        "Редактировать",
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }

        // --- Stats row ---
        item {
            Surface(color = MaterialTheme.colorScheme.surface) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .border(width = 1.dp, color = MaterialTheme.colorScheme.outline),
                ) {
                    listOf(
                        "0" to "задач",
                        listCount.toString() to "списков",
                        groupCount.toString() to "пространств",
                    ).forEachIndexed { index, (value, label) ->
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 16.dp)
                                .then(
                                    if (index < 2) Modifier.border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outline,
                                    ) else Modifier
                                ),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp))
                        }
                    }
                }
            }
        }

        // --- Settings section ---
        item {
            Spacer(Modifier.height(SweetHomeSpacing.lg))
            SectionLabel("НАСТРОЙКИ")
        }
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SweetHomeSpacing.md),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                Column {
                    data class SettingsItem(val icon: String, val label: String, val onClick: () -> Unit)
                    val settingsItems = listOf(
                        SettingsItem("📋", "Шаблоны", navigateToTemplates),
                        SettingsItem("🔔", "Уведомления", navigateToSettings),
                        SettingsItem("📅", "Интеграция с календарём", navigateToSettings),
                        SettingsItem("🌙", "Тема", navigateToSettings),
                        SettingsItem("🌐", "Язык", navigateToSettings),
                    )
                    settingsItems.forEachIndexed { index, entry ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { entry.onClick() }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(entry.icon, fontSize = 18.sp)
                            Text(entry.label, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                            Text("›", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (index < settingsItems.lastIndex) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }
        }

        // --- Telegram ---
        item {
            Spacer(Modifier.height(SweetHomeSpacing.lg))
            SectionLabel("TELEGRAM")
        }
        item {
            TelegramSection(
                status = telegramStatus,
                onLink = { viewModel.startTelegramLink() },
                onUnlink = { viewModel.unlinkTelegram() },
            )
        }

        // --- My spaces ---
        item {
            Spacer(Modifier.height(SweetHomeSpacing.lg))
            SectionLabel("МОИ ПРОСТРАНСТВА")
        }
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SweetHomeSpacing.md),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                Column {
                    // Personal space
                    SpaceRow(icon = "👤", title = "Личное", subtitle = "Только вы", onClick = {})
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(horizontal = 16.dp))

                    // Group spaces
                    groups.forEachIndexed { index, group ->
                        SpaceRow(
                            icon = when (group.type) {
                                "family" -> "👨‍👩‍👧‍👦"
                                "mentoring" -> "🎓"
                                else -> "👥"
                            },
                            title = group.title,
                            subtitle = when (group.role) {
                                "owner" -> "owner"
                                else -> "member"
                            },
                            onClick = {},
                        )
                        if (index < groups.lastIndex) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }
        }

        // --- Activity ---
        item {
            Spacer(Modifier.height(SweetHomeSpacing.lg))
            SectionLabel("АКТИВНОСТЬ")
        }
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SweetHomeSpacing.md),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                            .padding(top = 6.dp),
                    )
                    Text("Нет недавней активности", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        item {
            Spacer(Modifier.height(SweetHomeSpacing.xl))
            DangerZone(
                onDelete = { showDeleteDialog = true },
            )
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
    }

    if (showDeleteDialog) {
        ConfirmDeleteAccountDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                viewModel.deleteAccount()
            },
        )
    }
    when (val s = deleteState) {
        is ProfileViewModel.DeleteState.Error -> {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { viewModel.resetDeleteState() },
                confirmButton = { androidx.compose.material3.TextButton(onClick = { viewModel.resetDeleteState() }) { Text("OK") } },
                title = { Text("Не удалось") },
                text = { Text(s.message) },
            )
        }
        else -> Unit
    }

    when (val ls = telegramLinkState) {
        is ProfileViewModel.TelegramLinkState.Pending -> {
            TelegramLinkSheet(
                code = ls.code,
                expiresAtIso = ls.expiresAt,
                deeplink = ls.deeplink,
                onCheckNow = { viewModel.checkLinkNow() },
                onRetry = { viewModel.startTelegramLink() },
                onDismiss = { viewModel.cancelTelegramLink() },
            )
        }
        is ProfileViewModel.TelegramLinkState.Error -> {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { viewModel.cancelTelegramLink() },
                confirmButton = { androidx.compose.material3.TextButton(onClick = { viewModel.cancelTelegramLink() }) { Text("OK") } },
                title = { Text("Не удалось") },
                text = { Text(ls.message) },
            )
        }
        else -> Unit
    }
}

@Composable
private fun DangerZone(onDelete: () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = SweetHomeSpacing.md)) {
        SectionLabel("ОПАСНАЯ ЗОНА")
        Surface(
            onClick = onDelete,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("⚠️", fontSize = 18.sp)
                Column(modifier = Modifier.weight(1f)) {
                    Text("Удалить аккаунт", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    Text("Все данные будут удалены без возможности восстановления", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun ConfirmDeleteAccountDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    var input by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    val canConfirm = input.trim().equals("УДАЛИТЬ", ignoreCase = false)
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Удалить аккаунт?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Это действие необратимо. Все списки, задачи, история и баланс будут стёрты.")
                Text("Чтобы подтвердить, введите УДАЛИТЬ:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                androidx.compose.material3.OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onConfirm, enabled = canConfirm) {
                Text("Удалить", color = if (canConfirm) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline)
            }
        },
        dismissButton = { androidx.compose.material3.TextButton(onClick = onDismiss) { Text("Отмена") } },
    )
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(horizontal = SweetHomeSpacing.md, vertical = 0.dp).padding(bottom = 10.dp),
    )
}

@Composable
private fun SpaceRow(
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(icon, fontSize = 18.sp)
        Text(title, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text("$subtitle ›", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun TelegramSection(
    status: com.jetbrains.kmpapp.data.telegram.TelegramLinkStatusResponse?,
    onLink: () -> Unit,
    onUnlink: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SweetHomeSpacing.md),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        if (status?.linked == true) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("🤖", fontSize = 22.sp)
                    Column(modifier = Modifier.weight(1f)) {
                        val name = status.telegramUsername?.let { "@$it" } ?: "Привязан"
                        Text(name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        val linkedAt = formatLinkedAt(status.linkedAt)
                        if (linkedAt != null) {
                            Text("привязан $linkedAt", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                Surface(
                    onClick = onUnlink,
                    modifier = Modifier.align(Alignment.End),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                ) {
                    Text(
                        "Отвязать",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("🤖", fontSize = 22.sp)
                Column(modifier = Modifier.weight(1f)) {
                    Text("Telegram", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("Добавляйте задачи прямо из бота", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(
                    onClick = onLink,
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primary,
                ) {
                    Text(
                        "Привязать",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun TelegramLinkSheet(
    code: String,
    expiresAtIso: String,
    deeplink: String,
    onCheckNow: () -> Unit,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val uriHandler = LocalUriHandler.current
    val expiresAt = remember(expiresAtIso) { parseInstant(expiresAtIso) }
    val secondsLeft by produceState(initialValue = computeSecondsLeft(expiresAt), key1 = expiresAt) {
        while (true) {
            value = computeSecondsLeft(expiresAt)
            if (value <= 0L) break
            delay(1_000)
        }
    }
    val expired = secondsLeft <= 0L

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "Привязка Telegram",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                formatCode(code),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 6.sp,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                if (expired) "Срок кода истёк" else "Действителен ещё ${formatTimer(secondsLeft)}",
                fontSize = 13.sp,
                color = if (expired) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                "Откройте бота — он сам подставит код. После этого вернитесь сюда.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Surface(
                onClick = {
                    if (expired) onRetry() else uriHandler.openUri(deeplink)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary,
            ) {
                Text(
                    if (expired) "Получить новый код" else "Открыть бота",
                    modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center,
                )
            }
            if (!expired) {
                Surface(
                    onClick = onCheckNow,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                ) {
                    Text(
                        "Я уже привязал, проверить",
                        modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

private fun parseInstant(iso: String): Instant? = try {
    Instant.parse(iso)
} catch (_: Throwable) {
    null
}

private fun computeSecondsLeft(expiresAt: Instant?): Long {
    if (expiresAt == null) return 0L
    val left = (expiresAt - Clock.System.now()).inWholeSeconds
    return if (left < 0L) 0L else left
}

private fun formatTimer(secondsLeft: Long): String {
    val minutes = secondsLeft / 60
    val seconds = secondsLeft % 60
    val mm = if (minutes < 10) "0$minutes" else minutes.toString()
    val ss = if (seconds < 10) "0$seconds" else seconds.toString()
    return "$mm:$ss"
}

private fun formatCode(code: String): String =
    if (code.length == 6) "${code.substring(0, 3)} ${code.substring(3)}" else code

private fun formatLinkedAt(iso: String?): String? {
    val instant = iso?.let { parseInstant(it) } ?: return null
    val dt = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
    val day = if (dt.dayOfMonth < 10) "0${dt.dayOfMonth}" else dt.dayOfMonth.toString()
    val month = dt.monthNumber.let { if (it < 10) "0$it" else it.toString() }
    return "$day.$month.${dt.year}"
}
