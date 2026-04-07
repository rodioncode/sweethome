package com.jetbrains.kmpapp.screens.todo

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jetbrains.kmpapp.auth.AuthViewModel
import com.jetbrains.kmpapp.data.lists.TodoList
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TodoListsScreen(
    navigateToListDetail: (String) -> Unit,
) {
    val viewModel = koinViewModel<TodoListsViewModel>()
    val authViewModel = koinViewModel<AuthViewModel>()
    val lists by viewModel.lists.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showCreateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(error) {
        error?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearError()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sweet Home") },
                actions = {
                    IconButton(onClick = { authViewModel.logout() }) {
                        Icon(Icons.Default.ExitToApp, "Выйти")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                content = { Icon(Icons.Default.Add, "Добавить список") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        TodoListsContent(
            lists = lists,
            contentPadding = paddingValues,
            showCreateDialog = showCreateDialog,
            onShowCreateDialog = { showCreateDialog = it },
            onCreateList = { title, type -> viewModel.createList(title, type) },
            onListClick = navigateToListDetail,
        )
    }
}

@Composable
internal fun TodoListsContent(
    lists: List<TodoList>,
    contentPadding: PaddingValues,
    showCreateDialog: Boolean,
    onShowCreateDialog: (Boolean) -> Unit,
    onCreateList: (title: String, type: String) -> Unit,
    onListClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedContent(lists.isNotEmpty()) { hasLists ->
        if (hasLists) {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                contentPadding = WindowInsets.safeDrawing.asPaddingValues(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(lists, key = { it.id }) { list ->
                    TodoListCard(
                        list = list,
                        onClick = { onListClick(list.id) },
                    )
                }
            }
        } else {
            EmptyTodoListsContent(
                modifier = modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                onCreateList = { onShowCreateDialog(true) },
            )
        }
    }

    if (showCreateDialog) {
        CreateListDialog(
            onDismiss = { onShowCreateDialog(false) },
            onConfirm = { title, type ->
                onCreateList(title, type)
                onShowCreateDialog(false)
            },
        )
    }
}

private val listTypeOptions = listOf(
    Triple("common", "Общий", Icons.Default.List),
    Triple("shopping", "Покупки", Icons.Default.ShoppingCart),
    Triple("home_chores", "Дела по дому", Icons.Default.Home),
    Triple("goal", "Цели", Icons.Default.Star),
    Triple("wishlist", "Хотелки", Icons.Default.Favorite),
)

@Composable
internal fun CreateListDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, type: String) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("common") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новый список") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                listTypeOptions.forEach { (type, label, icon) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedType = type },
                    ) {
                        RadioButton(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                        )
                        Icon(icon, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                        Text(label, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(title.ifBlank { "Мой список" }, selectedType) }) {
                Text("Создать")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        },
    )
}

private fun listTypeIcon(type: String) = when (type) {
    "shopping" -> Icons.Default.ShoppingCart
    "home_chores" -> Icons.Default.Home
    "goal" -> Icons.Default.Star
    "wishlist" -> Icons.Default.Favorite
    else -> Icons.Default.List // "common", "general_todos", unknown
}

private fun listTypeLabel(type: String) = when (type) {
    "shopping" -> "Покупки"
    "home_chores" -> "Дела по дому"
    "goal" -> "Цели"
    "wishlist" -> "Хотелки"
    else -> "Общий" // "common", "general_todos"
}

@Composable
internal fun TodoListCard(
    list: TodoList,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                listTypeIcon(list.type),
                contentDescription = null,
                modifier = Modifier.padding(end = 12.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = list.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = listTypeLabel(list.type),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun EmptyTodoListsContent(
    modifier: Modifier = Modifier,
    onCreateList: () -> Unit,
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(text = "Нет списков", style = MaterialTheme.typography.titleLarge)
            Text(
                text = "Нажмите + чтобы создать первый список дел",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
