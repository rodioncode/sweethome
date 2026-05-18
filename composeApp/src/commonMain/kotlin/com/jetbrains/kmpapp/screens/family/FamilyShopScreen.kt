package com.jetbrains.kmpapp.screens.family

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jetbrains.kmpapp.data.gamification.Prize
import com.jetbrains.kmpapp.ui.LocalCozyExtraColors
import com.jetbrains.kmpapp.ui.LocalCozyShapes
import com.jetbrains.kmpapp.ui.LocalCozySpacing
import com.jetbrains.kmpapp.ui.components.CozyCard
import com.jetbrains.kmpapp.ui.components.CozyTopBar
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FamilyShopScreen(
    navigateBack: () -> Unit,
) {
    val vm = koinViewModel<GamificationViewModel>()
    val isOwnerOrAdmin by vm.isOwnerOrAdmin.collectAsStateWithLifecycle()
    val currency by vm.currency.collectAsStateWithLifecycle()
    val leaderboard by vm.leaderboard.collectAsStateWithLifecycle()
    val prizes by vm.prizes.collectAsStateWithLifecycle()
    val currentUserId by vm.currentUserId.collectAsStateWithLifecycle()

    val snackbar = remember { SnackbarHostState() }
    var creating by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Prize?>(null) }
    var deleting by remember { mutableStateOf<Prize?>(null) }

    val myBalance = leaderboard.firstOrNull { it.userId == currentUserId }?.balance ?: 0
    val currencyIcon = currency?.icon ?: "⭐"

    LaunchedEffect(Unit) {
        vm.events.collect { ev ->
            when (ev) {
                is GamificationViewModel.Event.Toast -> snackbar.showSnackbar(ev.message)
            }
        }
    }

    val spacing = LocalCozySpacing.current

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            CozyTopBar(
                title = "Магазин наград",
                onBack = navigateBack,
                action = if (isOwnerOrAdmin) {
                    {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .clickable { creating = true },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "+",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    }
                } else null,
            )

            BalanceHero(balance = myBalance, currencyIcon = currencyIcon)

            if (prizes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🎁", fontSize = 40.sp)
                        Spacer(Modifier.height(spacing.xs))
                        Text(
                            if (isOwnerOrAdmin) "Призов пока нет — добавьте первый"
                            else "Призов пока нет",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                Text(
                    "ДОСТУПНЫЕ НАГРАДЫ",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = spacing.xxl, vertical = spacing.sm),
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(
                        start = spacing.lg, end = spacing.lg,
                        top = spacing.xxs, bottom = spacing.huge,
                    ),
                    verticalArrangement = Arrangement.spacedBy(spacing.sm),
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(prizes, key = { it.id }) { prize ->
                        PrizeTile(
                            prize = prize,
                            currencyIcon = currencyIcon,
                            myBalance = myBalance,
                            canEdit = isOwnerOrAdmin,
                            onRedeem = { vm.redeemPrize(prize.id) },
                            onEdit = { editing = prize },
                            onDelete = { deleting = prize },
                        )
                    }
                }
            }
        }
    }

    if (creating) {
        EditPrizeDialog(
            initial = null,
            onDismiss = { creating = false },
            onConfirm = { title, desc, price ->
                vm.createPrize(title, desc, price)
                creating = false
            },
        )
    }
    editing?.let { prize ->
        EditPrizeDialog(
            initial = prize,
            onDismiss = { editing = null },
            onConfirm = { title, desc, price ->
                vm.updatePrize(prize.id, title, desc, price)
                editing = null
            },
        )
    }
    deleting?.let { prize ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Удалить приз?") },
            text = { Text("«${prize.title}» будет удалён без возможности восстановления.") },
            confirmButton = {
                TextButton(onClick = { vm.deletePrize(prize.id); deleting = null }) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("Отмена") } },
        )
    }
}

@Composable
private fun BalanceHero(balance: Int, currencyIcon: String) {
    val spacing = LocalCozySpacing.current
    val extras = LocalCozyExtraColors.current
    CozyCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.lg, vertical = spacing.xs),
        background = extras.coralSoft,
        radius = 22.dp,
        contentPadding = spacing.xl,
        bordered = true,
    ) {
        Column {
            Text(
                "МОЙ БАЛАНС",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = extras.coral,
                letterSpacing = 1.sp,
            )
            Spacer(Modifier.height(spacing.xs))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    "$balance",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.size(spacing.xs))
                Text(currencyIcon, fontSize = 28.sp, modifier = Modifier.padding(bottom = spacing.xs))
            }
            Spacer(Modifier.height(spacing.xxs))
            Text(
                "Зарабатывай за выполнение задач",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PrizeTile(
    prize: Prize,
    currencyIcon: String,
    myBalance: Int,
    canEdit: Boolean,
    onRedeem: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val spacing = LocalCozySpacing.current
    val extras = LocalCozyExtraColors.current
    val canAfford = myBalance >= prize.price
    val tileBg = when (prize.id.hashCode().let { (it and 0x7FFFFFFF) % 4 }) {
        0 -> extras.coralSoft
        1 -> extras.ochreSoft
        2 -> extras.lavenderSoft
        else -> MaterialTheme.colorScheme.primaryContainer
    }
    CozyCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = if (canAfford) onRedeem else null,
        bordered = true,
        contentPadding = spacing.md,
        radius = 18.dp,
    ) {
        Column {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(LocalCozyShapes.current.chip)
                    .background(tileBg),
                contentAlignment = Alignment.Center,
            ) {
                Text("🎁", fontSize = 26.sp)
            }
            Spacer(Modifier.height(spacing.sm))
            Text(
                prize.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 2,
            )
            prize.description?.takeIf { it.isNotBlank() }?.let {
                Spacer(Modifier.height(spacing.xxs))
                Text(
                    it,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                )
            }
            Spacer(Modifier.height(spacing.xs))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${prize.price}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (canAfford) MaterialTheme.colorScheme.primary
                    else extras.textTer,
                )
                Spacer(Modifier.size(spacing.xxs))
                Text(currencyIcon, fontSize = 12.sp)
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .clip(LocalCozyShapes.current.chip)
                        .background(
                            if (canAfford) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant,
                        )
                        .padding(horizontal = spacing.sm, vertical = spacing.xxs),
                ) {
                    Text(
                        if (canAfford) "Заберу" else "Не хватает",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (canAfford) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (canEdit) {
                Spacer(Modifier.height(spacing.xs))
                Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                    Box(
                        modifier = Modifier
                            .clip(LocalCozyShapes.current.chip)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable(onClick = onEdit)
                            .padding(horizontal = spacing.xs, vertical = spacing.xxs),
                    ) {
                        Text(
                            "✎",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(LocalCozyShapes.current.chip)
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .clickable(onClick = onDelete)
                            .padding(horizontal = spacing.xs, vertical = spacing.xxs),
                    ) {
                        Text(
                            "✕",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EditPrizeDialog(
    initial: Prize?,
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String?, price: Int) -> Unit,
) {
    var title by remember { mutableStateOf(initial?.title.orEmpty()) }
    var description by remember { mutableStateOf(initial?.description.orEmpty()) }
    var priceText by remember { mutableStateOf(initial?.price?.toString() ?: "") }
    val priceInt = priceText.toIntOrNull()
    val canConfirm = title.isNotBlank() && priceInt != null && priceInt in 1..999_999

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Новый приз" else "Изменить приз") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Название") }, singleLine = true)
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Описание (опц.)") })
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { v -> priceText = v.filter { it.isDigit() }.take(6) },
                    label = { Text("Цена") },
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(title.trim(), description.trim().ifBlank { null }, priceInt ?: 0) },
                enabled = canConfirm,
            ) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
    )
}
