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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jetbrains.kmpapp.ui.BackgroundWarm
import com.jetbrains.kmpapp.ui.DividerColor
import com.jetbrains.kmpapp.ui.PrimaryGreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToLinkEmail: () -> Unit,
    onNavigateToPasswordReset: () -> Unit = {},
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
                onForgotPassword = onNavigateToPasswordReset,
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
    onForgotPassword: () -> Unit = {},
    uiState: AuthUiState,
    onClearError: () -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWarm)
            .verticalScroll(rememberScrollState()),
    ) {
        // Green hero header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = PrimaryGreen,
                    shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
                )
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 24.dp, vertical = 40.dp),
        ) {
            // Decorative circles
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .align(Alignment.TopEnd)
                    .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(50)),
            )
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.TopEnd)
                    .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(50)),
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("🏠", fontSize = 28.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "SweetHome",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Войдите в свой аккаунт",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.75f),
                )
            }
        }

        // Form
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 32.dp),
        ) {
            // Email field
            Text(
                text = "Email",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp),
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; onClearError() },
                placeholder = { Text("your@email.com", color = Color(0xFFBDBDBD)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGreen,
                    unfocusedBorderColor = DividerColor,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                ),
            )

            Spacer(Modifier.height(16.dp))

            // Password field
            Text(
                text = "Пароль",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp),
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; onClearError() },
                placeholder = { Text("••••••••", color = Color(0xFFBDBDBD)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Text(
                            text = if (showPassword) "🙈" else "👁",
                            fontSize = 16.sp,
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGreen,
                    unfocusedBorderColor = DividerColor,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                ),
            )
            Text(
                text = "Минимум 8 символов",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp),
            )

            if (uiState is AuthUiState.Error) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = uiState.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { onLogin(email, password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                enabled = uiState !is AuthUiState.Loading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
            ) {
                if (uiState is AuthUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Войти", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(12.dp))

            TextButton(
                onClick = onForgotPassword,
                modifier = Modifier.align(Alignment.End),
            ) {
                Text("Забыли пароль?", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(Modifier.height(12.dp))

            DividerWithText("или")

            Spacer(Modifier.height(16.dp))

            // Google
            Surface(
                onClick = { /* coming soon */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFDADCE0)),
                shadowElevation = 2.dp,
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("G", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D1D1F))
                    Spacer(Modifier.width(10.dp))
                    Text("Войти через Google", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D1D1F))
                }
            }

            Spacer(Modifier.height(12.dp))

            // Apple
            Surface(
                onClick = { /* coming soon */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF1D1D1F),
                shadowElevation = 3.dp,
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("🍎", fontSize = 18.sp)
                    Spacer(Modifier.width(10.dp))
                    Text("Войти через Apple", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(Modifier.height(28.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Нет аккаунта? ",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(
                    onClick = onNavigateToRegister,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                ) {
                    Text(
                        text = "Зарегистрироваться",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen,
                    )
                }
            }

            TextButton(
                onClick = onGuestLogin,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Продолжить без аккаунта",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
internal fun DividerWithText(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = DividerColor)
        Text(
            text = "  $text  ",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = DividerColor)
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
            .background(BackgroundWarm)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Вы вошли как гость", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onLinkEmail,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
        ) {
            Text("Привязать email и пароль")
        }
        Spacer(Modifier.height(16.dp))
        TextButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
            Text("Выйти", color = MaterialTheme.colorScheme.error)
        }
    }
}
