package com.jetbrains.kmpapp.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalUriHandler
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jetbrains.kmpapp.ui.BackgroundWarm
import com.jetbrains.kmpapp.ui.DividerColor
import com.jetbrains.kmpapp.ui.PrimaryGreen
import com.jetbrains.kmpapp.ui.SurfaceVariantCream
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
            onRegister = { email, password, displayName, accepted ->
                viewModel.register(email, password, displayName, accepted)
            },
            onNavigateBack = onNavigateBack,
            uiState = uiState,
            onClearError = { viewModel.clearError() },
        )
    }
}

@Composable
private fun RegisterContent(
    onRegister: (String, String, String, Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    uiState: AuthUiState,
    onClearError: () -> Unit,
) {
    var displayName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var termsAccepted by remember { mutableStateOf(false) }

    val canSubmit = displayName.isNotBlank()
        && email.isNotBlank()
        && password.length >= 8
        && termsAccepted
        && uiState !is AuthUiState.Loading

    Scaffold(containerColor = BackgroundWarm) { paddingValues ->
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
                    color = PrimaryGreen,
                    shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
                )
                .padding(horizontal = 24.dp, vertical = 40.dp),
        ) {
            // Decorative circles
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .align(Alignment.TopEnd)
                    .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(50)),
            )

            // Back button
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .size(36.dp)
                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(18.dp)),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("🏠", fontSize = 28.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Создать аккаунт",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Это займёт меньше минуты",
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
                .padding(top = 28.dp),
        ) {
            listOf(
                Triple("Имя", displayName, { v: String -> displayName = v; onClearError() }),
                Triple("Email", email, { v: String -> email = v; onClearError() }),
            ).forEach { (label, value, onChange) ->
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp),
                )
                OutlinedTextField(
                    value = value,
                    onValueChange = onChange,
                    placeholder = {
                        Text(
                            text = if (label == "Имя") "Как вас зовут?" else "your@email.com",
                            color = Color(0xFFBDBDBD),
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (label == "Email") KeyboardType.Email else KeyboardType.Text,
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = DividerColor,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                    ),
                )
                Spacer(Modifier.height(16.dp))
            }

            // Password
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

            Spacer(Modifier.height(16.dp))

            // ToS checkbox
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { termsAccepted = !termsAccepted },
                color = SurfaceVariantCream,
                shape = RoundedCornerShape(12.dp),
            ) {
                Row(
                    modifier = Modifier.padding(14.dp, 14.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .background(
                                color = if (termsAccepted) PrimaryGreen else Color.White,
                                shape = RoundedCornerShape(6.dp),
                            )
                            .then(
                                if (!termsAccepted) Modifier.background(
                                    Color.Transparent,
                                    RoundedCornerShape(6.dp),
                                ) else Modifier
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (!termsAccepted) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .background(Color.White, RoundedCornerShape(6.dp))
                                    .then(
                                        Modifier.background(Color.Transparent)
                                    ),
                            )
                            // Border
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.White),
                            )
                        }
                        if (termsAccepted) {
                            Text("✓", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    Column {
                        val uriHandler = LocalUriHandler.current
                        Text(
                            text = "Принимаю ",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Row {
                            Text(
                                text = "условия использования",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGreen,
                                modifier = Modifier.clickable {
                                    uriHandler.openUri("https://sweethome.app/terms")
                                },
                            )
                            Text(
                                text = " и ",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = "политику конфиденциальности",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGreen,
                                modifier = Modifier.clickable {
                                    uriHandler.openUri("https://sweethome.app/privacy")
                                },
                            )
                        }
                    }
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

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { onRegister(email, password, displayName, termsAccepted) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                enabled = canSubmit,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreen,
                    disabledContainerColor = DividerColor,
                ),
            ) {
                if (uiState is AuthUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        text = "Зарегистрироваться",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

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

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Уже есть аккаунт? ",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(
                    onClick = onNavigateBack,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                ) {
                    Text(
                        text = "Войти",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen,
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
    }
}
