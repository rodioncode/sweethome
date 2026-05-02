package com.jetbrains.kmpapp.screens.wishlist

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jetbrains.kmpapp.data.lists.TodoItem
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PublicWishlistScreen(
    token: String,
    onBack: () -> Unit,
) {
    val vm = koinViewModel<PublicWishlistViewModel>()
    val state by vm.state.collectAsStateWithLifecycle()
    val toast by vm.toast.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(token) { vm.load(token) }

    LaunchedEffect(toast) {
        toast?.let {
            snackbar.showSnackbar(it)
            vm.clearToast()
        }
    }

    var claimItem by remember { mutableStateOf<TodoItem?>(null) }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        when (val s = state) {
            PublicWishlistViewModel.State.Loading -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            PublicWishlistViewModel.State.NotFound -> CenteredMessage("Список не найден или скрыт", padding, onBack)
            is PublicWishlistViewModel.State.Error -> CenteredMessage(s.message, padding, onBack)
            is PublicWishlistViewModel.State.Loaded -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                ) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(20.dp),
                        ) {
                            Column {
                                Text(s.wishlist.list.title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                                Text("Список желаний", fontSize = 13.sp, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f))
                            }
                        }
                    }
                    items(s.wishlist.items, key = { it.id }) { item ->
                        WishItemCard(item = item, onClaim = { claimItem = item })
                    }
                    item { Spacer(Modifier.height(40.dp)) }
                }
            }
        }
    }

    claimItem?.let { item ->
        ClaimDialog(
            itemTitle = item.title,
            onDismiss = { claimItem = null },
            onConfirm = { name, anonymous ->
                vm.claim(item.id, name, anonymous)
                claimItem = null
            },
        )
    }
}

@Composable
private fun CenteredMessage(text: String, padding: androidx.compose.foundation.layout.PaddingValues, onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextButton(onClick = onBack) { Text("Назад") }
        }
    }
}

@Composable
private fun WishItemCard(item: TodoItem, onClaim: () -> Unit) {
    val isClaimed = item.wishlist?.isClaimed == true
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                contentAlignment = Alignment.Center,
            ) { Text("🎁", fontSize = 22.sp) }
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                item.wishlist?.price?.let { Text("$it ₽", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            if (isClaimed) {
                Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surfaceVariant) {
                    Text("Занято", modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                Surface(onClick = onClaim, shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.primary) {
                    Text("Забронировать", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}

@Composable
private fun ClaimDialog(
    itemTitle: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, anonymous: Boolean) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var anonymous by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Забронировать «$itemTitle»") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Ваше имя") }, singleLine = true)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = anonymous, onCheckedChange = { anonymous = it })
                    Spacer(Modifier.size(8.dp))
                    Text("Анонимно", fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name.ifBlank { "—" }, anonymous) },
                enabled = anonymous || name.isNotBlank(),
            ) { Text("Забронировать") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
    )
}
