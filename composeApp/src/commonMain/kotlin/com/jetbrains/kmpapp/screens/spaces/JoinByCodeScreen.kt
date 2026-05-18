package com.jetbrains.kmpapp.screens.spaces

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.jetbrains.kmpapp.ui.LocalCozyExtraColors
import com.jetbrains.kmpapp.ui.LocalCozyShapes
import com.jetbrains.kmpapp.ui.components.CozyCard
import com.jetbrains.kmpapp.ui.components.CozyTopBar
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

private const val CODE_LENGTH = 6

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
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val extras = LocalCozyExtraColors.current
    val shapes = LocalCozyShapes.current

    fun submit() {
        val trimmed = code.trim()
        if (trimmed.length < CODE_LENGTH) return
        isLoading = true
        errorMessage = null
        coroutineScope.launch {
            groupsRepository.joinByCode(trimmed)
                .onSuccess { workspace ->
                    onSuccess(workspace.id, workspace.title)
                }
                .onFailure { err ->
                    isLoading = false
                    when (err) {
                        is EmailRequiredException -> onEmailRequired()
                        is InvalidInviteException, is InviteExpiredException -> {
                            errorMessage = "Код недействителен или истёк"
                            snackbarHostState.showSnackbar(errorMessage!!)
                        }
                        else -> {
                            errorMessage = err.message ?: "Ошибка при вступлении"
                            snackbarHostState.showSnackbar(errorMessage!!)
                        }
                    }
                }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            CozyTopBar(onBack = onNavigateBack)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.Start,
            ) {
                Spacer(Modifier.height(12.dp))

                // Hero chain icon (72dp tile)
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(shapes.cardLarge)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("🔗", fontSize = 32.sp)
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "Введи код\nприглашения",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 28.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "6 символов, которые тебе дали члены группы.",
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.height(24.dp))

                // 6-cell code input
                CodeInput(
                    code = code,
                    onCodeChange = { newCode ->
                        code = newCode.uppercase().take(CODE_LENGTH)
                        errorMessage = null
                        if (code.length == CODE_LENGTH) {
                            submit()
                        }
                    },
                    enabled = !isLoading,
                    hasError = errorMessage != null,
                )

                if (errorMessage != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        fontSize = 12.sp,
                        color = extras.coral,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Submit button
                val enabled = !isLoading && code.length == CODE_LENGTH
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(shapes.button)
                        .background(
                            if (enabled) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant
                        )
                        .clickable(enabled = enabled) { submit() },
                    contentAlignment = Alignment.Center,
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Text(
                            text = "Вступить",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // QR alt option
                CozyCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { /* TODO: QR scanner */ },
                    background = extras.surfaceSoft,
                    contentPadding = 14.dp,
                    radius = 14.dp,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text("📷", fontSize = 24.sp)
                        Text(
                            text = "Или отсканируй QR-код",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = "→",
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
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
    hasError: Boolean,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    val shapes = LocalCozyShapes.current
    val extras = LocalCozyExtraColors.current

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Box(modifier = modifier.fillMaxWidth()) {
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
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            for (i in 0 until CODE_LENGTH) {
                val char = code.getOrNull(i)?.toString() ?: ""
                val isFocused = i == code.length && code.length < CODE_LENGTH
                val borderColor = when {
                    hasError -> extras.coral
                    isFocused -> MaterialTheme.colorScheme.primary
                    char.isNotEmpty() -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.outlineVariant
                }

                if (i > 0) Spacer(Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clip(shapes.chip)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            width = if (isFocused || hasError) 2.dp else 1.dp,
                            color = borderColor,
                            shape = shapes.chip,
                        )
                        .clickable { focusRequester.requestFocus() },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = char,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
        }
    }
}
