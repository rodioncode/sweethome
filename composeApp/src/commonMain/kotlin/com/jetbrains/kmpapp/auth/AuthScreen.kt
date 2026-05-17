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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.Scaffold
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
import com.jetbrains.kmpapp.ui.LocalCozyExtraColors
import com.jetbrains.kmpapp.ui.LocalCozyShapes
import com.jetbrains.kmpapp.ui.LocalCozySpacing
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

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { paddingValues ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(paddingValues),
    ) {
        // Green hero header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
                )
                .padding(horizontal = LocalCozySpacing.current.xxl, vertical = LocalCozySpacing.current.xxxl),
        ) {
            // Decorative circles
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .align(Alignment.TopEnd)
                    .background(Color.White.copy(alpha = 0.06f), CircleShape),
            )
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .align(Alignment.TopEnd)
                    .background(Color.White.copy(alpha = 0.08f), CircleShape),
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("🏠", fontSize = 28.sp)
                Spacer(Modifier.height(LocalCozySpacing.current.xs))
                Text(
                    text = "SweetHome",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Spacer(Modifier.height(LocalCozySpacing.current.xxs))
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
                .padding(horizontal = LocalCozySpacing.current.xxl)
                .padding(top = LocalCozySpacing.current.xxxl),
        ) {
            // Email field
            Text(
                text = "Email",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = LocalCozySpacing.current.xs),
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; onClearError() },
                placeholder = { Text("your@email.com", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = MaterialTheme.shapes.small,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                ),
            )

            Spacer(Modifier.height(LocalCozySpacing.current.lg))

            // Password field
            Text(
                text = "Пароль",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = LocalCozySpacing.current.xs),
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; onClearError() },
                placeholder = { Text("••••••••", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = MaterialTheme.shapes.small,
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Text(
                            text = if (showPassword) "🙈" else "👁",
                            fontSize = 16.sp,
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                ),
            )
            Text(
                text = "Минимум 8 символов",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = LocalCozySpacing.current.xxs, start = LocalCozySpacing.current.xxs),
            )

            if (uiState is AuthUiState.Error) {
                Spacer(Modifier.height(LocalCozySpacing.current.xs))
                Text(
                    text = uiState.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(Modifier.height(LocalCozySpacing.current.xxl))

            Button(
                onClick = { onLogin(email, password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(LocalCozySpacing.current.huge),
                enabled = uiState !is AuthUiState.Loading,
                shape = LocalCozyShapes.current.card,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                if (uiState is AuthUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(LocalCozySpacing.current.xxl),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Войти", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(LocalCozySpacing.current.sm))

            TextButton(
                onClick = onForgotPassword,
                modifier = Modifier.align(Alignment.End),
            ) {
                Text("Забыли пароль?", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(Modifier.height(LocalCozySpacing.current.sm))

            DividerWithText("или")

            Spacer(Modifier.height(LocalCozySpacing.current.lg))

            // Google
            Surface(
                onClick = { /* coming soon */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(LocalCozySpacing.current.huge),
                shape = LocalCozyShapes.current.button,
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    MaterialTheme.colorScheme.outlineVariant,
                ),
                shadowElevation = 2.dp,
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("G", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.width(LocalCozySpacing.current.sm))
                    Text(
                        "Войти через Google",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            Spacer(Modifier.height(LocalCozySpacing.current.sm))

            // Apple
            Surface(
                onClick = { /* coming soon */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(LocalCozySpacing.current.huge),
                shape = LocalCozyShapes.current.button,
                color = Color(0xFF1D1D1F),
                shadowElevation = 3.dp,
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("🍎", fontSize = 18.sp)
                    Spacer(Modifier.width(LocalCozySpacing.current.sm))
                    Text("Войти через Apple", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(Modifier.height(LocalCozySpacing.current.xxxl))

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
                        color = MaterialTheme.colorScheme.primary,
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

            Spacer(Modifier.height(LocalCozySpacing.current.xxl))
        }
    }
    }
}

@Composable
internal fun DividerWithText(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
        Text(
            text = "  $text  ",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun GuestAuthenticatedContent(
    onLinkEmail: () -> Unit,
    onLogout: () -> Unit,
) {
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { paddingValues ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(LocalCozySpacing.current.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Вы вошли как гость", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(LocalCozySpacing.current.xxl))
        Button(
            onClick = onLinkEmail,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Text("Привязать email и пароль")
        }
        Spacer(Modifier.height(LocalCozySpacing.current.lg))
        TextButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
            Text("Выйти", color = MaterialTheme.colorScheme.error)
        }
    }
    }
}
