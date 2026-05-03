package com.jetbrains.kmpapp.screens.todo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jetbrains.kmpapp.data.categories.Category
import com.jetbrains.kmpapp.data.groups.GroupMember
import com.jetbrains.kmpapp.data.lists.ChoreSchedule
import com.jetbrains.kmpapp.data.lists.ShoppingItemFields
import com.jetbrains.kmpapp.data.lists.TodoItem
import com.jetbrains.kmpapp.data.suggestions.ChoreTemplate
import com.jetbrains.kmpapp.data.templates.TaskTemplate
import com.jetbrains.kmpapp.data.templates.TaskTemplateDetail
import com.jetbrains.kmpapp.ui.SweetHomeSpacing
import com.jetbrains.kmpapp.ui.listColorForType
import com.jetbrains.kmpapp.ui.listEmojiForType
import com.jetbrains.kmpapp.ui.toComposeColor
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListDetailScreen(
    listId: String,
    navigateBack: () -> Unit,
    contentWindowInsets: WindowInsets = WindowInsets.systemBars,
) {
    val viewModel = koinViewModel<TodoListDetailViewModel>()
    val listWithItems by viewModel.listWithItems.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val choreTemplates by viewModel.choreTemplates.collectAsStateWithLifecycle()
    val frequentItems by viewModel.frequentItems.collectAsStateWithLifecycle()
    val memberNames by viewModel.memberNames.collectAsStateWithLifecycle()
    val groupMembers by viewModel.groupMembers.collectAsStateWithLifecycle()
    val currentUserId by viewModel.currentUserId.collectAsStateWithLifecycle()
    val isGroupListFlag by viewModel.isGroupList.collectAsStateWithLifecycle()
    val taskTemplates by viewModel.taskTemplatesForList.collectAsStateWithLifecycle()
    val favoriteItems by viewModel.favoriteItems.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddSheet by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<TodoItem?>(null) }
    var assigneePickerFor by remember { mutableStateOf<TodoItem?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    var showSaveAsTemplateDialog by remember { mutableStateOf(false) }
    val saveAsTemplateScope = rememberCoroutineScope()

    LaunchedEffect(listId) { viewModel.loadList(listId) }
    LaunchedEffect(error) {
        error?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearError()
        }
    }

    val list = listWithItems?.first
    val listType = list?.type ?: "general_todos"
    // Для гейтинга UI назначения используем оба сигнала: workspace-тип и факт загруженности участников.
    val isGroupList = isGroupListFlag || groupMembers.isNotEmpty()
    val listColor = list?.color?.toComposeColor() ?: listColorForType(listType, isSystemInDarkTheme())
    val listIcon = list?.icon ?: listEmojiForType(listType)
    val categoryScope = when (listType) {
        "shopping" -> "shopping"
        "home_chores" -> "chore"
        else -> null
    }
    var completedExpanded by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = contentWindowInsets,
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.systemBars.only(androidx.compose.foundation.layout.WindowInsetsSides.Top))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // Circular back button
                    Surface(
                        onClick = navigateBack,
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "‹",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = listColor,
                            )
                        }
                    }
                    Text(text = listIcon, fontSize = 18.sp)
                    Text(
                        text = list?.title ?: "Список",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (listType == "wishlist") {
                        val coroutineScope = rememberCoroutineScope()
                        val platformContext = com.jetbrains.kmpapp.ui.rememberPlatformContext()
                        Surface(
                            onClick = {
                                coroutineScope.launch {
                                    val url = viewModel.ensurePublicShareUrl()
                                    if (url != null) {
                                        com.jetbrains.kmpapp.ui.shareUrl(url, platformContext)
                                    } else {
                                        snackbarHostState.showSnackbar("Не удалось создать ссылку")
                                    }
                                }
                            },
                            modifier = Modifier.size(36.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("📤", fontSize = 16.sp)
                            }
                        }
                    }
                    Box {
                        Surface(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(36.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = "Меню",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            // Save-as-template (G-03 фаза C). Запрещено для wishlist (бэкенд вернёт 400).
                            if (listType != "wishlist") {
                                DropdownMenuItem(
                                    text = { Text("Сохранить как шаблон") },
                                    onClick = {
                                        showMenu = false
                                        showSaveAsTemplateDialog = true
                                    },
                                    leadingIcon = { Text("📋", fontSize = 16.sp) },
                                )
                                HorizontalDivider()
                            }
                            DropdownMenuItem(
                                text = { Text("Удалить список") },
                                onClick = { showMenu = false },
                                leadingIcon = { Icon(Icons.Default.Delete, null) },
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = listColor,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier.shadow(8.dp, CircleShape),
            ) {
                Text("+", fontSize = 28.sp, fontWeight = FontWeight.Light)
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        val items = listWithItems?.second
        if (items == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                Text("Загрузка...", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 15.sp)
            }
            return@Scaffold
        }

        val activeItems = items.filter { !it.isDone }
        val completedItems = items.filter { it.isDone }
        val total = items.size
        val done = completedItems.size
        val progress = if (total > 0) done.toFloat() / total else 0f

        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Progress section — fixed below TopBar
            if (total > 0) {
                Surface(color = MaterialTheme.colorScheme.surface) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                            .padding(bottom = 4.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = "$done из $total выполнено",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = "${(progress * 100).toInt()}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = listColor,
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = listColor,
                            trackColor = MaterialTheme.colorScheme.outlineVariant,
                            strokeCap = StrokeCap.Round,
                        )
                    }
                }
            }

            // Items list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 16.dp,
                    vertical = 12.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Empty state
                if (total == 0) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 60.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text("✅", fontSize = 48.sp)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Список пуст",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Добавьте первый элемент",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                // Active items
                items(activeItems, key = { it.id }) { item ->
                    TodoItemRow(
                        item = item,
                        listColor = listColor,
                        onToggle = { viewModel.toggleItem(item) },
                        onDelete = { viewModel.deleteItem(item) },
                        onEdit = { editingItem = item },
                        memberNames = memberNames,
                        isGroupList = isGroupList,
                        currentUserId = currentUserId,
                        onQuickAssignSelf = {
                            currentUserId?.let { viewModel.setAssignee(item, it) }
                        },
                        onPickAssignee = { assigneePickerFor = item },
                    )
                }

                // Completed section
                if (completedItems.isNotEmpty()) {
                    item(key = "completed_divider") {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    }
                    item(key = "completed_header") {
                        CompletedSectionHeader(
                            count = completedItems.size,
                            expanded = completedExpanded,
                            listColor = listColor,
                            onClick = { completedExpanded = !completedExpanded },
                        )
                    }
                    if (completedExpanded) {
                        items(completedItems, key = { it.id }) { item ->
                            TodoItemRow(
                                item = item,
                                listColor = listColor,
                                onToggle = { viewModel.toggleItem(item) },
                                onDelete = { viewModel.deleteItem(item) },
                                onEdit = { editingItem = item },
                                memberNames = memberNames,
                            )
                        }
                    }
                }

                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    if (showAddSheet) {
        ItemBottomSheet(
            listType = listType,
            categories = categories,
            choreTemplates = choreTemplates,
            frequentItems = frequentItems,
            favoriteItems = favoriteItems,
            taskTemplates = taskTemplates,
            isGroupList = isGroupList,
            members = groupMembers,
            listColor = listColor,
            onLoadTemplatesForPicker = { viewModel.loadTemplatesForPicker() },
            onResolveTaskTemplate = { id -> viewModel.getTaskTemplateDetail(id) },
            onDismiss = { showAddSheet = false },
            onConfirm = { title, note, dueAt, isFavorite, assignedTo, shopping, choreSchedule ->
                viewModel.addItem(listId, title, note, dueAt, isFavorite, assignedTo, shopping, choreSchedule)
                showAddSheet = false
            },
            onCreateCategory = { name ->
                categoryScope?.let { viewModel.createCategory(it, name) }
            },
        )
    }

    if (showSaveAsTemplateDialog) {
        val initialTitle = list?.title.orEmpty()
        SaveAsTemplateDialog(
            initialTitle = initialTitle,
            onDismiss = { showSaveAsTemplateDialog = false },
            onConfirm = { title, category, description ->
                showSaveAsTemplateDialog = false
                saveAsTemplateScope.launch {
                    viewModel.saveListAsTemplate(category, title, description).fold(
                        onSuccess = { snackbarHostState.showSnackbar("Шаблон сохранён в «Мои»") },
                        onFailure = { snackbarHostState.showSnackbar(it.message ?: "Не удалось сохранить шаблон") },
                    )
                }
            },
        )
    }

    assigneePickerFor?.let { item ->
        AssigneePickerSheet(
            members = groupMembers,
            currentAssigneeId = item.assignedTo?.takeIf { it.isNotBlank() },
            currentUserId = currentUserId,
            onPick = { picked -> viewModel.setAssignee(item, picked) },
            onDismiss = { assigneePickerFor = null },
        )
    }

    editingItem?.let { item ->
        val picker = com.jetbrains.kmpapp.media.rememberImagePicker()
        val attachmentsRepo = org.koin.compose.koinInject<com.jetbrains.kmpapp.data.attachments.AttachmentsRepository>()
        ItemBottomSheet(
            listType = listType,
            item = item,
            categories = categories,
            choreTemplates = choreTemplates,
            frequentItems = frequentItems,
            favoriteItems = favoriteItems,
            taskTemplates = taskTemplates,
            isGroupList = isGroupList,
            members = groupMembers,
            listColor = listColor,
            onLoadTemplatesForPicker = { viewModel.loadTemplatesForPicker() },
            onResolveTaskTemplate = { id -> viewModel.getTaskTemplateDetail(id) },
            onDismiss = { editingItem = null },
            onConfirm = { title, note, dueAt, isFavorite, assignedTo, shopping, choreSchedule ->
                viewModel.updateItem(item, title, note, dueAt, isFavorite, assignedTo, shopping, choreSchedule)
                editingItem = null
            },
            onAttachPhoto = {
                val picked = picker.pick() ?: return@ItemBottomSheet "Отменено"
                val res = attachmentsRepo.upload(item.id, picked.mimeType, picked.bytes)
                res.fold(
                    onSuccess = { "Фото загружено" },
                    onFailure = { it.message ?: "Ошибка загрузки" },
                )
            },
            onCreateCategory = { name ->
                categoryScope?.let { viewModel.createCategory(it, name) }
            },
        )
    }
}

@Composable
private fun CompletedSectionHeader(
    count: Int,
    expanded: Boolean,
    listColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(listColor, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text("✓", color = MaterialTheme.colorScheme.onPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = "Выполнено · $count задач",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Icon(
            if (expanded) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun TodoItemRow(
    item: TodoItem,
    listColor: Color,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    memberNames: Map<String, String> = emptyMap(),
    isGroupList: Boolean = false,
    currentUserId: String? = null,
    onQuickAssignSelf: () -> Unit = {},
    onPickAssignee: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (item.isDone) 0.55f else 1f),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = if (item.isDone) 0.dp else 1.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp)),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp, 12.dp, 12.dp, 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Circular checkbox toggle
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (item.isDone) listColor else Color.Transparent)
                        .border(2.dp, if (item.isDone) listColor else Color(0xFFC9C9C9), CircleShape)
                        .clickable { onToggle() },
                    contentAlignment = Alignment.Center,
                ) {
                    if (item.isDone) {
                        Text("✓", color = MaterialTheme.colorScheme.onPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onEdit() },
                ) {
                    Text(
                        text = item.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (item.isDone) TextDecoration.LineThrough else TextDecoration.None,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    // Meta row
                    val metaParts = buildList {
                        item.shopping?.let { s ->
                            val qty = s.quantity?.let { q ->
                                if (q % 1 == 0.0) q.toLong().toString() else q.toString()
                            }
                            if (qty != null || s.unit != null) {
                                add("${qty ?: ""}${s.unit ?: ""}")
                            }
                        }
                        item.choreSchedule?.let { sched ->
                            sched.intervalDays?.let { add("📅 каждые $it д.") }
                        }
                        item.dueAt?.takeIf { it.isNotBlank() }?.let { due ->
                            val dueDate = parseDateOrNull(due)
                            val isOverdue = dueDate != null && !item.isDone && dueDate < today
                            add(if (isOverdue) "‼ до ${due.take(10)}" else "до ${due.take(10)}")
                        }
                        // assignedTo показывается отдельным AssigneeChip справа — в meta не дублируем.
                    }
                    if (metaParts.isNotEmpty()) {
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = metaParts.joinToString(" · "),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                // Right area
                if (item.isFavorite) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFA726),
                        modifier = Modifier.size(16.dp),
                    )
                }
                if (isGroupList && !item.isDone) {
                    AssigneeChip(
                        assignedTo = item.assignedTo?.takeIf { it.isNotBlank() },
                        currentUserId = currentUserId,
                        memberNames = memberNames,
                        onQuickAssignSelf = onQuickAssignSelf,
                        onPickAssignee = onPickAssignee,
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Удалить",
                        tint = Color(0xFFBDBDBD),
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun AssigneeChip(
    assignedTo: String?,
    currentUserId: String?,
    memberNames: Map<String, String>,
    onQuickAssignSelf: () -> Unit,
    onPickAssignee: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isMe = assignedTo != null && assignedTo == currentUserId
    val isUnassigned = assignedTo == null
    val (bg, fg) = when {
        isUnassigned -> Color.Transparent to MaterialTheme.colorScheme.onSurfaceVariant
        isMe -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    val label = when {
        isUnassigned -> "+"
        isMe -> "Я"
        else -> {
            val name = memberNames[assignedTo] ?: assignedTo!!
            name.firstOrNull()?.uppercase() ?: "?"
        }
    }
    val onClick = if (isUnassigned) onQuickAssignSelf else onPickAssignee
    Box(
        modifier = modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(bg)
            .border(
                width = 1.dp,
                color = if (isUnassigned) MaterialTheme.colorScheme.outline else Color.Transparent,
                shape = CircleShape,
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontSize = if (isUnassigned) 16.sp else 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = fg,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AssigneePickerSheet(
    members: List<GroupMember>,
    currentAssigneeId: String?,
    currentUserId: String?,
    onPick: (userId: String?) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val close: (String?) -> Unit = { picked ->
        onPick(picked)
        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
    }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(
                "Назначить исполнителя",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp),
            )
            // Unassign option
            AssigneePickerRow(
                initial = "—",
                title = "Не назначен",
                subtitle = null,
                selected = currentAssigneeId == null,
                onClick = { close(null) },
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            members.forEach { member ->
                val isMe = member.userId == currentUserId
                val name = member.displayName?.takeIf { it.isNotBlank() } ?: member.userId.take(8)
                AssigneePickerRow(
                    initial = name.firstOrNull()?.uppercase() ?: "?",
                    title = if (isMe) "$name (вы)" else name,
                    subtitle = roleLabel(member.role),
                    selected = currentAssigneeId == member.userId,
                    onClick = { close(member.userId) },
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun AssigneePickerRow(
    initial: String,
    title: String,
    subtitle: String?,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = initial,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = if (selected) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            subtitle?.let {
                Text(it, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (selected) {
            Text("✓", fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
        }
    }
}

private fun roleLabel(role: String): String = when (role) {
    "owner" -> "Владелец"
    "admin" -> "Админ"
    "mentor" -> "Наставник"
    else -> "Участник"
}

@Composable
private fun SaveAsTemplateDialog(
    initialTitle: String,
    onDismiss: () -> Unit,
    onConfirm: (title: String, category: String, description: String) -> Unit,
) {
    var title by remember { mutableStateOf(initialTitle) }
    var category by remember { mutableStateOf("Общие") }
    var description by remember { mutableStateOf("") }
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Сохранить как шаблон") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Шаблон будет приватным. Опубликовать можно из карточки шаблона.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Категория") },
                    placeholder = { Text("Общие, Завтраки, Поездка…") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание (необязательно)") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(title, category, description) },
                enabled = title.isNotBlank(),
            ) { Text("Сохранить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        },
    )
}

private val dayOptions = listOf(
    "monday" to "Пн", "tuesday" to "Вт", "wednesday" to "Ср",
    "thursday" to "Чт", "friday" to "Пт", "saturday" to "Сб", "sunday" to "Вс",
)

private fun parseDateOrNull(dateStr: String): LocalDate? = try {
    LocalDate.parse(dateStr.take(10))
} catch (_: Exception) { null }

private fun choreStatusColor(daysRemaining: Int): Color = when {
    daysRemaining < 0 -> Color(0xFFD32F2F)
    daysRemaining <= 1 -> Color(0xFFF57C00)
    daysRemaining <= 3 -> Color(0xFFFBC02D)
    else -> Color(0xFF388E3C)
}

private fun millisToDateString(millis: Long): String {
    val instant = Instant.fromEpochMilliseconds(millis)
    val date = instant.toLocalDateTime(TimeZone.UTC).date
    return date.toString()
}

private fun dateStringToMillis(dateStr: String): Long? =
    parseDateOrNull(dateStr)?.atStartOfDayIn(TimeZone.UTC)?.toEpochMilliseconds()

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
            dismissButton = { TextButton(onClick = { showPicker = false }) { Text("Отмена") } },
        ) { DatePicker(state = datePickerState) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemBottomSheet(
    listType: String,
    item: TodoItem? = null,
    categories: List<Category>,
    choreTemplates: List<ChoreTemplate>,
    frequentItems: List<TodoItem>,
    favoriteItems: List<TodoItem> = emptyList(),
    taskTemplates: List<TaskTemplate> = emptyList(),
    isGroupList: Boolean = false,
    members: List<GroupMember> = emptyList(),
    listColor: Color,
    onLoadTemplatesForPicker: () -> Unit = {},
    onResolveTaskTemplate: suspend (id: String) -> TaskTemplateDetail? = { null },
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
    onAttachPhoto: (suspend () -> String?)? = null,    // null когда недоступно (новый item / нет picker)
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val attachScope = rememberCoroutineScope()
    var attachStatus by remember { mutableStateOf<String?>(null) }

    var title by remember { mutableStateOf(item?.title ?: "") }
    var note by remember { mutableStateOf(item?.note ?: "") }
    var dueAt by remember { mutableStateOf(item?.dueAt?.take(10) ?: "") }
    var isFavorite by remember { mutableStateOf(item?.isFavorite ?: false) }
    var assignedTo by remember { mutableStateOf(item?.assignedTo) }
    var priority by remember { mutableStateOf("medium") }
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
    val homeZones = listOf("Кухня", "Гостиная", "Спальня", "Ванная", "Другое")
    var selectedZone by remember {
        mutableStateOf(if (listType == "home_chores") item?.choreSchedule?.category else null)
    }
    var selectedCategory by remember {
        mutableStateOf(if (listType == "shopping") item?.shopping?.category else null)
    }
    var showNewCategoryField by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var showPicker by remember { mutableStateOf(false) }
    val pickerScope = rememberCoroutineScope()

    val typeHeadings = mapOf(
        "shopping" to "Добавить товар",
        "home_chores" to "Добавить дело",
        "general_todos" to "Добавить задачу",
    )
    val heading = if (item == null) (typeHeadings[listType] ?: "Добавить элемент") else "Редактировать"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        // Handle bar already rendered by ModalBottomSheet
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                heading,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (onAttachPhoto != null) {
                IconButton(onClick = {
                    attachScope.launch {
                        attachStatus = "Загрузка…"
                        attachStatus = onAttachPhoto() ?: "Не удалось"
                    }
                }) {
                    Text("📎", fontSize = 20.sp)
                }
            }
            IconButton(onClick = { isFavorite = !isFavorite }) {
                Text(
                    "⭐",
                    fontSize = 22.sp,
                    modifier = Modifier.alpha(if (isFavorite) 1f else 0.3f),
                )
            }
        }
        attachStatus?.let { status ->
            Text(
                status,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 20.dp, vertical = 8.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Suggestions — chore templates
            if (item == null && choreTemplates.isNotEmpty() && listType == "home_chores") {
                item {
                    SheetFieldLabel("Шаблоны")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(choreTemplates) { template ->
                            SuggestionChip(
                                onClick = {
                                    title = template.title
                                    intervalDays = template.intervalDays.toString()
                                    selectedCategory = template.category.takeIf { it.isNotBlank() }
                                },
                                label = { Text(template.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            )
                        }
                    }
                }
            }

            // Frequent items
            if (item == null && frequentItems.isNotEmpty() && listType == "shopping") {
                item {
                    SheetFieldLabel("Часто добавляли")
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
                                label = { Text(suggestion.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            )
                        }
                    }
                }
            }

            // Use template button (G-03) — only when adding new item
            if (item == null) {
                item {
                    Surface(
                        onClick = {
                            onLoadTemplatesForPicker()
                            showPicker = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("📋", fontSize = 18.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Использовать шаблон",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                "→",
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                }
            }

            // Title
            item {
                SheetFieldLabel("Название *")
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("Введите название...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                )
            }

            // Shopping fields
            if (listType == "shopping") {
                item {
                    SheetFieldLabel("Количество и единица")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = quantity,
                            onValueChange = { quantity = it },
                            placeholder = { Text("1") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                        )
                        val units = listOf("шт", "л", "мл", "кг", "г", "уп", "пач")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(units) { u ->
                                FilterChip(
                                    selected = unit == u,
                                    onClick = { unit = u },
                                    label = { Text(u, fontSize = 12.sp) },
                                )
                            }
                        }
                    }
                }
            }

            // Home chore fields
            if (listType == "home_chores") {
                item {
                    SheetFieldLabel("Зона дома")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(homeZones) { zone ->
                            FilterChip(
                                selected = selectedZone == zone,
                                onClick = { selectedZone = if (selectedZone == zone) null else zone },
                                label = { Text(zone) },
                            )
                        }
                    }
                }
                item {
                    SheetFieldLabel("Расписание")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("1" to "Каждый день", "7" to "Еженедельно").forEach { (days, label) ->
                            FilterChip(
                                selected = intervalDays == days,
                                onClick = { intervalDays = if (intervalDays == days) "" else days },
                                label = { Text(label) },
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(dayOptions) { (dayKey, dayLabel) ->
                            FilterChip(
                                selected = dayKey in selectedDays,
                                onClick = {
                                    selectedDays = if (dayKey in selectedDays)
                                        selectedDays - dayKey else selectedDays + dayKey
                                },
                                label = { Text(dayLabel) },
                            )
                        }
                    }
                }
            }

            // Priority
            item {
                SheetFieldLabel("Приоритет")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("low" to "🟢 Низкий", "medium" to "🟡 Средний", "high" to "🔴 Высокий").forEach { (value, label) ->
                        FilterChip(
                            selected = priority == value,
                            onClick = { priority = value },
                            label = { Text(label, fontSize = 12.sp) },
                        )
                    }
                }
            }

            // Note
            item {
                SheetFieldLabel("Заметка")
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = { Text("Дополнительно...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                )
            }

            // Due date
            item {
                DatePickerField(
                    label = "Дедлайн",
                    dateValue = dueAt,
                    onDateSelected = { dueAt = it },
                    onClear = { dueAt = "" },
                )
            }

            // Assigned to
            if (isGroupList && members.isNotEmpty()) {
                item {
                    SheetFieldLabel("Исполнитель")
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
                                    assignedTo = if (assignedTo == member.userId) null else member.userId
                                },
                                label = { Text(member.displayName ?: member.userId) },
                            )
                        }
                    }
                }
            }

            // Shopping categories
            if (categories.isNotEmpty() && listType == "shopping") {
                item {
                    SheetFieldLabel("Категория")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories) { category ->
                            FilterChip(
                                selected = selectedCategory == category.name,
                                onClick = {
                                    selectedCategory = if (selectedCategory == category.name) null else category.name
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
                                        TextButton(onClick = {
                                            if (newCategoryName.isNotBlank()) {
                                                onCreateCategory(newCategoryName)
                                                selectedCategory = newCategoryName
                                                newCategoryName = ""
                                                showNewCategoryField = false
                                            }
                                        }) { Text("ОК") }
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

            // Submit button
            item {
                Surface(
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
                            (intervalDays.isNotBlank() || selectedZone != null ||
                                    selectedDays.isNotEmpty() || startDate.isNotBlank())
                        ) {
                            ChoreSchedule(
                                intervalDays = intervalDays.toIntOrNull(),
                                daysOfWeek = selectedDays.takeIf { it.isNotEmpty() },
                                startDate = startDate.takeIf { it.isNotBlank() }
                                    ?: Clock.System.todayIn(TimeZone.currentSystemDefault()).toString(),
                                endDate = endDate.takeIf { it.isNotBlank() },
                                category = selectedZone,
                            )
                        } else null
                        val dueAtRfc3339 = dueAt.takeIf { it.isNotBlank() }
                            ?.let { dateStr ->
                                if ("T" in dateStr) dateStr
                                else parseDateOrNull(dateStr)
                                    ?.atStartOfDayIn(TimeZone.currentSystemDefault())
                                    ?.toString() ?: dateStr
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
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = listColor,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            if (item == null) "Добавить" else "Сохранить",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }
                Spacer(Modifier.height(SweetHomeSpacing.lg))
            }
        }
    }

    if (showPicker) {
        TemplatePickerSheet(
            listType = listType,
            taskTemplates = taskTemplates,
            frequentItems = frequentItems,
            favoriteItems = favoriteItems,
            onPickTemplate = { template ->
                pickerScope.launch {
                    onResolveTaskTemplate(template.id)?.let { detail ->
                        title = detail.title
                        detail.note?.takeIf { it.isNotBlank() }?.let { note = it }
                        detail.priority?.let { priority = it }
                        detail.shoppingDetails?.let { sd ->
                            sd.quantity?.let {
                                quantity = if (it % 1 == 0.0) it.toLong().toString() else it.toString()
                            }
                            sd.unit?.let { unit = it }
                            sd.category?.let { selectedCategory = it }
                        }
                        detail.choreSchedule?.let { ch ->
                            ch.intervalDays?.let { intervalDays = it.toString() }
                            ch.daysOfWeek?.takeIf { it.isNotEmpty() }?.let { selectedDays = it }
                            ch.startDate?.takeIf { it.isNotBlank() }?.let { startDate = it }
                            ch.endDate?.takeIf { it.isNotBlank() }?.let { endDate = it }
                            ch.category?.let { selectedZone = it }
                        }
                    }
                }
                showPicker = false
            },
            onPickItem = { picked ->
                title = picked.title
                picked.note?.takeIf { it.isNotBlank() }?.let { note = it }
                picked.priority?.let { priority = it }
                picked.shopping?.let { sh ->
                    sh.quantity?.let {
                        quantity = if (it % 1 == 0.0) it.toLong().toString() else it.toString()
                    }
                    sh.unit?.let { unit = it }
                    sh.category?.let { selectedCategory = it }
                }
                picked.choreSchedule?.let { ch ->
                    ch.intervalDays?.let { intervalDays = it.toString() }
                    ch.daysOfWeek?.takeIf { it.isNotEmpty() }?.let { selectedDays = it }
                    ch.startDate?.takeIf { it.isNotBlank() }?.let { startDate = it }
                    ch.endDate?.takeIf { it.isNotBlank() }?.let { endDate = it }
                    ch.category?.let { selectedZone = it }
                }
                showPicker = false
            },
            onDismiss = { showPicker = false },
        )
    }
}

@Composable
private fun SheetFieldLabel(text: String) {
    Text(
        text = text.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 0.3.sp,
        modifier = Modifier.padding(bottom = 6.dp),
    )
}
