package com.jetbrains.kmpapp.screens.groups

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jetbrains.kmpapp.data.groups.GroupDTO
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(
    navigateToGroupDetail: (groupId: String) -> Unit,
    navigateToCreateGroup: () -> Unit,
) {
    val viewModel = koinViewModel<GroupsViewModel>()
    val groups by viewModel.groups.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val error by viewModel.error.collectAsState()

    var showInviteDialog by remember { mutableStateOf(false) }
    var inviteTokenInput by remember { mutableStateOf("") }

    // Диалог принятия приглашения по токену
    if (showInviteDialog) {
        AlertDialog(
            onDismissRequest = { showInviteDialog = false },
            title = { Text("Войти по приглашению") },
            text = {
                OutlinedTextField(
                    value = inviteTokenInput,
                    onValueChange = { inviteTokenInput = it },
                    label = { Text("Токен приглашения") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (inviteTokenInput.isNotBlank()) {
                            viewModel.acceptInvite(inviteTokenInput.trim()) { groupId ->
                                showInviteDialog = false
                                inviteTokenInput = ""
                                navigateToGroupDetail(groupId)
                            }
                        }
                    },
                ) { Text("Войти") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showInviteDialog = false
                    inviteTokenInput = ""
                }) { Text("Отмена") }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Группы") },
                actions = {
                    IconButton(onClick = { showInviteDialog = true }) {
                        Icon(Icons.Default.Group, contentDescription = "Войти по инвайту")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = navigateToCreateGroup) {
                Icon(Icons.Default.Add, contentDescription = "Создать группу")
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            when {
                uiState is GroupsUiState.Loading && groups.isEmpty() -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                groups.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            Icons.Default.Group,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            "У вас пока нет групп",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            "Создайте группу или войдите по приглашению",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp),
                    ) {
                        items(groups) { group ->
                            GroupItem(
                                group = group,
                                onClick = { navigateToGroupDetail(group.id) },
                            )
                        }
                    }
                }
            }

            // Ошибка
            error?.let { msg ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) { Text("OK") }
                    },
                ) { Text(msg) }
            }
        }
    }
}

@Composable
private fun GroupItem(
    group: GroupDTO,
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = {
            Text(group.name, fontWeight = FontWeight.Medium)
        },
        supportingContent = {
            Text(
                if (group.role == "owner") "Владелец" else "Участник",
                color = if (group.role == "owner")
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        leadingContent = {
            Icon(Icons.Default.Group, contentDescription = null)
        },
        modifier = Modifier.clickable(onClick = onClick),
    )
    HorizontalDivider()
}
