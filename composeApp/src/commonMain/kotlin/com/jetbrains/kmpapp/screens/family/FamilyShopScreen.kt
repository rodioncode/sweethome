package com.jetbrains.kmpapp.screens.family

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jetbrains.kmpapp.data.gamification.Prize
import org.koin.compose.viewmodel.koinViewModel

private val shopGradient = Brush.linearGradient(listOf(Color(0xFFE8A87C), Color(0xFFD4956B)))

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
    val currencyIcon = currency?.icon ?: "💎"

    LaunchedEffect(Unit) {
        vm.events.collect { ev ->
            when (ev) {
                is GamificationViewModel.Event.Toast -> snackbar.showSnackbar(ev.message)
            }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }, containerColor = MaterialTheme.colorScheme.background) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Header with gradient
            Box(modifier = Modifier.fillMaxWidth().background(shopGradient)) {
                Text(
                    "🛍",
                    fontSize = 80.sp,
                    modifier = Modifier.align(Alignment.TopEnd),
                    color = Color.White.copy(alpha = 0.15f),
                )
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 14.dp, bottom = 20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(bottom = 14.dp),
                    ) {
                        Surface(onClick = navigateBack, modifier = Modifier.size(36.dp), shape = CircleShape, color = Color.White.copy(alpha = 0.2f)) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("‹", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                        Text(
                            "Магазин призов",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.weight(1f),
                        )
                        if (isOwnerOrAdmin) {
                            Surface(onClick = { creating = true }, modifier = Modifier.size(36.dp), shape = CircleShape, color = Color.White.copy(alpha = 0.25f)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("+", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                    Text("Твой баланс", fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("$myBalance", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(Modifier.size(6.dp))
                        Text(currencyIcon, fontSize = 22.sp, modifier = Modifier.padding(bottom = 4.dp))
                    }
                }
            }

            if (prizes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🎁", fontSize = 40.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            if (isOwnerOrAdmin) "Призов пока нет — добавьте первый" else "Призов пока нет",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 12.dp)) {
                    items(prizes, key = { it.id }) { prize ->
                        PrizeCard(
                            prize = prize,
                            currencyIcon = currencyIcon,
                            myBalance = myBalance,
                            canEdit = isOwnerOrAdmin,
                            onRedeem = { vm.redeemPrize(prize.id) },
                            onEdit = { editing = prize },
                            onDelete = { deleting = prize },
                        )
                    }
                    item { Spacer(Modifier.height(40.dp)) }
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
private fun PrizeCard(
    prize: Prize,
    currencyIcon: String,
    myBalance: Int,
    canEdit: Boolean,
    onRedeem: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val canAfford = myBalance >= prize.price
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                    contentAlignment = Alignment.Center,
                ) { Text("🎁", fontSize = 22.sp) }
                Spacer(Modifier.size(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(prize.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    prize.description?.takeIf { it.isNotBlank() }?.let {
                        Text(it, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (canEdit) {
                    Surface(onClick = onEdit, modifier = Modifier.size(32.dp), shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                        Box(contentAlignment = Alignment.Center) { Text("✎", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface) }
                    }
                    Spacer(Modifier.size(4.dp))
                    Surface(onClick = onDelete, modifier = Modifier.size(32.dp), shape = CircleShape, color = MaterialTheme.colorScheme.errorContainer) {
                        Box(contentAlignment = Alignment.Center) { Text("✕", fontSize = 14.sp, color = MaterialTheme.colorScheme.error) }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${prize.price}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.size(4.dp))
                Text(currencyIcon, fontSize = 14.sp)
                Spacer(Modifier.weight(1f))
                Surface(
                    onClick = if (canAfford) onRedeem else ({}),
                    shape = RoundedCornerShape(50),
                    color = if (canAfford) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Text(
                        if (canAfford) "Купить" else "Не хватает",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (canAfford) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
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
