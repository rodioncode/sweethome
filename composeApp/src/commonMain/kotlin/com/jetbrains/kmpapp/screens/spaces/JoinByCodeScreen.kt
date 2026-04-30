package com.jetbrains.kmpapp.screens.spaces

import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetbrains.kmpapp.data.groups.EmailRequiredException
import com.jetbrains.kmpapp.data.groups.GroupsRepository
import com.jetbrains.kmpapp.data.groups.InvalidInviteException
import com.jetbrains.kmpapp.data.groups.InviteExpiredException
import com.jetbrains.kmpapp.ui.PrimaryGreen
import com.jetbrains.kmpapp.ui.PrimaryGreenLight
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

private const val CODE_LENGTH = 6

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinByCodeScreen(
    prefillCode: String = "",
    onSuccess: (groupId: String, groupName: String) -> Unit,
    onEmailRequired: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val groupsRepository: GroupsRepository = koinInject()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var code by remember { mutableStateOf(prefillCode.take(CODE_LENGTH).uppercase()) }
    var isLoading by remember { mutableStateOf(false) }

    fun submit() {
        val trimmed = code.trim()
        if (trimmed.length < CODE_LENGTH) return
        isLoading = true
        coroutineScope.launch {
            groupsRepository.joinByCode(trimmed)
                .onSuccess { workspace ->
                    onSuccess(workspace.id, workspace.title)
                }
                .onFailure { err ->
                    isLoading = false
                    when (err) {
                        is EmailRequiredException -> onEmailRequired()
                        is InvalidInviteException, is InviteExpiredException ->
                            snackbarHostState.showSnackbar("Код недействителен или истёк")
                        else ->
                            snackbarHostState.showSnackbar(err.message ?: "Ошибка при вступлении")
                    }
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Войти по коду") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.weight(0.15f))

            // Chain link icon
            Surface(
                modifier = Modifier.size(72.dp),
                shape = CircleShape,
                color = PrimaryGreenLight.copy(alpha = 0.3f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "🔗", fontSize = 32.sp)
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Вступить в пространство",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Введите код из 6 символов, который\nподелился с вами владелец пространства",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(32.dp))

            // 6-cell code input
            CodeInput(
                code = code,
                onCodeChange = { newCode ->
                    code = newCode.uppercase().take(CODE_LENGTH)
                    if (newCode.length == CODE_LENGTH) {
                        submit()
                    }
                },
                enabled = !isLoading,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Код чувствителен к регистру",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(16.dp))

            // Paste from clipboard
            TextButton(onClick = { /* TODO: clipboard paste */ }) {
                Text("📋 Вставить из буфера", color = PrimaryGreen)
            }

            Spacer(Modifier.height(16.dp))

            // Divider with "или"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text = "или",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(16.dp))

            // Scan QR code button
            OutlinedButton(
                onClick = { /* TODO: QR scanner */ },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("📷 Сканировать QR-код")
            }

            Spacer(Modifier.weight(0.15f))

            // Submit button at bottom
            Button(
                onClick = { submit() },
                enabled = !isLoading && code.length == CODE_LENGTH,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                shape = RoundedCornerShape(12.dp),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White,
                    )
                } else {
                    Text("Вступить", color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun CodeInput(
    code: String,
    onCodeChange: (String) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }

    Box(modifier = modifier) {
        // Hidden text field that captures input
        BasicTextField(
            value = code,
            onValueChange = { value ->
                val filtered = value.filter { it.isLetterOrDigit() }.take(CODE_LENGTH).uppercase()
                onCodeChange(filtered)
            },
            enabled = enabled,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done,
            ),
            textStyle = TextStyle(color = Color.Transparent, fontSize = 1.sp),
            modifier = Modifier
                .focusRequester(focusRequester)
                .fillMaxWidth()
                .height(1.dp),
        )

        // Visual cells
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            for (i in 0 until CODE_LENGTH) {
                val char = code.getOrNull(i)?.toString() ?: ""
                val isFocused = i == code.length && code.length < CODE_LENGTH

                if (i > 0) Spacer(Modifier.width(8.dp))

                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .border(
                            width = if (isFocused) 2.dp else 1.dp,
                            color = if (isFocused) PrimaryGreen
                            else if (char.isNotEmpty()) MaterialTheme.colorScheme.outline
                            else MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(8.dp),
                        ),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    onClick = { focusRequester.requestFocus() },
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = char,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }
}
