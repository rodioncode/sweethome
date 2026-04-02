package com.jetbrains.kmpapp.screens.groups

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jetbrains.kmpapp.data.lists.TodoList
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    groupId: String,
    currentUserId: String?,
    navigateBack: () -> Unit,
    navigateToListDetail: (listId: String) -> Unit,
) {
    val viewModel = koinViewModel<GroupDetailViewModel>(parameters = { parametersOf(groupId) })
    val group by viewModel.group.collectAsState()
    val groupLists by viewModel.groupLists.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    val clipboardManager = LocalClipboardManager.current

    var showDeleteGroupDialog by remember { mutableStateOf(false) }
    var showInviteLinkDialog by remember { mutableStateOf<String?>(null) }

    val isOwner = group?.role == "owner"

    // Диалог подтверждения удаления группы
    if (showDeleteGroupDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteGroupDialog = false },
            title = { Text("Удалить группу?") },
            text = { Text("Все списки и задачи группы «${group?.name}» будут удалены. Это действие необратимо.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteGroupDialog = false
                        viewModel.deleteGroup { navigateBack() }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteGroupDialog = false }) { Text("Отмена") }
            },
        )
    }

    // Диалог с инвайт-ссылкой
    showInviteLinkDialog?.let { deepLink ->
        AlertDialog(
            onDismissRequest = { showInviteLinkDialog = null; viewModel.dismissState() },
            title = { Text("Пригласить в группу") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Поделитесь этой ссылкой:")
                    OutlinedTextField(
                        value = deepLink,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    clipboardManager.setText(AnnotatedString(deepLink))
                    showInviteLinkDialog = null
                    viewModel.dismissState()
                }) { Text("Скопировать") }
            },
            dismissButton = {
                TextButton(onClick = { showInviteLinkDialog = null; viewModel.dismissState() }) {
                    Text("Закрыть")
                }
            },
        )
    }

    // Обработка состояний
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is GroupDetailUiState.InviteCreated -> {
                showInviteLinkDialog = "familytodo://invite/${state.invite.token}"
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(group?.name ?: "Группа") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    // Кнопка «Пригласить» — доступна всем участникам
                    IconButton(onClick = { viewModel.createInvite() }) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Пригласить")
                    }
                    // Owner-действия
                    if (isOwner) {
                        IconButton(onClick = { showDeleteGroupDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Удалить группу",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                },
            )
        },
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (uiState) {
                is GroupDetailUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is GroupDetailUiState.Error -> {
                    val msg = (uiState as GroupDetailUiState.Error).message
                    Snackbar(
                        modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                        action = { TextButton(onClick = { viewModel.dismissState() }) { Text("OK") } },
                    ) { Text(msg) }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp),
                    ) {
                        // Секция списков группы
                        if (groupLists.isNotEmpty()) {
                            item {
                                Text(
                                    "Списки группы",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            items(groupLists) { list ->
                                GroupListItem(list = list, onClick = { navigateToListDetail(list.id) })
                            }
                            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }
                        }

                        // Секция роли / действий участника
                        item {
                            Text(
                                "Моя роль",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        item {
                            ListItem(
                                headlineContent = {
                                    Text(if (isOwner) "Владелец" else "Участник")
                                },
                                leadingContent = {
                                    Icon(
                                        if (isOwner) Icons.Default.Star else Icons.Default.Person,
                                        contentDescription = null,
                                        tint = if (isOwner) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                },
                            )
                        }

                        // Выход из группы (только для участников, не owner)
                        if (!isOwner && currentUserId != null) {
                            item {
                                HorizontalDivider()
                                ListItem(
                                    headlineContent = {
                                        Text("Выйти из группы", color = MaterialTheme.colorScheme.error)
                                    },
                                    leadingContent = {
                                        Icon(
                                            Icons.Default.ExitToApp,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                        )
                                    },
                                    modifier = Modifier.clickableWithRipple {
                                        viewModel.removeMember(currentUserId) { navigateBack() }
                                    },
                                )
                            }
                        }

                        // Подсказка для owner — нельзя выйти без передачи роли
                        if (isOwner) {
                            item {
                                HorizontalDivider()
                                Card(
                                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    ),
                                ) {
                                    Text(
                                        "Чтобы покинуть группу, сначала передайте роль владельца другому участнику.",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(12.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupListItem(list: TodoList, onClick: () -> Unit) {
    val icon = when (list.type) {
        "shopping" -> Icons.Default.ShoppingCart
        "home_chores" -> Icons.Default.Home
        else -> Icons.Default.CheckCircle
    }
    ListItem(
        headlineContent = { Text(list.title) },
        leadingContent = {
            Text(list.icon ?: "", style = MaterialTheme.typography.titleMedium)
                .takeIf { list.icon != null }
                ?: Icon(icon, contentDescription = null)
        },
        modifier = Modifier.clickableWithRipple(onClick),
    )
}

// Расширение для Modifier — кликабельность с ripple без предупреждений
private fun Modifier.clickableWithRipple(onClick: () -> Unit): Modifier =
    this.then(Modifier.clickable(onClick = onClick))
