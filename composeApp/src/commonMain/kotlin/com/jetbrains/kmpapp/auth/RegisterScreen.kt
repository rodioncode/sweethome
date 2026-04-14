package com.jetbrains.kmpapp.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jetbrains.kmpapp.ui.PrimaryGreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val viewModel = koinViewModel<AuthViewModel>()
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (authState is AuthState.Authenticated && (authState as AuthState.Authenticated).isGuest == false) {
        LaunchedEffect(Unit) { onRegisterSuccess() }
    } else {
        RegisterContent(
            onRegister = { email, password, displayName ->
                viewModel.register(email, password, displayName)
            },
            onNavigateBack = onNavigateBack,
            uiState = uiState,
            onClearError = { viewModel.clearError() },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegisterContent(
    onRegister: (String, String, String) -> Unit,
    onNavigateBack: () -> Unit,
    uiState: AuthUiState,
    onClearError: () -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var termsAccepted by remember { mutableStateOf(false) }

    val passwordMismatch = confirmPassword.isNotEmpty() && password != confirmPassword
    val canSubmit = email.isNotBlank()
        && password.isNotBlank()
        && confirmPassword.isNotBlank()
        && displayName.isNotBlank()
        && !passwordMismatch
        && termsAccepted
        && uiState !is AuthUiState.Loading

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                }
            )
        },
        modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars),
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(8.dp))

            // Title block
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = "Создать аккаунт",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Заполните данные ниже",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(28.dp))

            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("Ваше имя") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                placeholder = { Text("your@email.com") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = MaterialTheme.shapes.medium,
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Пароль") },
                placeholder = { Text("Минимум 8 символов") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = MaterialTheme.shapes.medium,
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Повторите пароль") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = passwordMismatch,
                supportingText = if (passwordMismatch) {
                    { Text("Пароли не совпадают") }
                } else null,
                shape = MaterialTheme.shapes.medium,
            )

            Spacer(Modifier.height(16.dp))

            // Terms checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Checkbox(
                    checked = termsAccepted,
                    onCheckedChange = { termsAccepted = it },
                )
                Text(
                    text = "Согласен с ",
                    style = MaterialTheme.typography.bodyMedium,
                )
                TextButton(
                    onClick = { /* open terms */ },
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                ) {
                    Text(
                        text = "условиями использования",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PrimaryGreen,
                    )
                }
            }

            if (uiState is AuthUiState.Error) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = uiState.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = { onRegister(email, password, displayName) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                enabled = canSubmit,
                shape = MaterialTheme.shapes.medium,
            ) {
                if (uiState is AuthUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Зарегистрироваться", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(24.dp))

            DividerWithText("или")

            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick = { /* Coming soon */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = MaterialTheme.shapes.medium,
            ) {
                Text("G", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(end = 8.dp))
                Text("Зарегистрироваться через Google")
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = { /* Coming soon */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = MaterialTheme.shapes.medium,
            ) {
                Text("◆", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(end = 8.dp))
                Text("Зарегистрироваться через Apple")
            }

            Spacer(Modifier.height(20.dp))

            TextButton(onClick = onNavigateBack) {
                Text("Уже есть аккаунт? ")
                Text("Войти", fontWeight = FontWeight.SemiBold, color = PrimaryGreen)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
