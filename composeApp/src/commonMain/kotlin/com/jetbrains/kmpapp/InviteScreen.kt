package com.jetbrains.kmpapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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

    LaunchedEffect(token) {
        groupsRepository.acceptInvite(token)
            .onSuccess { response ->
                val group = groupsRepository.groups.value.find { it.id == response.groupId }
                onSuccess(response.groupId, group?.name ?: "Группа")
            }
            .onFailure { err ->
                when (err) {
                    is EmailRequiredException -> onEmailRequired()
                    else -> onError()
                }
            }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
        Text(
            text = "Принимаем приглашение...",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
