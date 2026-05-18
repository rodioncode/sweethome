package com.jetbrains.kmpapp.screens.profile

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.jetbrains.kmpapp.ui.LocalCozyExtraColors
import com.jetbrains.kmpapp.ui.LocalCozyShapes
import com.jetbrains.kmpapp.ui.LocalCozySpacing
import com.jetbrains.kmpapp.ui.components.CozyAvatar
import com.jetbrains.kmpapp.ui.components.CozyCard
import com.jetbrains.kmpapp.ui.components.CozyTopBar
import com.jetbrains.kmpapp.ui.components.MetaRow
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
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val listCount by viewModel.listCount.collectAsStateWithLifecycle()
    val groups by viewModel.groups.collectAsStateWithLifecycle()
    val deleteState by viewModel.deleteState.collectAsStateWithLifecycle()
    val telegramStatus by viewModel.telegramStatus.collectAsStateWithLifecycle()
    val telegramLinkState by viewModel.telegramLinkState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

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
                top = spacing.sm,
                start = spacing.xxl,
                end = spacing.xxl,
                bottom = 80.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(spacing.xl),
        ) {
            item {
                CozyTopBar(
                    title = "Профиль",
                    action = {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable(onClick = navigateToSettings),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("⚙", fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
                        }
                    },
                )
            }

            item {
                ProfileHero(
                    isGuest = isGuest,
                    displayName = profile?.displayName?.takeIf { it.isNotBlank() },
                    email = profile?.email,
                    listCount = listCount,
                    groupsCount = groups.size,
                    onLinkEmail = navigateToLinkEmail,
                )
            }

            item {
                AccountSection(
                    isGuest = isGuest,
                    email = profile?.email,
                    telegramStatus = telegramStatus,
                    onEditProfile = navigateToSettings,
                    onLinkEmail = navigateToLinkEmail,
                    onLinkTelegram = { viewModel.startTelegramLink() },
                    onUnlinkTelegram = { viewModel.unlinkTelegram() },
                )
            }

            item {
                AppearanceShortcuts(
                    onTemplates = navigateToTemplates,
                    onSettings = navigateToSettings,
                )
            }

            item {
                SpacesSection(
                    groups = groups.map { SpaceEntry(it.title, it.type, it.role) },
                )
            }

            item {
                DangerSection(
                    onLogout = { viewModel.logout() },
                    onDelete = { showDeleteDialog = true },
                )
            }
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
            AlertDialog(
                onDismissRequest = { viewModel.resetDeleteState() },
                confirmButton = {
                    TextButton(onClick = { viewModel.resetDeleteState() }) { Text("OK") }
                },
                title = { Text("Не удалось") },
                text = { Text(s.message) },
            )
        }
        ProfileViewModel.DeleteState.InProgress -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(spacing.lg),
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.error)
                    Text(
                        "Удаляем ваш аккаунт…",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                    )
                }
            }
        }
        ProfileViewModel.DeleteState.Done -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(spacing.lg),
                    modifier = Modifier.padding(spacing.xxl),
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "✓",
                            fontSize = 32.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Text(
                        "Аккаунт удалён",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        "Спасибо за время с SweetHome 🌿",
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
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
            AlertDialog(
                onDismissRequest = { viewModel.cancelTelegramLink() },
                confirmButton = {
                    TextButton(onClick = { viewModel.cancelTelegramLink() }) { Text("OK") }
                },
                title = { Text("Не удалось") },
                text = { Text(ls.message) },
            )
        }
        else -> Unit
    }
}

private data class SpaceEntry(val title: String, val type: String, val role: String)

@Composable
private fun ProfileHero(
    isGuest: Boolean,
    displayName: String?,
    email: String?,
    listCount: Int,
    groupsCount: Int,
    onLinkEmail: () -> Unit,
) {
    val spacing = LocalCozySpacing.current
    CozyCard(
        modifier = Modifier.fillMaxWidth(),
        radius = 24.dp,
        background = MaterialTheme.colorScheme.primaryContainer,
        contentPadding = spacing.xl,
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.md),
            ) {
                val letter = (displayName ?: if (isGuest) "Г" else "·").firstOrNull()?.uppercase() ?: "·"
                CozyAvatar(
                    letter = letter,
                    color = MaterialTheme.colorScheme.primary,
                    size = 64.dp,
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isGuest) "Гостевой аккаунт"
                        else (displayName ?: "Пользователь"),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    val subtitle = when {
                        isGuest -> "Привяжи email — сохрани прогресс"
                        !email.isNullOrBlank() -> email
                        else -> "Семья и порядок"
                    }
                    Text(
                        text = subtitle,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = spacing.xxs / 2),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (isGuest) {
                        Spacer(Modifier.height(spacing.xs))
                        Box(
                            modifier = Modifier
                                .clip(LocalCozyShapes.current.chip)
                                .background(MaterialTheme.colorScheme.primary)
                                .clickable(onClick = onLinkEmail)
                                .padding(horizontal = spacing.sm, vertical = spacing.xxs),
                        ) {
                            Text(
                                "Привязать email",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(spacing.lg))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                HeroStat(value = listCount.toString(), label = "списков", modifier = Modifier.weight(1f))
                HeroStat(value = groupsCount.toString(), label = "пространств", modifier = Modifier.weight(1f))
                HeroStat(value = "0", label = "наклеек", modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun HeroStat(value: String, label: String, modifier: Modifier = Modifier) {
    CozyCard(
        modifier = modifier,
        radius = 14.dp,
        contentPadding = 10.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(
                label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = LocalCozySpacing.current.xs, start = 4.dp),
    )
}

@Composable
private fun AccountSection(
    isGuest: Boolean,
    email: String?,
    telegramStatus: com.jetbrains.kmpapp.data.telegram.TelegramLinkStatusResponse?,
    onEditProfile: () -> Unit,
    onLinkEmail: () -> Unit,
    onLinkTelegram: () -> Unit,
    onUnlinkTelegram: () -> Unit,
) {
    Column {
        SectionTitle("АККАУНТ")
        CozyCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = 0.dp,
            radius = 18.dp,
        ) {
            Column {
                MetaRow(
                    icon = "✉️",
                    title = if (isGuest) "Привязать email" else "Email",
                    value = if (isGuest) null else (email ?: "—"),
                    onClick = if (isGuest) onLinkEmail else onEditProfile,
                )
                Divider()
                if (telegramStatus?.linked == true) {
                    MetaRow(
                        icon = "🤖",
                        title = "Telegram",
                        value = telegramStatus.telegramUsername?.let { "@$it" } ?: "Привязан",
                        onClick = onUnlinkTelegram,
                    )
                } else {
                    MetaRow(
                        icon = "🤖",
                        title = "Telegram",
                        value = "Подключить",
                        onClick = onLinkTelegram,
                    )
                }
                Divider()
                MetaRow(
                    icon = "✏️",
                    title = "Редактировать профиль",
                    onClick = onEditProfile,
                )
            }
        }
    }
}

@Composable
private fun AppearanceShortcuts(
    onTemplates: () -> Unit,
    onSettings: () -> Unit,
) {
    Column {
        SectionTitle("БЫСТРЫЙ ДОСТУП")
        CozyCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = 0.dp,
            radius = 18.dp,
        ) {
            Column {
                MetaRow(icon = "📋", title = "Шаблоны", onClick = onTemplates)
                Divider()
                MetaRow(icon = "🔔", title = "Уведомления", onClick = onSettings)
                Divider()
                MetaRow(icon = "🎨", title = "Тема", onClick = onSettings)
                Divider()
                MetaRow(icon = "🌐", title = "Язык", value = "Русский", onClick = onSettings)
            }
        }
    }
}

@Composable
private fun SpacesSection(groups: List<SpaceEntry>) {
    Column {
        SectionTitle("СЕМЬИ И ГРУППЫ")
        CozyCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = 0.dp,
            radius = 18.dp,
        ) {
            Column {
                SpaceRow(
                    letter = "Я",
                    color = MaterialTheme.colorScheme.primary,
                    title = "Личное",
                    subtitle = "Только вы",
                    onClick = {},
                )
                groups.forEach { g ->
                    Divider()
                    val palette = paletteForType(g.type)
                    SpaceRow(
                        letter = g.title.firstOrNull()?.uppercase() ?: "·",
                        color = palette,
                        title = g.title,
                        subtitle = if (g.role == "owner") "Владелец" else "Участник",
                        onClick = {},
                    )
                }
            }
        }
    }
}

@Composable
private fun paletteForType(type: String): Color {
    val extras = LocalCozyExtraColors.current
    return when (type) {
        "family" -> extras.coral
        "mentoring" -> extras.ochre
        else -> extras.lavender
    }
}

@Composable
private fun SpaceRow(
    letter: String,
    color: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    val spacing = LocalCozySpacing.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = spacing.lg, vertical = spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        CozyAvatar(letter = letter, color = color, size = 36.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text("›", fontSize = 14.sp, color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun DangerSection(
    onLogout: () -> Unit,
    onDelete: () -> Unit,
) {
    Column {
        SectionTitle("ОПАСНАЯ ЗОНА")
        CozyCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = 0.dp,
            radius = 18.dp,
        ) {
            Column {
                MetaRow(icon = "🚪", title = "Выйти", danger = true, onClick = onLogout)
                Divider()
                MetaRow(icon = "⚠️", title = "Удалить аккаунт", danger = true, onClick = onDelete)
            }
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
private fun ConfirmDeleteAccountDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    var input by remember { mutableStateOf("") }
    val canConfirm = input.trim() == "УДАЛИТЬ"
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Удалить аккаунт?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Это действие необратимо. Все списки, задачи, история и баланс будут стёрты.")
                Text(
                    "Чтобы подтвердить, введите УДАЛИТЬ:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = canConfirm) {
                Text(
                    "Удалить",
                    color = if (canConfirm) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.outline,
                )
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
    )
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
    val spacing = LocalCozySpacing.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = LocalCozyShapes.current.sheet,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.xxl)
                .padding(bottom = spacing.xxl),
            verticalArrangement = Arrangement.spacedBy(spacing.lg),
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
                color = if (expired) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                "Откройте бота — он сам подставит код. После этого вернитесь сюда.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(LocalCozyShapes.current.button)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable {
                        if (expired) onRetry() else uriHandler.openUri(deeplink)
                    }
                    .padding(vertical = spacing.md),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    if (expired) "Получить новый код" else "Открыть бота",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
            if (!expired) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(LocalCozyShapes.current.button)
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable(onClick = onCheckNow)
                        .padding(vertical = spacing.md),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "Я уже привязал, проверить",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
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
