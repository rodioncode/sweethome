package com.jetbrains.kmpapp.screens.family

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jetbrains.kmpapp.data.achievements.Achievement
import com.jetbrains.kmpapp.data.gamification.LeaderboardEntry
import com.jetbrains.kmpapp.data.gamification.Transaction
import com.jetbrains.kmpapp.ui.LocalCozyExtraColors
import com.jetbrains.kmpapp.ui.LocalCozyShapes
import com.jetbrains.kmpapp.ui.LocalCozySpacing
import com.jetbrains.kmpapp.ui.components.CozyAvatar
import com.jetbrains.kmpapp.ui.components.CozyCard
import com.jetbrains.kmpapp.ui.components.CozyTopBar
import org.koin.compose.viewmodel.koinViewModel

private val rankEmoji = listOf("🥇", "🥈", "🥉")

@Composable
private fun paletteFor(seed: String): Color {
    val extras = LocalCozyExtraColors.current
    val palette = listOf(
        extras.coral, extras.primaryLight, extras.lavender,
        extras.ochre, MaterialTheme.colorScheme.primary, extras.success,
    )
    return palette[(seed.hashCode() and 0x7FFFFFFF) % palette.size]
}

private fun initialsOf(name: String?, fallback: String): String =
    (name ?: fallback).split(" ", limit = 2).map { it.firstOrNull()?.toString().orEmpty() }
        .joinToString("").uppercase().take(2).ifBlank { "?" }

@Composable
fun GamificationScreen(
    navigateBack: () -> Unit,
    navigateToShop: () -> Unit,
) {
    val vm = koinViewModel<GamificationViewModel>()
    val workspace by vm.familyWorkspace.collectAsStateWithLifecycle()
    val isOwnerOrAdmin by vm.isOwnerOrAdmin.collectAsStateWithLifecycle()
    val currency by vm.currency.collectAsStateWithLifecycle()
    val leaderboard by vm.leaderboard.collectAsStateWithLifecycle()
    val transactions by vm.transactions.collectAsStateWithLifecycle()
    val achievementsCatalog by vm.achievementsCatalog.collectAsStateWithLifecycle()
    val achievementsMine by vm.achievementsMine.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    var showCurrencyDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        vm.events.collect { ev ->
            when (ev) {
                is GamificationViewModel.Event.Toast -> snackbar.showSnackbar(ev.message)
            }
        }
    }

    val spacing = LocalCozySpacing.current
    val currencyIcon = currency?.icon ?: "⭐"

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            CozyTopBar(
                title = "Рейтинг семьи 🏆",
                onBack = navigateBack,
                action = if (isOwnerOrAdmin) {
                    {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { showCurrencyDialog = true },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("✎", fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                } else null,
            )

            if (workspace == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Создайте семейное пространство, чтобы видеть рейтинг",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(spacing.xxl),
                    )
                }
                return@Scaffold
            }

            LazyColumn(contentPadding = PaddingValues(bottom = spacing.xxxl)) {
                if (leaderboard.size >= 3) {
                    item {
                        Podium(
                            top = leaderboard.take(3),
                            currencyIcon = currencyIcon,
                            modifier = Modifier.padding(horizontal = spacing.lg, vertical = spacing.xxl),
                        )
                    }
                }

                item {
                    SectionLabel("ВСЕ УЧАСТНИКИ")
                }

                if (leaderboard.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(spacing.xxl),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "Пока никто не заработал баллы",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                } else {
                    val toShow = if (leaderboard.size >= 3) leaderboard.drop(3) else leaderboard
                    items(toShow, key = { it.userId }) { entry ->
                        val rank = leaderboard.indexOfFirst { it.userId == entry.userId } + 1
                        LeaderboardRow(entry = entry, rank = rank, currencyIcon = currencyIcon)
                    }
                }

                item {
                    Spacer(Modifier.height(spacing.xl))
                    ShopBanner(onClick = navigateToShop)
                }

                if (achievementsCatalog.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(spacing.xxl))
                        SectionLabel("ДОСТИЖЕНИЯ")
                        AchievementsGrid(
                            catalog = achievementsCatalog,
                            earned = achievementsMine.map { it.id }.toSet(),
                        )
                    }
                }

                if (transactions.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(spacing.xxl))
                        SectionLabel("ИСТОРИЯ")
                    }
                    items(transactions.take(20), key = { it.id }) { tx ->
                        TransactionRow(
                            tx = tx,
                            currencyIcon = currencyIcon,
                            displayNameOf = { uid -> leaderboard.firstOrNull { it.userId == uid }?.displayName },
                        )
                    }
                }
            }
        }
    }

    if (showCurrencyDialog) {
        EditCurrencyDialog(
            initialName = currency?.name ?: "монетки",
            initialIcon = currency?.icon ?: "🪙",
            onDismiss = { showCurrencyDialog = false },
            onConfirm = { n, i ->
                vm.updateCurrency(n, i)
                showCurrencyDialog = false
            },
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    val spacing = LocalCozySpacing.current
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(horizontal = spacing.xxl, vertical = spacing.sm),
    )
}

@Composable
private fun Podium(
    top: List<LeaderboardEntry>,
    currencyIcon: String,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalCozySpacing.current
    val extras = LocalCozyExtraColors.current
    val first = top[0]
    val second = top[1]
    val third = top[2]
    CozyCard(
        modifier = modifier.fillMaxWidth(),
        background = extras.ochreSoft,
        radius = 22.dp,
        contentPadding = spacing.lg,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(spacing.xs),
        ) {
            PodiumSlot(
                entry = second,
                rank = 2,
                podiumHeight = 60.dp,
                avatarSize = 52.dp,
                currencyIcon = currencyIcon,
                podiumColor = MaterialTheme.colorScheme.surface,
                accentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            PodiumSlot(
                entry = first,
                rank = 1,
                podiumHeight = 80.dp,
                avatarSize = 64.dp,
                currencyIcon = currencyIcon,
                podiumColor = extras.ochre,
                accentColor = extras.ochre,
                showCrown = true,
                modifier = Modifier.weight(1.1f),
            )
            PodiumSlot(
                entry = third,
                rank = 3,
                podiumHeight = 44.dp,
                avatarSize = 52.dp,
                currencyIcon = currencyIcon,
                podiumColor = MaterialTheme.colorScheme.surface,
                accentColor = extras.coral,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun PodiumSlot(
    entry: LeaderboardEntry,
    rank: Int,
    podiumHeight: androidx.compose.ui.unit.Dp,
    avatarSize: androidx.compose.ui.unit.Dp,
    currencyIcon: String,
    podiumColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier,
    showCrown: Boolean = false,
) {
    val spacing = LocalCozySpacing.current
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        if (showCrown) {
            Text("👑", fontSize = 22.sp)
        } else {
            Text(
                "$rank",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }
        CozyAvatar(
            letter = initialsOf(entry.displayName, entry.userId),
            color = paletteFor(entry.userId),
            size = avatarSize,
        )
        Spacer(Modifier.height(spacing.xs))
        Text(
            entry.displayName?.split(" ")?.firstOrNull() ?: entry.userId.take(6),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
        )
        Text(
            "${entry.balance}$currencyIcon",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = accentColor,
        )
        Spacer(Modifier.height(spacing.xs))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(podiumHeight)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(podiumColor),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = rankEmoji.getOrElse(rank - 1) { "$rank" },
                fontSize = if (rank == 1) 28.sp else 22.sp,
            )
        }
    }
}

@Composable
private fun LeaderboardRow(entry: LeaderboardEntry, rank: Int, currencyIcon: String) {
    val spacing = LocalCozySpacing.current
    val extras = LocalCozyExtraColors.current
    CozyCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.lg, vertical = spacing.xxs),
        bordered = true,
        contentPadding = spacing.sm,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            Text(
                "$rank",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(20.dp),
            )
            CozyAvatar(
                letter = initialsOf(entry.displayName, entry.userId),
                color = paletteFor(entry.userId),
                size = 40.dp,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    entry.displayName ?: entry.userId.take(8),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                )
                Text(
                    "Всего заработано: ${entry.totalEarned}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                "${entry.balance}$currencyIcon",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = extras.ochre,
            )
        }
    }
}

@Composable
private fun ShopBanner(onClick: () -> Unit) {
    val spacing = LocalCozySpacing.current
    val extras = LocalCozyExtraColors.current
    CozyCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.lg),
        background = extras.coralSoft,
        onClick = onClick,
        radius = 18.dp,
        contentPadding = spacing.md,
        bordered = true,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            Text("🛍", fontSize = 28.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Магазин наград",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    "Потратить баллы",
                    fontSize = 12.sp,
                    color = extras.coral,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Text("→", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = extras.coral)
        }
    }
}

@Composable
private fun TransactionRow(
    tx: Transaction,
    currencyIcon: String,
    displayNameOf: (String) -> String?,
) {
    val spacing = LocalCozySpacing.current
    val positive = tx.amount > 0
    CozyCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.lg, vertical = 3.dp),
        bordered = true,
        radius = 14.dp,
        contentPadding = spacing.sm,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            Text(if (positive) "✓" else "🛍", fontSize = 16.sp)
            Column(modifier = Modifier.weight(1f)) {
                val label = when (tx.sourceType) {
                    "item" -> "Задача выполнена"
                    "prize_redeem" -> "Покупка приза"
                    else -> tx.sourceType
                }
                Text(
                    label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    "${displayNameOf(tx.userId) ?: tx.userId.take(8)} · ${tx.createdAt.take(10)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (positive) "+${tx.amount}" else "${tx.amount}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (positive) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error,
                )
                Spacer(Modifier.width(2.dp))
                Text(currencyIcon, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun AchievementsGrid(catalog: List<Achievement>, earned: Set<String>) {
    val spacing = LocalCozySpacing.current
    Column(
        modifier = Modifier.padding(horizontal = spacing.lg),
        verticalArrangement = Arrangement.spacedBy(spacing.xs),
    ) {
        catalog.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                row.forEach { ach ->
                    AchievementTile(ach = ach, unlocked = ach.id in earned, modifier = Modifier.weight(1f))
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun AchievementTile(ach: Achievement, unlocked: Boolean, modifier: Modifier = Modifier) {
    val spacing = LocalCozySpacing.current
    val extras = LocalCozyExtraColors.current
    val bg = when {
        !unlocked -> MaterialTheme.colorScheme.surfaceVariant
        ach.id.hashCode().let { (it and 0x7FFFFFFF) % 3 } == 0 -> extras.coralSoft
        ach.id.hashCode().let { (it and 0x7FFFFFFF) % 3 } == 1 -> extras.ochreSoft
        else -> MaterialTheme.colorScheme.primaryContainer
    }
    Box(
        modifier = modifier
            .clip(LocalCozyShapes.current.chip)
            .background(bg)
            .then(
                if (!unlocked) Modifier.border(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant,
                    LocalCozyShapes.current.chip,
                ) else Modifier
            )
            .padding(spacing.sm),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(if (unlocked) (ach.icon ?: "🏆") else "🔒", fontSize = 28.sp)
            Spacer(Modifier.height(spacing.xxs))
            Text(
                ach.title,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
            )
        }
    }
}

private val currencyEmojiPresets = listOf("🪙", "⭐", "❤️", "💎", "🎁", "🏆", "✨", "🔥", "🌟", "🍀")

@Composable
private fun EditCurrencyDialog(
    initialName: String,
    initialIcon: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, icon: String) -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    var icon by remember { mutableStateOf(initialIcon) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Валюта семьи") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(30) },
                    label = { Text("Название (мн.ч., например «Монеты»)") },
                    singleLine = true,
                )
                Text(
                    "Иконка:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    currencyEmojiPresets.forEach { preset ->
                        val selected = icon == preset
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant,
                                )
                                .clickable { icon = preset },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(preset, fontSize = 18.sp)
                        }
                    }
                }
                OutlinedTextField(
                    value = icon,
                    onValueChange = { icon = it.take(4) },
                    label = { Text("Или своя иконка") },
                    singleLine = true,
                )
                val previewName = name.trim().ifBlank { initialName }
                val previewIcon = icon.trim().ifBlank { initialIcon }
                Box(
                    modifier = Modifier
                        .clip(LocalCozyShapes.current.chip)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = "Участники будут зарабатывать $previewName $previewIcon",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        name.trim().ifBlank { initialName },
                        icon.trim().ifBlank { initialIcon },
                    )
                },
            ) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
    )
}

/**
 * Backward-compat helper for legacy callers (FamilyScreen, etc.) — keeps
 * the old MemberCircle signature so we don't break other modules.
 */
@Composable
internal fun MemberCircle(
    displayName: String,
    color: Color,
    size: Int,
    fontSize: Int,
    borderColor: Color? = null,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(color)
            .then(if (borderColor != null) Modifier.border(3.dp, borderColor, CircleShape) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            displayName,
            fontSize = fontSize.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
    }
}
