package com.jetbrains.kmpapp.screens.groups

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jetbrains.kmpapp.data.groups.GroupMember
import com.jetbrains.kmpapp.data.groups.Invite
import com.jetbrains.kmpapp.data.lists.TodoList
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    groupId: String,
    groupName: String,
    navigateBack: () -> Unit,
    navigateToListDetail: (String) -> Unit,
    navigateToLinkEmail: () -> Unit,
) {
    val viewModel = koinViewModel<GroupDetailViewModel>()
    val group by viewModel.group.collectAsStateWithLifecycle()
    val groupLists by viewModel.groupLists.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboardManager = LocalClipboardManager.current

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showInviteDialog by remember { mutableStateOf(false) }
    var currentInvite by remember { mutableStateOf<Invite?>(null) }
    var showTransferDialog by remember { mutableStateOf(false) }
    var showAddListDialog by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(groupId) { viewModel.load(groupId) }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is GroupDetailUiEvent.ShowInvite -> {
                    currentInvite = event.invite
                    showInviteDialog = true
                }
                is GroupDetailUiEvent.NavigateToLinkEmail -> navigateToLinkEmail()
                is GroupDetailUiEvent.GroupDeleted -> navigateBack()
                is GroupDetailUiEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(group?.name ?: groupName) },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                },
                actions = {
                    val isOwner = group?.role == "owner"
                    if (isOwner) {
                        Box {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(Icons.Default.MoreVert, "Действия")
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false },
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Пригласить") },
                                    onClick = {
                                        menuExpanded = false
                                        viewModel.createInvite()
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text("Передать роль владельца") },
                                    onClick = {
                                        menuExpanded = false
                                        showTransferDialog = true
                                    },
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "Удалить группу",
                                            color = MaterialTheme.colorScheme.error,
                                        )
                                    },
                                    onClick = {
                                        menuExpanded = false
                                        showDeleteConfirm = true
                                    },
                                )
                            }
                        }
                    } else {
                        IconButton(onClick = {
                            val currentUserId = group?.members
                                ?.firstOrNull { it.role != "owner" }?.userId
                            // Self-leave: remove own userId
                            group?.members
                                ?.firstOrNull { it.role == "member" }
                                ?.let { viewModel.removeMember(it.userId) }
                        }) {
                            Text("Выйти", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddListDialog = true }) {
                Icon(Icons.Default.Add, "Добавить список в группу")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = WindowInsets.safeDrawing.asPaddingValues(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            // Members section
            val members = group?.members
            if (!members.isNullOrEmpty()) {
                item {
                    Text(
                        text = "Участники",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
                items(members, key = { "member_${it.userId}" }) { member ->
                    MemberRow(
                        member = member,
                        isOwner = group?.role == "owner",
                        onRemove = { viewModel.removeMember(member.userId) },
                    )
                }
                item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }
            }

            // Lists section
            item {
                Text(
                    text = "Списки группы",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
            if (groupLists.isEmpty()) {
                item {
                    Text(
                        text = "Нет списков. Нажмите + чтобы создать.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            } else {
                items(groupLists, key = { "list_${it.id}" }) { list ->
                    GroupListCard(list = list, onClick = { navigateToListDetail(list.id) })
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    // Delete confirmation dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Удалить группу?") },
            text = { Text("Все списки группы будут удалены. Это действие необратимо.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.deleteGroup()
                    },
                ) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Отмена") }
            },
        )
    }

    // Invite dialog
    if (showInviteDialog && currentInvite != null) {
        val deepLink = "familytodo://invite/${currentInvite!!.token}"
        AlertDialog(
            onDismissRequest = { showInviteDialog = false },
            title = { Text("Ссылка-приглашение") },
            text = {
                Column {
                    Text(
                        text = deepLink,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Действует до: ${currentInvite!!.expiresAt.take(10)}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    clipboardManager.setText(AnnotatedString(deepLink))
                    showInviteDialog = false
                }) {
                    Text("Скопировать")
                }
            },
            dismissButton = {
                TextButton(onClick = { showInviteDialog = false }) { Text("Закрыть") }
            },
        )
    }

    // Transfer ownership dialog
    if (showTransferDialog) {
        val nonOwnerMembers = group?.members?.filter { it.role != "owner" } ?: emptyList()
        AlertDialog(
            onDismissRequest = { showTransferDialog = false },
            title = { Text("Передать роль владельца") },
            text = {
                if (nonOwnerMembers.isEmpty()) {
                    Text("Нет участников для передачи роли")
                } else {
                    Column {
                        nonOwnerMembers.forEach { member ->
                            TextButton(
                                onClick = {
                                    viewModel.transferOwnership(member.userId)
                                    showTransferDialog = false
                                },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(member.displayName)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showTransferDialog = false }) { Text("Отмена") }
            },
        )
    }

    // Add list dialog
    if (showAddListDialog) {
        var listTitle by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddListDialog = false },
            title = { Text("Новый список в группе") },
            text = {
                OutlinedTextField(
                    value = listTitle,
                    onValueChange = { listTitle = it },
                    label = { Text("Название") },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.createListInGroup(listTitle.ifBlank { "Новый список" })
                    showAddListDialog = false
                }) {
                    Text("Создать")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddListDialog = false }) { Text("Отмена") }
            },
        )
    }
}

@Composable
private fun MemberRow(
    member: GroupMember,
    isOwner: Boolean,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = member.displayName, style = MaterialTheme.typography.bodyLarge)
        }
        SuggestionChip(
            onClick = {},
            label = { Text(if (member.role == "owner") "Владелец" else "Участник") },
        )
        if (isOwner && member.role != "owner") {
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Delete,
                    "Удалить участника",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun GroupListCard(
    list: TodoList,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = list.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = list.type,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
