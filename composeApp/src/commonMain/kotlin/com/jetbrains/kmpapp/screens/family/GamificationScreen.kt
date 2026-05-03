package com.jetbrains.kmpapp.screens.family

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jetbrains.kmpapp.data.achievements.Achievement
import com.jetbrains.kmpapp.data.gamification.LeaderboardEntry
import com.jetbrains.kmpapp.data.gamification.Transaction
import org.koin.compose.viewmodel.koinViewModel

private val rankEmoji = listOf("🥇", "🥈", "🥉", "4️⃣", "5️⃣", "6️⃣")
private val avatarPalette = listOf(
    Color(0xFF5B7C5A), Color(0xFF42A5F5), Color(0xFFFF7043),
    Color(0xFFAB47BC), Color(0xFFFFA726), Color(0xFF26A69A),
)

private fun colorFor(seed: String): Color = avatarPalette[(seed.hashCode().and(0x7FFFFFFF)) % avatarPalette.size]
private fun initialsOf(name: String?, fallback: String): String =
    (name ?: fallback).split(" ", limit = 2).map { it.firstOrNull()?.toString().orEmpty() }.joinToString("").uppercase().take(2).ifBlank { "?" }

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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Surface(onClick = navigateBack, modifier = Modifier.size(36.dp), shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("‹", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Text(
                        "${currency?.icon ?: "🏆"} ${currency?.name?.replaceFirstChar { it.uppercaseChar() } ?: "Семейный рейтинг"}",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    if (isOwnerOrAdmin) {
                        Surface(onClick = { showCurrencyDialog = true }, shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(36.dp)) {
                            Box(contentAlignment = Alignment.Center) { Text("✎", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface) }
                        }
                    }
                }
            }

            if (workspace == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Создайте семейное пространство, чтобы видеть рейтинг", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(24.dp))
                }
                return@Scaffold
            }

            LazyColumn(contentPadding = PaddingValues(bottom = 32.dp)) {
                if (leaderboard.size >= 3) {
                    item { Podium(leaderboard.take(3), modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)) }
                }

                item {
                    Text("Таблица лидеров", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                    Spacer(Modifier.height(8.dp))
                }

                if (leaderboard.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            Text("Пока никто не заработал баллы", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    items(leaderboard.size) { i ->
                        LeaderboardRow(entry = leaderboard[i], rank = i + 1, currencyIcon = currency?.icon ?: "💎")
                    }
                }

                item {
                    Spacer(Modifier.height(20.dp))
                    Surface(
                        onClick = navigateToShop,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = Color.Transparent,
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(listOf(Color(0xFFE8A87C), Color(0xFFD4956B))),
                                    RoundedCornerShape(14.dp),
                                )
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("🛍 Потратить баллы в магазине", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                if (achievementsCatalog.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(24.dp))
                        Text("Достижения", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                        Spacer(Modifier.height(8.dp))
                        AchievementsGrid(catalog = achievementsCatalog, earned = achievementsMine.map { it.id }.toSet())
                    }
                }

                if (transactions.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(24.dp))
                        Text("История", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                        Spacer(Modifier.height(8.dp))
                    }
                    items(transactions.take(20), key = { it.id }) { tx ->
                        TransactionRow(tx = tx, currencyIcon = currency?.icon ?: "💎", displayNameOf = { uid -> leaderboard.firstOrNull { it.userId == uid }?.displayName })
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
private fun Podium(top: List<LeaderboardEntry>, modifier: Modifier = Modifier) {
    val first = top[0]
    val second = top[1]
    val third = top[2]
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PodiumSlot(second, 2, podiumHeight = 60, podiumColor = Color(0xFFE8E8E8), avatarSize = 56, avatarBorderColor = Color(0xFFC0C0C0), modifier = Modifier.weight(1f))
        PodiumSlot(first, 1, podiumHeight = 80, podiumColor = null, avatarSize = 68, avatarBorderColor = Color(0xFFFFD700), modifier = Modifier.weight(1f))
        PodiumSlot(third, 3, podiumHeight = 44, podiumColor = Color(0xFFCD7F32), avatarSize = 56, avatarBorderColor = Color(0xFFCD7F32), modifier = Modifier.weight(1f))
    }
}

@Composable
private fun PodiumSlot(
    entry: LeaderboardEntry,
    rank: Int,
    podiumHeight: Int,
    podiumColor: Color?,
    avatarSize: Int,
    avatarBorderColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        MemberCircle(
            displayName = initialsOf(entry.displayName, entry.userId),
            color = colorFor(entry.userId),
            size = avatarSize,
            fontSize = if (avatarSize >= 68) 22 else 18,
            borderColor = avatarBorderColor,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            entry.displayName?.split(" ")?.firstOrNull() ?: entry.userId.take(6),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
        )
        Text("${entry.balance} ⭐", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(podiumHeight.dp)
                .then(
                    if (podiumColor != null) Modifier.background(podiumColor, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    else Modifier.background(
                        Brush.verticalGradient(listOf(Color(0xFFFFD700), Color(0xFFFFA000))),
                        RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(rankEmoji[rank - 1], fontSize = if (rank == 1) 24.sp else 20.sp)
        }
    }
}

@Composable
private fun LeaderboardRow(entry: LeaderboardEntry, rank: Int, currencyIcon: String) {
    val isFirst = rank == 1
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        color = if (isFirst) Color(0xFFFFFDE7) else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isFirst) Color(0xFFFFD700) else MaterialTheme.colorScheme.outline),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(rankEmoji.getOrElse(rank - 1) { "$rank" }, fontSize = 20.sp, modifier = Modifier.width(28.dp))
            MemberCircle(
                displayName = initialsOf(entry.displayName, entry.userId),
                color = colorFor(entry.userId),
                size = 40, fontSize = 14,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.displayName ?: entry.userId.take(8), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                Text("Всего заработано: ${entry.totalEarned}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${entry.balance}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(2.dp))
                    Text(currencyIcon, fontSize = 13.sp)
                }
                Text("баланс", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun TransactionRow(tx: Transaction, currencyIcon: String, displayNameOf: (String) -> String?) {
    val positive = tx.amount > 0
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 3.dp),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(if (positive) "✓" else "🛍", fontSize = 16.sp)
            Column(modifier = Modifier.weight(1f)) {
                val label = when (tx.sourceType) {
                    "item" -> "Задача выполнена"
                    "prize_redeem" -> "Покупка приза"
                    else -> tx.sourceType
                }
                Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
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
                    color = if (positive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                )
                Spacer(Modifier.width(2.dp))
                Text(currencyIcon, fontSize = 12.sp)
            }
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
                Text("Иконка:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    currencyEmojiPresets.forEach { preset ->
                        val selected = icon == preset
                        Surface(
                            onClick = { icon = preset },
                            shape = CircleShape,
                            color = if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(40.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(preset, fontSize = 18.sp)
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = icon,
                    onValueChange = { icon = it.take(4) },
                    label = { Text("Или своя иконка") },
                    singleLine = true,
                )
                // Preview
                val previewName = name.trim().ifBlank { initialName }
                val previewIcon = icon.trim().ifBlank { initialIcon }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Text(
                        text = "Участники будут зарабатывать $previewName $previewIcon",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name.trim().ifBlank { initialName }, icon.trim().ifBlank { initialIcon }) }) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
    )
}

@Composable
private fun AchievementsGrid(catalog: List<Achievement>, earned: Set<String>) {
    Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        catalog.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { ach ->
                    AchievementCard(ach = ach, unlocked = ach.id in earned, modifier = Modifier.weight(1f))
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun AchievementCard(ach: Achievement, unlocked: Boolean, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = if (unlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (unlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        ),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(if (unlocked) (ach.icon ?: "🏆") else "🔒", fontSize = 28.sp)
            Spacer(Modifier.height(6.dp))
            Text(ach.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            ach.description?.takeIf { it.isNotBlank() }?.let {
                Spacer(Modifier.height(2.dp))
                Text(it, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 15.sp)
            }
            if (unlocked) {
                Spacer(Modifier.height(6.dp))
                Text("✓ Получено", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

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
        Text(displayName, fontSize = fontSize.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}
