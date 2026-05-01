package com.jetbrains.kmpapp.screens.todo

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jetbrains.kmpapp.auth.AuthViewModel
import com.jetbrains.kmpapp.data.groups.Group
import com.jetbrains.kmpapp.data.lists.TodoList
import com.jetbrains.kmpapp.ui.PrimaryGreen
import com.jetbrains.kmpapp.ui.listColorForType
import com.jetbrains.kmpapp.ui.listEmojiForType
import com.jetbrains.kmpapp.ui.toComposeColor
import com.jetbrains.kmpapp.ui.components.SweetHomeListCard
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TodoListsScreen(
    navigateToListDetail: (String) -> Unit,
) {
    val viewModel = koinViewModel<TodoListsViewModel>()
    val authViewModel = koinViewModel<AuthViewModel>()
    val lists by viewModel.lists.collectAsStateWithLifecycle()
    val groups by viewModel.groups.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showCreateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(error) {
        error?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearError()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sweet Home") },
                actions = {
                    IconButton(onClick = { authViewModel.logout() }) {
                        Icon(Icons.Default.ExitToApp, "Выйти")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                content = { Icon(Icons.Default.Add, "Добавить список") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        TodoListsContent(
            lists = lists,
            groups = groups,
            contentPadding = paddingValues,
            showCreateDialog = showCreateDialog,
            onShowCreateDialog = { showCreateDialog = it },
            onCreateList = { title, type, icon, color, workspaceId ->
                viewModel.createList(title, type, workspaceId, icon, color)
            },
            onListClick = navigateToListDetail,
        )
    }
}

private val filterOptions = listOf(
    null to "Все",
    "general_todos" to "Общие",
    "shopping" to "Покупки",
    "home_chores" to "Дела по дому",
)

@Composable
internal fun TodoListsContent(
    lists: List<TodoList>,
    groups: List<Group> = emptyList(),
    contentPadding: PaddingValues,
    showCreateDialog: Boolean,
    onShowCreateDialog: (Boolean) -> Unit,
    onCreateList: (title: String, type: String, icon: String?, color: String?, workspaceId: String) -> Unit,
    onListClick: (String) -> Unit,
    isGuest: Boolean = false,
    navigateToLinkEmail: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf<String?>(null) }

    val filteredLists = remember(lists, searchQuery, selectedFilter) {
        lists.filter { list ->
            val matchesSearch = searchQuery.isBlank() || list.title.contains(searchQuery, ignoreCase = true)
            val matchesFilter = selectedFilter == null || list.type == selectedFilter
            matchesSearch && matchesFilter
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding),
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Поиск списков...") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = MaterialTheme.shapes.medium,
        )

        // Filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            filterOptions.forEach { (type, label) ->
                FilterChip(
                    selected = selectedFilter == type,
                    onClick = { selectedFilter = if (selectedFilter == type) null else type },
                    label = { Text(label) },
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        AnimatedContent(filteredLists.isNotEmpty()) { hasLists ->
            if (hasLists) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp, end = 16.dp,
                        top = 8.dp,
                        bottom = WindowInsets.safeDrawing.asPaddingValues().calculateBottomPadding() + 80.dp,
                    ),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (isGuest && navigateToLinkEmail != null) {
                        gridItems(listOf("guest_banner")) {
                            GuestLinkEmailBanner(
                                onLinkEmail = navigateToLinkEmail,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                    gridItems(filteredLists, key = { it.id }) { list ->
                        val listColor = list.color?.toComposeColor() ?: listColorForType(list.type)
                        SweetHomeListCard(
                            title = list.title,
                            onClick = { onListClick(list.id) },
                            icon = list.icon ?: listEmojiForType(list.type),
                            listColor = listColor,
                            doneCount = list.doneCount,
                            totalCount = list.totalCount,
                        )
                    }
                }
            } else {
                EmptyTodoListsContent(
                    modifier = Modifier.fillMaxSize(),
                    onCreateList = { onShowCreateDialog(true) },
                    isGuest = isGuest,
                    navigateToLinkEmail = navigateToLinkEmail,
                )
            }
        }
    }

    if (showCreateDialog) {
        CreateListDialog(
            groups = groups,
            onDismiss = { onShowCreateDialog(false) },
            onConfirm = { title, type, icon, color, workspaceId ->
                onCreateList(title, type, icon, color, workspaceId)
                onShowCreateDialog(false)
            },
        )
    }
}

// List types with label, emoji and description
private data class ListTypeOption(val type: String, val emoji: String, val label: String, val desc: String)

private val listTypeGrid = listOf(
    ListTypeOption("shopping",      "🛒", "Список покупок",  "Продукты, товары, покупки"),
    ListTypeOption("home_chores",   "🏠", "Домашние дела",   "Уборка, ремонт, расписание"),
    ListTypeOption("general_todos", "✅", "Задачи",           "Дела с дедлайнами"),
    ListTypeOption("study",         "📚", "Учёба",            "Курсы, книги, обучение"),
    ListTypeOption("travel",        "✈️", "Поездка",          "Вещи, документы, маршрут"),
)

// Colors for the color picker — 7 design presets (color → hex)
private val listColorPresets = listOf(
    Color(0xFFFF7043) to "#FF7043", // Coral
    Color(0xFF42A5F5) to "#42A5F5", // Sky
    Color(0xFF66BB6A) to "#66BB6A", // Mint
    Color(0xFFAB47BC) to "#AB47BC", // Lavender
    Color(0xFFFFA726) to "#FFA726", // Amber
    Color(0xFFEC407A) to "#EC407A", // Rose
    Color(0xFF78909C) to "#78909C", // Slate
)

// Icons for the icon picker
private val listIconOptions = listOf("📋", "🛍", "📦", "🎯", "💡", "🌿", "🏋️", "🍳", "💊", "🐾", "🎵", "💼")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CreateListBottomSheet(
    groups: List<Group> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (title: String, type: String, icon: String?, color: String?, workspaceId: String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var step by remember { mutableStateOf(1) }
    var selectedTypeOption by remember { mutableStateOf(listTypeGrid[2]) } // default: general_todos
    var title by remember { mutableStateOf("") }
    var selectedColorIndex by remember { mutableStateOf(0) }
    val selectedColor = listColorPresets[selectedColorIndex].first
    val selectedColorHex = listColorPresets[selectedColorIndex].second
    var selectedIcon by remember { mutableStateOf<String?>(null) }
    var description by remember { mutableStateOf("") }
    val personalWorkspace = groups.firstOrNull { it.type == "personal" }
    val nonPersonalWorkspaces = groups.filter { it.type != "personal" }
    var selectedWorkspaceId by remember(personalWorkspace) { mutableStateOf(personalWorkspace?.id ?: "") }
    var groupExpanded by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = com.jetbrains.kmpapp.ui.SurfaceWhite,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        // Header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (step == 2) {
                TextButton(onClick = { step = 1 }) {
                    Text("‹ Назад", color = PrimaryGreen, fontWeight = FontWeight.SemiBold)
                }
            } else {
                Spacer(Modifier.width(72.dp))
            }
            Text(
                "Новый список",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "$step/2",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = onDismiss) { Text("✕") }
            }
        }

        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(com.jetbrains.kmpapp.ui.DividerColor),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = step * 0.5f)
                    .fillMaxSize()
                    .background(PrimaryGreen),
            )
        }

        if (step == 1) {
            // Step 1: Vertical list of type cards
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    "Выберите тип",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    "Что будем отслеживать?",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 10.dp),
                )
                listTypeGrid.forEach { option ->
                    Surface(
                        onClick = {
                            selectedTypeOption = option
                            selectedIcon = option.emoji
                            step = 2
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                        color = com.jetbrains.kmpapp.ui.SurfaceWhite,
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            if (selectedTypeOption.type == option.type) PrimaryGreen
                            else com.jetbrains.kmpapp.ui.DividerColor
                        ),
                        shadowElevation = 0.dp,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp, 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(com.jetbrains.kmpapp.ui.SurfaceVariantCream, androidx.compose.foundation.shape.RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(option.emoji, fontSize = 22.sp)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(option.label, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text(option.desc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text("›", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
        } else {
            // Step 2: Configure
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column {
                    Text("Настройте список", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text(
                        "${selectedTypeOption.label} · ${selectedTypeOption.desc}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                // Title field
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("НАЗВАНИЕ *", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.3.sp)
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("Например: Продукты на неделю", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                    )
                }

                // Description field
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("ОПИСАНИЕ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.3.sp)
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = { Text("Для чего этот список...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth(),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                    )
                }

                // Workspace selector
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("ПРОСТРАНСТВО", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.3.sp)
                    val allWorkspaces = buildList {
                        personalWorkspace?.let { add(it.id to "Личное") }
                        nonPersonalWorkspaces.forEach { add(it.id to it.title) }
                    }
                    if (allWorkspaces.size <= 1) {
                        // Single workspace — show as a chip
                        allWorkspaces.firstOrNull()?.let { (id, label) ->
                            FilterChip(selected = true, onClick = {}, label = { Text(label) })
                        }
                    } else {
                        ExposedDropdownMenuBox(
                            expanded = groupExpanded,
                            onExpandedChange = { groupExpanded = it },
                        ) {
                            OutlinedTextField(
                                value = allWorkspaces.find { it.first == selectedWorkspaceId }?.second ?: "Выберите пространство",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = groupExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                shape = MaterialTheme.shapes.medium,
                            )
                            ExposedDropdownMenu(expanded = groupExpanded, onDismissRequest = { groupExpanded = false }) {
                                allWorkspaces.forEach { (id, label) ->
                                    DropdownMenuItem(
                                        text = { Text(label) },
                                        onClick = { selectedWorkspaceId = id; groupExpanded = false },
                                    )
                                }
                            }
                        }
                    }
                }

                // Icon picker
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("ИКОНКА", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.3.sp)
                    val iconRows = listIconOptions.chunked(6)
                    iconRows.forEach { rowIcons ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowIcons.forEach { emoji ->
                                val isSelected = selectedIcon == emoji
                                Surface(
                                    onClick = { selectedIcon = if (isSelected) null else emoji },
                                    modifier = Modifier.size(40.dp),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                                    color = if (isSelected) PrimaryGreen.copy(alpha = 0.15f)
                                    else com.jetbrains.kmpapp.ui.SurfaceVariantCream,
                                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryGreen) else null,
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(emoji, fontSize = 18.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Color picker
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("ЦВЕТ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.3.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listColorPresets.forEachIndexed { index, (color, _) ->
                            val isSelected = selectedColorIndex == index
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(color, CircleShape)
                                    .then(
                                        if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                        else Modifier
                                    )
                                    .clickable { selectedColorIndex = index },
                            )
                        }
                    }
                }

                // Preview card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                    color = com.jetbrains.kmpapp.ui.SurfaceWhite,
                    border = androidx.compose.foundation.BorderStroke(1.dp, com.jetbrains.kmpapp.ui.DividerColor),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(androidx.compose.foundation.layout.IntrinsicSize.Min),
                    ) {
                        Box(modifier = Modifier.width(4.dp).fillMaxSize().background(selectedColor))
                        Column(modifier = Modifier.weight(1f).padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(selectedIcon ?: selectedTypeOption.emoji, fontSize = 18.sp)
                                Text(
                                    title.ifBlank { "Название списка" },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .background(com.jetbrains.kmpapp.ui.DividerColor, androidx.compose.foundation.shape.RoundedCornerShape(2.dp)),
                            ) {
                                Box(modifier = Modifier.fillMaxWidth(0f).fillMaxSize().background(selectedColor, androidx.compose.foundation.shape.RoundedCornerShape(2.dp)))
                            }
                            Spacer(Modifier.height(4.dp))
                            Text("0 элементов", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // Create button
                Surface(
                    onClick = {
                        if (selectedWorkspaceId.isNotBlank()) {
                            onConfirm(
                                title.ifBlank { "Мой список" },
                                selectedTypeOption.type,
                                selectedIcon,
                                selectedColorHex,
                                selectedWorkspaceId,
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    color = if (title.isNotBlank()) selectedColor else com.jetbrains.kmpapp.ui.DividerColor,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            "Создать список",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = com.jetbrains.kmpapp.ui.OnPrimaryWhite,
                        )
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CreateListDialog(
    groups: List<Group> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (title: String, type: String, icon: String?, color: String?, workspaceId: String) -> Unit,
) {
    CreateListBottomSheet(groups = groups, onDismiss = onDismiss, onConfirm = onConfirm)
}

private fun listTypeIcon(type: String) = when (type) {
    "shopping" -> Icons.Default.ShoppingCart
    "home_chores" -> Icons.Default.Home
    else -> Icons.Default.List
}

private fun listTypeLabel(type: String) = when (type) {
    "shopping" -> "Покупки"
    "home_chores" -> "Дела по дому"
    else -> "Общий"
}

@Composable
internal fun TodoListCard(
    list: TodoList,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (!list.icon.isNullOrBlank()) {
                Text(
                    text = list.icon,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(end = 12.dp),
                )
            } else {
                Icon(
                    listTypeIcon(list.type),
                    contentDescription = null,
                    modifier = Modifier.padding(end = 12.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = list.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = listTypeLabel(list.type),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun GuestLinkEmailBanner(
    onLinkEmail: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Привяжите email, чтобы не\nпотерять свои данные",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onLinkEmail) {
                Text("Привязать")
            }
        }
    }
}

@Composable
private fun EmptyTodoListsContent(
    modifier: Modifier = Modifier,
    onCreateList: () -> Unit,
    isGuest: Boolean = false,
    navigateToLinkEmail: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (isGuest && navigateToLinkEmail != null) {
            GuestLinkEmailBanner(
                onLinkEmail = navigateToLinkEmail,
                modifier = Modifier.padding(bottom = 16.dp),
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(text = "Нет списков", style = MaterialTheme.typography.titleLarge)
            Text(
                text = "Нажмите + чтобы создать первый список дел",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
