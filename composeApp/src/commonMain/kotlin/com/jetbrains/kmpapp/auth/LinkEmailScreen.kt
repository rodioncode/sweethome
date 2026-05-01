package com.jetbrains.kmpapp.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LinkEmailScreen(
    onLinkSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val viewModel = koinViewModel<AuthViewModel>()
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (authState is AuthState.Authenticated && (authState as AuthState.Authenticated).isGuest == false) {
        LaunchedEffect(Unit) { onLinkSuccess() }
    } else {
        LinkEmailContent(
            onLink = { email, password, displayName ->
                viewModel.linkEmail(email, password, displayName)
            },
            onNavigateBack = onNavigateBack,
            uiState = uiState,
            onClearError = { viewModel.clearError() },
        )
    }
}

@Composable
private fun LinkEmailContent(
    onLink: (String, String, String) -> Unit,
    onNavigateBack: () -> Unit,
    uiState: AuthUiState,
    onClearError: () -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars),
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                }
            }
            Text(
                "Привяжите email и пароль к гостевому аккаунту",
                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("Имя") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Пароль") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            )
            Spacer(Modifier.height(24.dp))

            when (uiState) {
                is AuthUiState.Error -> {
                    Text(
                        (uiState as AuthUiState.Error).message,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.height(8.dp))
                }
                else -> {}
            }

            Button(
                onClick = { onLink(email, password, displayName) },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is AuthUiState.Loading,
            ) {
                if (uiState is AuthUiState.Loading) {
                    CircularProgressIndicator(Modifier.height(24.dp))
                } else {
                    Text("Привязать")
                }
            }
        }
    }
}
