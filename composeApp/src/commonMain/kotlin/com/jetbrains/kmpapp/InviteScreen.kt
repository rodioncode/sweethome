package com.jetbrains.kmpapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import com.jetbrains.kmpapp.data.groups.EmailRequiredException
import com.jetbrains.kmpapp.data.groups.GroupsRepository
import com.jetbrains.kmpapp.data.groups.InvalidInviteException
import org.koin.compose.koinInject

@Composable
fun InviteScreen(
    token: String,
    onSuccess: (groupId: String, groupName: String) -> Unit,
    onEmailRequired: () -> Unit,
    onError: () -> Unit,
) {
    val groupsRepository: GroupsRepository = koinInject()
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(token) {
        groupsRepository.acceptInvite(token)
            .onSuccess { response ->
                val group = groupsRepository.groups.value.find { it.id == response.groupId }
                onSuccess(response.groupId, group?.name ?: "Группа")
            }
            .onFailure { err ->
                when (err) {
                    is EmailRequiredException -> onEmailRequired()
                    is InvalidInviteException -> errorMessage = "Приглашение недействительно или истекло"
                    else -> errorMessage = err.message ?: "Ошибка при принятии приглашения"
                }
            }
    }

    errorMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = onError,
            title = { Text("Ошибка") },
            text = { Text(msg) },
            confirmButton = {
                TextButton(onClick = onError) { Text("ОК") }
            },
        )
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (errorMessage == null) {
            CircularProgressIndicator()
            Text(
                text = "Принимаем приглашение...",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
