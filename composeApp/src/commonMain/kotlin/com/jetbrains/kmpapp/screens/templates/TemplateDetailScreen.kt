package com.jetbrains.kmpapp.screens.templates

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jetbrains.kmpapp.data.groups.Group
import com.jetbrains.kmpapp.data.templates.ListTemplateDetail
import com.jetbrains.kmpapp.data.templates.TemplateListItem
import com.jetbrains.kmpapp.ui.SweetHomeShapes
import com.jetbrains.kmpapp.ui.SweetHomeSpacing
import com.jetbrains.kmpapp.ui.listColorForType
import com.jetbrains.kmpapp.ui.listEmojiForType
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TemplateDetailScreen(
    templateId: String,
    scope: String = "shopping",
    titleHint: String = "",
    navigateBack: () -> Unit,
    navigateToLists: () -> Unit = {},
) {
    val viewModel = koinViewModel<TemplateDetailViewModel>()
    val detail by viewModel.detail.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val workspaces by viewModel.workspaces.collectAsStateWithLifecycle()
    val isMine by viewModel.isMine.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var showUseDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(templateId) { viewModel.load(templateId) }
    LaunchedEffect(viewModel.error) {
        viewModel.error.collect { msg ->
            msg?.let {
                snackbarHostState.showSnackbar(it)
                viewModel.clearError()
            }
        }
    }

    val accent = listColorForType(detail?.scope ?: scope, isDark = false)
    val emoji = listEmojiForType(detail?.scope ?: scope)
    val title = detail?.title?.takeIf { it.isNotBlank() } ?: titleHint.takeIf { it.isNotBlank() } ?: "Шаблон"

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Hero header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(accent),
            ) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .align(Alignment.TopEnd)
                        .background(Color.White.copy(alpha = 0.06f), CircleShape),
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = SweetHomeSpacing.md)
                        .padding(top = SweetHomeSpacing.sm, bottom = SweetHomeSpacing.lg),
                ) {
                    // Top action bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        IconBadge(emoji = "←", onClick = navigateBack)
                        Row(horizontalArrangement = Arrangement.spacedBy(SweetHomeSpacing.xs)) {
                            detail?.let { d ->
                                IconBadge(
                                    emoji = if (d.isFavorite) "★" else "☆",
                                    onClick = { viewModel.toggleFavorite() },
                                )
                            }
                            // Publication / delete actions live in PublicationActionsBar below.
                        }
                    }
                    Spacer(Modifier.height(SweetHomeSpacing.md))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(Color.White.copy(alpha = 0.18f), CircleShape),
                            contentAlignment = Alignment.Center,
                        ) { Text(emoji, fontSize = 28.sp) }
                        Spacer(Modifier.size(SweetHomeSpacing.sm))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                title,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            detail?.category?.takeIf { it.isNotBlank() }?.let {
                                Text(
                                    it,
                                    fontSize = 13.sp,
                                    color = Color.White.copy(alpha = 0.85f),
                                )
                            }
                        }
                    }
                    detail?.description?.takeIf { it.isNotBlank() }?.let {
                        Spacer(Modifier.height(SweetHomeSpacing.xs))
                        Text(
                            it,
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.9f),
                        )
                    }
                }
            }

            // Publication / delete bar (visible for owned templates) — content TBD in phase C-3.
            if (isMine) {
                PublicationActionsBar(
                    visibility = detail?.visibility,
                    isLoading = isLoading,
                    onRequestPublication = { coroutineScope.launch { viewModel.requestPublication() } },
                    onWithdrawPublication = { coroutineScope.launch { viewModel.withdrawPublication() } },
                    onDelete = { showDeleteConfirm = true },
                )
            }

            // Items
            val items = detail?.items.orEmpty()
            if (isLoading && detail == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (items.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(SweetHomeSpacing.xl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("📋", fontSize = 32.sp)
                    Spacer(Modifier.height(SweetHomeSpacing.xs))
                    Text(
                        "В этом шаблоне нет задач",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = SweetHomeSpacing.lg,
                        vertical = SweetHomeSpacing.sm,
                    ),
                    verticalArrangement = Arrangement.spacedBy(SweetHomeSpacing.xs),
                ) {
                    items(items, key = { it.id }) { templateItem ->
                        TemplateItemCard(item = templateItem)
                    }
                    item { Spacer(Modifier.height(SweetHomeSpacing.xxl)) }
                }
            }

            // Use button
            detail?.let {
                Surface(
                    onClick = { showUseDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(SweetHomeSpacing.md)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = accent,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            "Использовать шаблон",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }

    if (showUseDialog) {
        UseTemplateDialog(
            defaultTitle = title,
            workspaces = workspaces,
            onDismiss = { showUseDialog = false },
            onConfirm = { workspaceId, listTitle ->
                showUseDialog = false
                coroutineScope.launch {
                    val result = viewModel.use(workspaceId = workspaceId, title = listTitle)
                    result.fold(
                        onSuccess = {
                            snackbarHostState.showSnackbar("Список создан")
                            navigateToLists()
                        },
                        onFailure = { snackbarHostState.showSnackbar(it.message ?: "Не удалось создать") },
                    )
                }
            },
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Удалить шаблон?") },
            text = { Text("Действие необратимо. Шаблон исчезнет из «Моих» и избранного.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    coroutineScope.launch {
                        viewModel.delete().onSuccess { navigateBack() }
                    }
                }) { Text("Удалить", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Отмена") } },
        )
    }
}

@Composable
private fun IconBadge(emoji: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(36.dp),
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.18f),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(emoji, fontSize = 18.sp, color = Color.White)
        }
    }
}

@Composable
private fun PublicationActionsBar(
    visibility: String?,
    isLoading: Boolean,
    onRequestPublication: () -> Unit,
    onWithdrawPublication: () -> Unit,
    onDelete: () -> Unit,
) {
    val (label, ctaLabel, ctaAction) = when (visibility) {
        "private" -> Triple(
            "Видимость: только вам",
            "📤 Запросить публикацию",
            onRequestPublication,
        )
        "pending" -> Triple(
            "Видимость: на модерации",
            "⏪ Отозвать заявку",
            onWithdrawPublication,
        )
        "public" -> Triple(
            "Видимость: публичный",
            null,
            null,
        )
        else -> return
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SweetHomeSpacing.md, vertical = SweetHomeSpacing.xs),
        shape = SweetHomeShapes.Card,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(modifier = Modifier.padding(SweetHomeSpacing.sm)) {
            Text(
                label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(SweetHomeSpacing.xs))
            Row(horizontalArrangement = Arrangement.spacedBy(SweetHomeSpacing.xs)) {
                if (ctaLabel != null && ctaAction != null) {
                    TextButton(onClick = ctaAction, enabled = !isLoading) { Text(ctaLabel) }
                }
                TextButton(
                    onClick = onDelete,
                    enabled = !isLoading,
                ) { Text("🗑 Удалить", color = MaterialTheme.colorScheme.error) }
            }
        }
    }
}

@Composable
private fun TemplateItemCard(item: TemplateListItem) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = SweetHomeShapes.Card,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 0.5.dp,
    ) {
        Column(modifier = Modifier.padding(SweetHomeSpacing.sm)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                priorityEmoji(item.priority)?.let {
                    Text(it, fontSize = 14.sp)
                    Spacer(Modifier.size(6.dp))
                }
                Text(
                    item.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                item.reward?.takeIf { it > 0 }?.let {
                    Text(
                        "+$it",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            val parts = buildList {
                item.shoppingDetails?.let { sd ->
                    val qty = sd.quantity?.let { q -> if (q % 1 == 0.0) q.toLong().toString() else q.toString() }
                    if (qty != null || sd.unit != null) add("${qty ?: ""}${sd.unit ?: ""}".trim())
                    sd.category?.let { add(it) }
                }
                item.choreSchedule?.intervalDays?.let { add("каждые $it д.") }
                item.note?.takeIf { it.isNotBlank() }?.let { add(it) }
            }.filter { it.isNotBlank() }
            if (parts.isNotEmpty()) {
                Text(
                    parts.joinToString(" · "),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun UseTemplateDialog(
    defaultTitle: String,
    workspaces: List<Group>,
    onDismiss: () -> Unit,
    onConfirm: (workspaceId: String, title: String) -> Unit,
) {
    var listTitle by remember { mutableStateOf(defaultTitle) }
    var selectedWorkspace by remember(workspaces) {
        mutableStateOf(
            workspaces.firstOrNull { it.type == "personal" }?.id
                ?: workspaces.firstOrNull()?.id,
        )
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Создать список") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = listTitle,
                    onValueChange = { listTitle = it },
                    label = { Text("Название списка") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    "Куда сохранить:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                workspaces.forEach { ws ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        androidx.compose.material3.RadioButton(
                            selected = selectedWorkspace == ws.id,
                            onClick = { selectedWorkspace = ws.id },
                        )
                        Text(
                            "${workspaceEmoji(ws.type)} ${ws.title}",
                            fontSize = 14.sp,
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { selectedWorkspace?.let { onConfirm(it, listTitle.ifBlank { defaultTitle }) } },
                enabled = selectedWorkspace != null && listTitle.isNotBlank(),
            ) { Text("Создать") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
    )
}

private fun priorityEmoji(p: String?): String? = when (p) {
    "high" -> "⬆️"
    "medium" -> "➡️"
    "low" -> "⬇️"
    else -> null
}

private fun workspaceEmoji(type: String): String = when (type) {
    "personal" -> "👤"
    "family" -> "👨‍👩‍👧‍👦"
    "work" -> "💼"
    "mentoring" -> "🎓"
    else -> "👥"
}
