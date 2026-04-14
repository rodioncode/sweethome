package com.jetbrains.kmpapp.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jetbrains.kmpapp.ui.PrimaryGreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToLinkEmail: () -> Unit,
) {
    val viewModel = koinViewModel<AuthViewModel>()
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = authState) {
        is AuthState.Authenticated -> {
            if (state.isGuest) {
                GuestAuthenticatedContent(
                    onLinkEmail = onNavigateToLinkEmail,
                    onLogout = { viewModel.logout() },
                )
            } else {
                LaunchedEffect(Unit) { onAuthSuccess() }
            }
        }
        else -> {
            LoginContent(
                onLogin = { email, password -> viewModel.login(email, password) },
                onNavigateToRegister = onNavigateToRegister,
                onGuestLogin = { viewModel.loginAsGuest() },
                uiState = uiState,
                onClearError = { viewModel.clearError() },
            )
        }
    }
}

@Composable
private fun LoginContent(
    onLogin: (String, String) -> Unit,
    onNavigateToRegister: () -> Unit,
    onGuestLogin: () -> Unit,
    uiState: AuthUiState,
    onClearError: () -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(48.dp))

        // Hero section
        HeroSection(
            subtitle = "Войдите в свой аккаунт",
        )

        Spacer(Modifier.height(40.dp))

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

        Spacer(Modifier.height(8.dp))

        if (uiState is AuthUiState.Error) {
            Text(
                text = uiState.message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { onLogin(email, password) },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            enabled = uiState !is AuthUiState.Loading,
            shape = MaterialTheme.shapes.medium,
        ) {
            if (uiState is AuthUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                )
            } else {
                Text("Войти", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
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
            Text("Войти через Google")
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
            Text("Войти через Apple")
        }

        Spacer(Modifier.height(24.dp))

        TextButton(onClick = onNavigateToRegister) {
            Text("Нет аккаунта? ")
            Text("Зарегистрироваться", fontWeight = FontWeight.SemiBold, color = PrimaryGreen)
        }

        Spacer(Modifier.height(8.dp))

        TextButton(onClick = onGuestLogin) {
            Text(
                "Продолжить без аккаунта",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
internal fun HeroSection(subtitle: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Concentric circles with house icon
        Box(contentAlignment = Alignment.Center) {
            Surface(
                modifier = Modifier.size(88.dp),
                shape = CircleShape,
                color = PrimaryGreen.copy(alpha = 0.12f),
            ) {}
            Surface(
                modifier = Modifier.size(66.dp),
                shape = CircleShape,
                color = PrimaryGreen.copy(alpha = 0.18f),
            ) {}
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = PrimaryGreen.copy(alpha = 0.25f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("🏠", fontSize = 22.sp)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "SweetHome",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
internal fun DividerWithText(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text(
            text = "  $text  ",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun GuestAuthenticatedContent(
    onLinkEmail: () -> Unit,
    onLogout: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Вы вошли как гость", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onLinkEmail, modifier = Modifier.fillMaxWidth()) {
            Text("Привязать email и пароль")
        }
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
            Text("Выйти")
        }
    }
}
