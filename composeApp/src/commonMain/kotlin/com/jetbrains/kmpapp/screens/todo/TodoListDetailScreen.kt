package com.jetbrains.kmpapp.screens.todo

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jetbrains.kmpapp.data.categories.Category
import com.jetbrains.kmpapp.data.groups.GroupMember
import com.jetbrains.kmpapp.data.lists.ChoreSchedule
import com.jetbrains.kmpapp.data.lists.ShoppingItemFields
import com.jetbrains.kmpapp.data.lists.TodoItem
import com.jetbrains.kmpapp.data.suggestions.ChoreTemplate
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.daysUntil
import kotlinx.datetime.todayIn
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TodoListDetailScreen(
    listId: String,
    navigateBack: () -> Unit,
) {
    val viewModel = koinViewModel<TodoListDetailViewModel>()
    val listWithItems by viewModel.listWithItems.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val choreTemplates by viewModel.choreTemplates.collectAsStateWithLifecycle()
    val frequentItems by viewModel.frequentItems.collectAsStateWithLifecycle()
    val memberNames by viewModel.memberNames.collectAsStateWithLifecycle()
    val groupMembers by viewModel.groupMembers.collectAsStateWithLifecycle()
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
    val isGroupList = listWithItems?.first?.scope == "group"
    val categoryScope = when (listType) {
        "shopping" -> "shopping"
        "home_chores" -> "chore"
        else -> null
    }

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
                            memberNames = memberNames,
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
            categories = categories,
            choreTemplates = choreTemplates,
            frequentItems = frequentItems,
            isGroupList = isGroupList,
            members = groupMembers,
            onDismiss = { showAddDialog = false },
            onConfirm = { title, note, dueAt, isFavorite, assignedTo, shopping, choreSchedule ->
                viewModel.addItem(listId, title, note, dueAt, isFavorite, assignedTo, shopping, choreSchedule)
                showAddDialog = false
            },
            onCreateCategory = { name ->
                categoryScope?.let { viewModel.createCategory(it, name) }
            },
        )
    }

    editingItem?.let { item ->
        ItemDialog(
            listType = listType,
            item = item,
            categories = categories,
            choreTemplates = choreTemplates,
            frequentItems = frequentItems,
            isGroupList = isGroupList,
            members = groupMembers,
            onDismiss = { editingItem = null },
            onConfirm = { title, note, dueAt, isFavorite, assignedTo, shopping, choreSchedule ->
                viewModel.updateItem(item, title, note, dueAt, isFavorite, assignedTo, shopping, choreSchedule)
                editingItem = null
            },
            onCreateCategory = { name ->
                categoryScope?.let { viewModel.createCategory(it, name) }
            },
        )
    }
}

private fun parseDateOrNull(dateStr: String): LocalDate? {
    return try {
        LocalDate.parse(dateStr.take(10))
    } catch (_: Exception) {
        null
    }
}

private fun choreStatusColor(daysRemaining: Int): androidx.compose.ui.graphics.Color {
    return when {
        daysRemaining < 0 -> androidx.compose.ui.graphics.Color(0xFFD32F2F)
        daysRemaining <= 1 -> androidx.compose.ui.graphics.Color(0xFFF57C00)
        daysRemaining <= 3 -> androidx.compose.ui.graphics.Color(0xFFFBC02D)
        else -> androidx.compose.ui.graphics.Color(0xFF388E3C)
    }
}

private fun millisToDateString(millis: Long): String {
    val instant = Instant.fromEpochMilliseconds(millis)
    val date = instant.toLocalDateTime(TimeZone.UTC).date
    return date.toString() // YYYY-MM-DD
}

private fun dateStringToMillis(dateStr: String): Long? {
    return parseDateOrNull(dateStr)?.atStartOfDayIn(TimeZone.UTC)?.toEpochMilliseconds()
}

@Composable
private fun TodoItemRow(
    item: TodoItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    memberNames: Map<String, String> = emptyMap(),
    modifier: Modifier = Modifier,
) {
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }

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
                item.assignedTo?.let { userId ->
                    if (userId.isNotBlank()) {
                        val name = memberNames[userId] ?: userId.take(8)
                        Text(
                            text = "→ $name",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary,
                        )
                    }
                }
                item.shopping?.let { s ->
                    val parts = buildList {
                        s.quantity?.let { qty ->
                            add(if (qty % 1 == 0.0) qty.toLong().toString() else qty.toString())
                        }
                        s.unit?.let { if (it.isNotBlank()) add(it) }
                        s.category?.let { if (it.isNotBlank()) add("· $it") }
                    }
                    if (parts.isNotEmpty()) {
                        Text(
                            text = parts.joinToString(" "),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                item.dueAt?.let { due ->
                    if (due.isNotBlank()) {
                        val dueDate = parseDateOrNull(due)
                        val isOverdue = dueDate != null && !item.isDone && dueDate < today
                        val daysLeft = dueDate?.let { today.daysUntil(it) }
                        val label = when {
                            isOverdue -> "Просрочено: $due"
                            daysLeft != null && daysLeft == 0 -> "Сегодня: $due"
                            daysLeft != null && daysLeft == 1 -> "Завтра: $due"
                            else -> "До: $due"
                        }
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isOverdue) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                item.choreSchedule?.let { sched ->
                    val parts = buildList {
                        sched.intervalDays?.let { add("Каждые $it д.") }
                        sched.category?.let { if (it.isNotBlank()) add("· $it") }
                    }
                    if (parts.isNotEmpty()) {
                        Text(
                            text = parts.joinToString(" "),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    val interval = sched.intervalDays
                    val lastDone = sched.lastDoneAt?.let { parseDateOrNull(it) }
                    if (interval != null && interval > 0 && lastDone != null && !item.isDone) {
                        val daysSinceDone = lastDone.daysUntil(today)
                        val daysRemaining = interval - daysSinceDone
                        val statusText = when {
                            daysRemaining < 0 -> "Просрочено на ${-daysRemaining} д."
                            daysRemaining == 0 -> "Нужно сделать сегодня"
                            daysRemaining == 1 -> "Осталось 1 день"
                            else -> "Осталось $daysRemaining д."
                        }
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelSmall,
                            color = choreStatusColor(daysRemaining),
                        )
                    }
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

private val dayOptions = listOf(
    "monday" to "Пн",
    "tuesday" to "Вт",
    "wednesday" to "Ср",
    "thursday" to "Чт",
    "friday" to "Пт",
    "saturday" to "Сб",
    "sunday" to "Вс",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerField(
    label: String,
    dateValue: String,
    onDateSelected: (String) -> Unit,
    onClear: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    var showPicker by remember { mutableStateOf(false) }
    val initialMillis = dateStringToMillis(dateValue)
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            value = if (dateValue.isNotBlank()) dateValue else "Не выбрана",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                IconButton(onClick = { showPicker = true }) {
                    Icon(Icons.Default.DateRange, "Выбрать дату")
                }
            },
            modifier = Modifier.weight(1f),
        )
        if (dateValue.isNotBlank() && onClear != null) {
            TextButton(onClick = onClear) { Text("Сбросить") }
        }
    }

    if (showPicker) {
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        onDateSelected(millisToDateString(millis))
                    }
                    showPicker = false
                }) { Text("ОК") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Отмена") }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemDialog(
    listType: String,
    item: TodoItem? = null,
    categories: List<Category>,
    choreTemplates: List<ChoreTemplate>,
    frequentItems: List<TodoItem>,
    isGroupList: Boolean = false,
    members: List<GroupMember> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        note: String,
        dueAt: String,
        isFavorite: Boolean,
        assignedTo: String?,
        shopping: ShoppingItemFields?,
        choreSchedule: ChoreSchedule?,
    ) -> Unit,
    onCreateCategory: (name: String) -> Unit,
) {
    var title by remember { mutableStateOf(item?.title ?: "") }
    var note by remember { mutableStateOf(item?.note ?: "") }
    var dueAt by remember { mutableStateOf(item?.dueAt?.take(10) ?: "") }
    var isFavorite by remember { mutableStateOf(item?.isFavorite ?: false) }
    var assignedTo by remember { mutableStateOf(item?.assignedTo) }
    var quantity by remember {
        mutableStateOf(item?.shopping?.quantity?.let {
            if (it % 1 == 0.0) it.toLong().toString() else it.toString()
        } ?: "")
    }
    var unit by remember { mutableStateOf(item?.shopping?.unit ?: "") }
    var intervalDays by remember { mutableStateOf(item?.choreSchedule?.intervalDays?.toString() ?: "") }
    var selectedDays by remember { mutableStateOf(item?.choreSchedule?.daysOfWeek ?: emptyList()) }
    var startDate by remember { mutableStateOf(item?.choreSchedule?.startDate ?: "") }
    var endDate by remember { mutableStateOf(item?.choreSchedule?.endDate ?: "") }
    var selectedCategory by remember {
        mutableStateOf(item?.shopping?.category ?: item?.choreSchedule?.category)
    }
    var showNewCategoryField by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (item == null) "Новая задача" else "Редактировать") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Suggestions (only in add mode)
                if (item == null) {
                    if (choreTemplates.isNotEmpty() && listType == "home_chores") {
                        item {
                            Text(
                                "Шаблоны",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        item {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(choreTemplates) { template ->
                                    SuggestionChip(
                                        onClick = {
                                            title = template.title
                                            intervalDays = template.intervalDays.toString()
                                            selectedCategory = template.category.takeIf { it.isNotBlank() }
                                        },
                                        label = {
                                            Text(
                                                template.title,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                        },
                                    )
                                }
                            }
                        }
                    }
                    if (frequentItems.isNotEmpty() && listType == "shopping") {
                        item {
                            Text(
                                "Часто добавляли",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        item {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(frequentItems) { suggestion ->
                                    SuggestionChip(
                                        onClick = {
                                            title = suggestion.title
                                            selectedCategory = suggestion.shopping?.category
                                            quantity = suggestion.shopping?.quantity?.let {
                                                if (it % 1 == 0.0) it.toLong().toString() else it.toString()
                                            } ?: ""
                                            unit = suggestion.shopping?.unit ?: ""
                                        },
                                        label = {
                                            Text(
                                                suggestion.title,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                        },
                                    )
                                }
                            }
                        }
                    }
                }

                // Title
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Название") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                // Note
                item {
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Заметка") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                // Due date — universal for all types (DatePicker)
                item {
                    DatePickerField(
                        label = "Срок",
                        dateValue = dueAt,
                        onDateSelected = { dueAt = it },
                        onClear = { dueAt = "" },
                    )
                }

                // Favorite — universal for all types
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isFavorite, onCheckedChange = { isFavorite = it })
                        Text("Избранное")
                    }
                }

                // Assigned to — for group lists
                if (isGroupList && members.isNotEmpty()) {
                    item {
                        Text(
                            "Исполнитель",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                FilterChip(
                                    selected = assignedTo == null,
                                    onClick = { assignedTo = null },
                                    label = { Text("Никто") },
                                )
                            }
                            items(members) { member ->
                                FilterChip(
                                    selected = assignedTo == member.userId,
                                    onClick = {
                                        assignedTo = if (assignedTo == member.userId) null
                                        else member.userId
                                    },
                                    label = { Text(member.displayName) },
                                )
                            }
                        }
                    }
                }

                // Type-specific fields
                when (listType) {
                    "shopping" -> {
                        item {
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
                        }
                    }
                    "home_chores" -> {
                        item {
                            OutlinedTextField(
                                value = intervalDays,
                                onValueChange = { intervalDays = it },
                                label = { Text("Повтор каждые N дней") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        // Days of week
                        item {
                            Text(
                                "Дни недели",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        item {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                items(dayOptions) { (dayKey, dayLabel) ->
                                    FilterChip(
                                        selected = dayKey in selectedDays,
                                        onClick = {
                                            selectedDays = if (dayKey in selectedDays) {
                                                selectedDays - dayKey
                                            } else {
                                                selectedDays + dayKey
                                            }
                                        },
                                        label = { Text(dayLabel) },
                                    )
                                }
                            }
                        }
                        // Start date
                        item {
                            DatePickerField(
                                label = "Дата начала",
                                dateValue = startDate,
                                onDateSelected = { startDate = it },
                                onClear = { startDate = "" },
                            )
                        }
                        // End date
                        item {
                            DatePickerField(
                                label = "Дата окончания",
                                dateValue = endDate,
                                onDateSelected = { endDate = it },
                                onClear = { endDate = "" },
                            )
                        }
                    }
                }

                // Categories (shopping and home_chores)
                if (categories.isNotEmpty() && listType != "general_todos") {
                    item {
                        Text(
                            "Категория",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(categories) { category ->
                                FilterChip(
                                    selected = selectedCategory == category.name,
                                    onClick = {
                                        selectedCategory = if (selectedCategory == category.name) null
                                        else category.name
                                    },
                                    label = { Text(category.name) },
                                )
                            }
                            item {
                                if (showNewCategoryField) {
                                    OutlinedTextField(
                                        value = newCategoryName,
                                        onValueChange = { newCategoryName = it },
                                        label = { Text("Название") },
                                        singleLine = true,
                                        trailingIcon = {
                                            TextButton(
                                                onClick = {
                                                    if (newCategoryName.isNotBlank()) {
                                                        onCreateCategory(newCategoryName)
                                                        selectedCategory = newCategoryName
                                                        newCategoryName = ""
                                                        showNewCategoryField = false
                                                    }
                                                }
                                            ) { Text("ОК") }
                                        },
                                    )
                                } else {
                                    InputChip(
                                        selected = false,
                                        onClick = { showNewCategoryField = true },
                                        label = { Text("+ Создать") },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val shopping = if (listType == "shopping" &&
                        (quantity.isNotBlank() || unit.isNotBlank() || selectedCategory != null)
                    ) {
                        ShoppingItemFields(
                            quantity = quantity.toDoubleOrNull(),
                            unit = unit.takeIf { it.isNotBlank() },
                            category = selectedCategory,
                        )
                    } else null
                    val choreSchedule = if (listType == "home_chores" &&
                        (intervalDays.isNotBlank() || selectedCategory != null ||
                                selectedDays.isNotEmpty() || startDate.isNotBlank())
                    ) {
                        ChoreSchedule(
                            intervalDays = intervalDays.toIntOrNull(),
                            daysOfWeek = selectedDays.takeIf { it.isNotEmpty() },
                            startDate = startDate.takeIf { it.isNotBlank() },
                            endDate = endDate.takeIf { it.isNotBlank() },
                            category = selectedCategory,
                        )
                    } else null
                    val dueAtRfc3339 = dueAt.takeIf { it.isNotBlank() }
                        ?.let { dateStr ->
                            if ("T" in dateStr) dateStr
                            else parseDateOrNull(dateStr)
                                ?.atStartOfDayIn(TimeZone.currentSystemDefault())
                                ?.toString()
                                ?: dateStr
                        } ?: ""
                    onConfirm(
                        title.ifBlank { "Новая задача" },
                        note,
                        dueAtRfc3339,
                        isFavorite,
                        assignedTo,
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
