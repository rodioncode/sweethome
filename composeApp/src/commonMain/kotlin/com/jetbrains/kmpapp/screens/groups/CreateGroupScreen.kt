package com.jetbrains.kmpapp.screens.groups

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(
    navigateBack: () -> Unit,
    onGroupCreated: (groupId: String) -> Unit,
) {
    val viewModel = koinViewModel<GroupsViewModel>()
    val uiState by viewModel.uiState.collectAsState()

    var name by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }

    val isLoading = uiState is GroupsUiState.Loading

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Новая группа") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = null
                },
                label = { Text("Название группы") },
                placeholder = { Text("Например: Семья Ивановых") },
                isError = nameError != null,
                supportingText = nameError?.let { { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
            )

            // Ошибка от API
            if (uiState is GroupsUiState.Error) {
                Text(
                    (uiState as GroupsUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = "Введите название группы"
                        return@Button
                    }
                    viewModel.createGroup(name.trim()) {
                        // После создания группы — группа добавилась в список,
                        // берём последнюю из репозитория
                        val groups = viewModel.groups.value
                        val created = groups.lastOrNull()
                        if (created != null) onGroupCreated(created.id)
                        else navigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text("Создать")
                }
            }
        }
    }
}
