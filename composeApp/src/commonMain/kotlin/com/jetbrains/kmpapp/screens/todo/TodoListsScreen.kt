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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import com.jetbrains.kmpapp.data.groups.Group
import com.jetbrains.kmpapp.data.lists.TodoList
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TodoListsScreen(
    navigateToListDetail: (String) -> Unit,
) {
    val viewModel = koinViewModel<TodoListsViewModel>()
    val authViewModel = koinViewModel<AuthViewModel>()
    val lists by viewModel.lists.collectAsStateWithLifecycle()
    val groups by viewModel.groups.collectAsStateWithLifecycle()
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
            groups = groups,
            contentPadding = paddingValues,
            showCreateDialog = showCreateDialog,
            onShowCreateDialog = { showCreateDialog = it },
            onCreateList = { title, type, icon, scope, groupId ->
                viewModel.createList(title, type, icon, scope, groupId)
            },
            onListClick = navigateToListDetail,
        )
    }
}

@Composable
internal fun TodoListsContent(
    lists: List<TodoList>,
    groups: List<Group> = emptyList(),
    contentPadding: PaddingValues,
    showCreateDialog: Boolean,
    onShowCreateDialog: (Boolean) -> Unit,
    onCreateList: (title: String, type: String, icon: String?, scope: String, groupId: String?) -> Unit,
    onListClick: (String) -> Unit,
    isGuest: Boolean = false,
    navigateToLinkEmail: (() -> Unit)? = null,
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
                if (isGuest && navigateToLinkEmail != null) {
                    item(key = "guest_banner") {
                        GuestLinkEmailBanner(
                            onLinkEmail = navigateToLinkEmail,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        )
                    }
                }
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
                isGuest = isGuest,
                navigateToLinkEmail = navigateToLinkEmail,
            )
        }
    }

    if (showCreateDialog) {
        CreateListDialog(
            groups = groups,
            onDismiss = { onShowCreateDialog(false) },
            onConfirm = { title, type, icon, scope, groupId ->
                onCreateList(title, type, icon, scope, groupId)
                onShowCreateDialog(false)
            },
        )
    }
}

private val listTypeOptions = listOf(
    Triple("general_todos", "Общий", Icons.Default.List),
    Triple("shopping", "Покупки", Icons.Default.ShoppingCart),
    Triple("home_chores", "Дела по дому", Icons.Default.Home),
)

private val scopeOptions = listOf(
    "personal" to "Личный",
    "group" to "Групповой",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CreateListDialog(
    groups: List<Group> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (title: String, type: String, icon: String?, scope: String, groupId: String?) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var icon by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("general_todos") }
    var selectedScope by remember { mutableStateOf("personal") }
    var selectedGroupId by remember { mutableStateOf<String?>(null) }
    var scopeExpanded by remember { mutableStateOf(false) }
    var groupExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новый список") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Title
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Название") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                // Icon (emoji)
                item {
                    OutlinedTextField(
                        value = icon,
                        onValueChange = { icon = it },
                        label = { Text("Иконка (эмодзи)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                // Type selection
                item {
                    Text(
                        "Тип списка",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                item {
                    Column {
                        listTypeOptions.forEach { (type, label, typeIcon) ->
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
                                Icon(typeIcon, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                                Text(label, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
                // Scope dropdown
                item {
                    ExposedDropdownMenuBox(
                        expanded = scopeExpanded,
                        onExpandedChange = { scopeExpanded = it },
                    ) {
                        OutlinedTextField(
                            value = scopeOptions.first { it.first == selectedScope }.second,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Область") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = scopeExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        )
                        ExposedDropdownMenu(
                            expanded = scopeExpanded,
                            onDismissRequest = { scopeExpanded = false },
                        ) {
                            scopeOptions.forEach { (value, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        selectedScope = value
                                        if (value == "personal") selectedGroupId = null
                                        scopeExpanded = false
                                    },
                                )
                            }
                        }
                    }
                }
                // Group dropdown (when scope = group)
                if (selectedScope == "group" && groups.isNotEmpty()) {
                    item {
                        val selectedGroupName = groups.find { it.id == selectedGroupId }?.name ?: "Выберите группу"
                        ExposedDropdownMenuBox(
                            expanded = groupExpanded,
                            onExpandedChange = { groupExpanded = it },
                        ) {
                            OutlinedTextField(
                                value = selectedGroupName,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Группа") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = groupExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            )
                            ExposedDropdownMenu(
                                expanded = groupExpanded,
                                onDismissRequest = { groupExpanded = false },
                            ) {
                                groups.forEach { group ->
                                    DropdownMenuItem(
                                        text = { Text(group.name) },
                                        onClick = {
                                            selectedGroupId = group.id
                                            groupExpanded = false
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        title.ifBlank { "Мой список" },
                        selectedType,
                        icon.takeIf { it.isNotBlank() },
                        selectedScope,
                        selectedGroupId,
                    )
                },
                enabled = selectedScope == "personal" || selectedGroupId != null,
            ) {
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
    else -> Icons.Default.List
}

private fun listTypeLabel(type: String) = when (type) {
    "shopping" -> "Покупки"
    "home_chores" -> "Дела по дому"
    else -> "Общий"
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
            if (!list.icon.isNullOrBlank()) {
                Text(
                    text = list.icon,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(end = 12.dp),
                )
            } else {
                Icon(
                    listTypeIcon(list.type),
                    contentDescription = null,
                    modifier = Modifier.padding(end = 12.dp),
                )
            }
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
private fun GuestLinkEmailBanner(
    onLinkEmail: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Привяжите email, чтобы не\nпотерять свои данные",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onLinkEmail) {
                Text("Привязать")
            }
        }
    }
}

@Composable
private fun EmptyTodoListsContent(
    modifier: Modifier = Modifier,
    onCreateList: () -> Unit,
    isGuest: Boolean = false,
    navigateToLinkEmail: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (isGuest && navigateToLinkEmail != null) {
            GuestLinkEmailBanner(
                onLinkEmail = navigateToLinkEmail,
                modifier = Modifier.padding(bottom = 16.dp),
            )
        }
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
