package com.jetbrains.kmpapp.screens.todo

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jetbrains.kmpapp.data.lists.ChoreSchedule
import com.jetbrains.kmpapp.data.lists.ShoppingItemFields
import com.jetbrains.kmpapp.data.lists.TodoItem
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TodoListDetailScreen(
    listId: String,
    navigateBack: () -> Unit,
) {
    val viewModel = koinViewModel<TodoListDetailViewModel>()
    val listWithItems by viewModel.listWithItems.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<TodoItem?>(null) }

    LaunchedEffect(listId) {
        viewModel.loadList(listId)
    }

    LaunchedEffect(error) {
        error?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearError()
        }
    }

    val listType = listWithItems?.first?.type ?: "general_todos"

    @OptIn(ExperimentalMaterial3Api::class)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(listWithItems?.first?.title ?: "Список") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                content = { Icon(Icons.Default.Add, "Добавить") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        AnimatedContent(listWithItems != null) { hasData ->
            if (hasData) {
                val (_, items) = listWithItems!!
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = WindowInsets.safeDrawing.asPaddingValues(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(items, key = { it.id }) { item ->
                        TodoItemRow(
                            item = item,
                            onToggle = { viewModel.toggleItem(item) },
                            onDelete = { viewModel.deleteItem(item) },
                            onEdit = { editingItem = item },
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("Загрузка...", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }

    if (showAddDialog) {
        ItemDialog(
            listType = listType,
            onDismiss = { showAddDialog = false },
            onConfirm = { title, note, dueAt, isFavorite, shopping, choreSchedule ->
                viewModel.addItem(listId, title, note, dueAt, isFavorite, shopping, choreSchedule)
                showAddDialog = false
            },
        )
    }

    editingItem?.let { item ->
        ItemDialog(
            listType = listType,
            item = item,
            onDismiss = { editingItem = null },
            onConfirm = { title, note, dueAt, isFavorite, shopping, choreSchedule ->
                viewModel.updateItem(item, title, note, dueAt, isFavorite, shopping, choreSchedule)
                editingItem = null
            },
        )
    }
}

@Composable
private fun TodoItemRow(
    item: TodoItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isDone) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onToggle,
                modifier = Modifier.padding(end = 8.dp),
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = if (item.isDone) "Отметить невыполненным" else "Выполнено",
                    tint = if (item.isDone) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline
                    },
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onEdit() },
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (item.isDone) TextDecoration.LineThrough else null,
                )
                item.note?.let { note ->
                    if (note.isNotBlank()) {
                        Text(
                            text = note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                item.shopping?.let { s ->
                    val qty = s.quantity
                    val unit = s.unit
                    if (qty != null || unit != null) {
                        val display = buildString {
                            if (qty != null) append(if (qty % 1 == 0.0) qty.toLong().toString() else qty.toString())
                            if (unit != null) append(" $unit")
                        }
                        Text(
                            text = display.trim(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                item.dueAt?.let { due ->
                    if (due.isNotBlank()) {
                        Text(
                            text = "До: $due",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
                item.choreSchedule?.intervalDays?.let { days ->
                    Text(
                        text = "Каждые $days д.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (item.isFavorite) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = "Избранное",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Удалить", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun ItemDialog(
    listType: String,
    item: TodoItem? = null,
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        note: String,
        dueAt: String,
        isFavorite: Boolean,
        shopping: ShoppingItemFields?,
        choreSchedule: ChoreSchedule?,
    ) -> Unit,
) {
    var title by remember { mutableStateOf(item?.title ?: "") }
    var note by remember { mutableStateOf(item?.note ?: "") }
    var dueAt by remember { mutableStateOf(item?.dueAt ?: "") }
    var isFavorite by remember { mutableStateOf(item?.isFavorite ?: false) }
    var quantity by remember { mutableStateOf(item?.shopping?.quantity?.let { if (it % 1 == 0.0) it.toLong().toString() else it.toString() } ?: "") }
    var unit by remember { mutableStateOf(item?.shopping?.unit ?: "") }
    var intervalDays by remember { mutableStateOf(item?.choreSchedule?.intervalDays?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (item == null) "Новая задача" else "Редактировать") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Заметка") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
                when (listType) {
                    "shopping" -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = quantity,
                                onValueChange = { quantity = it },
                                label = { Text("Кол-во") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                            )
                            OutlinedTextField(
                                value = unit,
                                onValueChange = { unit = it },
                                label = { Text("Ед. изм.") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = isFavorite, onCheckedChange = { isFavorite = it })
                            Text("Избранное")
                        }
                    }
                    "general_todos" -> {
                        OutlinedTextField(
                            value = dueAt,
                            onValueChange = { dueAt = it },
                            label = { Text("Срок (ГГГГ-ММ-ДД)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    "home_chores" -> {
                        OutlinedTextField(
                            value = intervalDays,
                            onValueChange = { intervalDays = it },
                            label = { Text("Повтор каждые N дней") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val shopping = if (listType == "shopping" && (quantity.isNotBlank() || unit.isNotBlank())) {
                        ShoppingItemFields(
                            quantity = quantity.toDoubleOrNull(),
                            unit = unit.takeIf { it.isNotBlank() },
                        )
                    } else null
                    val choreSchedule = if (listType == "home_chores" && intervalDays.isNotBlank()) {
                        ChoreSchedule(intervalDays = intervalDays.toIntOrNull())
                    } else null
                    onConfirm(
                        title.ifBlank { "Новая задача" },
                        note,
                        dueAt,
                        isFavorite,
                        shopping,
                        choreSchedule,
                    )
                }
            ) {
                Text(if (item == null) "Добавить" else "Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        },
    )
}
