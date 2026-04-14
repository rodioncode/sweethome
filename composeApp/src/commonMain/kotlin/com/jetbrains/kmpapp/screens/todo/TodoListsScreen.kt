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
            onCreateList = { title, type, icon, scope, groupId ->
                viewModel.createList(title, type, icon, scope, groupId)
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
    onCreateList: (title: String, type: String, icon: String?, scope: String, groupId: String?) -> Unit,
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
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = WindowInsets.safeDrawing.asPaddingValues(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (isGuest && navigateToLinkEmail != null) {
                        item(key = "guest_banner") {
                            GuestLinkEmailBanner(
                                onLinkEmail = navigateToLinkEmail,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            )
                        }
                    }
                    items(filteredLists, key = { it.id }) { list ->
                        TodoListCard(
                            list = list,
                            onClick = { onListClick(list.id) },
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
            onConfirm = { title, type, icon, scope, groupId ->
                onCreateList(title, type, icon, scope, groupId)
                onShowCreateDialog(false)
            },
        )
    }
}

private val listTypeOptions = listOf(
    Triple("general_todos", "Общий", Icons.Default.List),
    Triple("shopping", "Покупки", Icons.Default.ShoppingCart),
    Triple("home_chores", "Дела по дому", Icons.Default.Home),
)

private val scopeOptions = listOf(
    "personal" to "Личный",
    "group" to "Групповой",
)

// Grid of list types shown in the bottom sheet
private val listTypeGrid = listOf(
    Triple("general_todos", "Задачи", "📋"),
    Triple("shopping", "Покупки", "🛒"),
    Triple("home_chores", "Дом", "🏠"),
    Triple("study", "Учёба", "📚"),
    Triple("travel", "Путешествие", "✈️"),
    Triple("custom", "Свой тип", "⭐"),
)

// Colors for the color picker
private val listColors = listOf(
    Color(0xFF5B7C5A),
    Color(0xFFE8A87C),
    Color(0xFFD4574E),
    Color(0xFF4A7FA5),
    Color(0xFF9B59B6),
    Color(0xFF95A5A6),
)

// Icons for the icon picker
private val listIconOptions = listOf("🛒", "🏠", "📋", "✅", "🎯", "🌿", "💡", "⭐")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CreateListBottomSheet(
    groups: List<Group> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (title: String, type: String, icon: String?, scope: String, groupId: String?) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var step by remember { mutableStateOf(1) } // 1 = type selection, 2 = form
    var selectedType by remember { mutableStateOf("general_todos") }
    var title by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(listColors[0]) }
    var selectedIcon by remember { mutableStateOf<String?>(null) }
    var description by remember { mutableStateOf("") }
    var selectedScope by remember { mutableStateOf("personal") }
    var selectedGroupId by remember { mutableStateOf<String?>(null) }
    var scopeExpanded by remember { mutableStateOf(false) }
    var groupExpanded by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        if (step == 1) {
            // Step 1: Type selection grid
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Тип списка",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    TextButton(onClick = onDismiss) {
                        Text("✕")
                    }
                }
                Spacer(Modifier.height(16.dp))

                // 2-column grid of type options
                val rows = listTypeGrid.chunked(2)
                rows.forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        rowItems.forEach { (type, label, emoji) ->
                            val isSelected = selectedType == type
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(80.dp)
                                    .clickable { selectedType = type },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant,
                                ),
                                shape = MaterialTheme.shapes.medium,
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                ) {
                                    Text(emoji, fontSize = 26.sp)
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        label,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isSelected)
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                        // Fill remaining space if odd item
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }

                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = { step = 2 },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Text("Далее", fontWeight = FontWeight.SemiBold)
                }
            }
        } else {
            // Step 2: Full create form
            val typeEmoji = listTypeGrid.find { it.first == selectedType }?.third ?: "📋"
            val typeName = listTypeGrid.find { it.first == selectedType }?.second ?: "Список"

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = { step = 1 }) { Text("← Назад") }
                    Text(
                        "Новый список",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    TextButton(onClick = onDismiss) { Text("✕") }
                }

                // Type tag
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(vertical = 8.dp),
                ) {
                    Text(
                        "$typeEmoji $typeName",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название") },
                    placeholder = { Text("Мой список") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                )

                Spacer(Modifier.height(16.dp))

                // Color picker
                Text(
                    "Цвет",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    listColors.forEach { color ->
                        val isSelected = selectedColor == color
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 36.dp else 32.dp)
                                .background(color, CircleShape)
                                .then(
                                    if (isSelected) Modifier.border(
                                        3.dp, MaterialTheme.colorScheme.onBackground, CircleShape
                                    ) else Modifier
                                )
                                .clickable { selectedColor = color },
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Icon picker
                Text(
                    "Иконка",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listIconOptions.forEach { emoji ->
                        val isSelected = selectedIcon == emoji
                        Surface(
                            modifier = Modifier
                                .size(40.dp)
                                .clickable { selectedIcon = if (isSelected) null else emoji },
                            shape = MaterialTheme.shapes.small,
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(emoji, fontSize = 18.sp)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание") },
                    placeholder = { Text("Добавьте описание (необязательно)") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                )

                Spacer(Modifier.height(16.dp))

                // Scope selector
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("personal" to "Личное", "group" to "Групповое").forEach { (scope, label) ->
                        FilterChip(
                            selected = selectedScope == scope,
                            onClick = {
                                selectedScope = scope
                                if (scope == "personal") selectedGroupId = null
                            },
                            label = { Text(label) },
                        )
                    }
                }

                // Group selection
                if (selectedScope == "group" && groups.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    ExposedDropdownMenuBox(
                        expanded = groupExpanded,
                        onExpandedChange = { groupExpanded = it },
                    ) {
                        OutlinedTextField(
                            value = groups.find { it.id == selectedGroupId }?.name ?: "Выберите группу",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Группа") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = groupExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            shape = MaterialTheme.shapes.medium,
                        )
                        ExposedDropdownMenu(
                            expanded = groupExpanded,
                            onDismissRequest = { groupExpanded = false },
                        ) {
                            groups.forEach { group ->
                                DropdownMenuItem(
                                    text = { Text(group.name) },
                                    onClick = {
                                        selectedGroupId = group.id
                                        groupExpanded = false
                                    },
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        val finalType = when (selectedType) {
                            "study", "travel", "custom" -> "general_todos"
                            else -> selectedType
                        }
                        onConfirm(
                            title.ifBlank { "Мой список" },
                            finalType,
                            selectedIcon,
                            selectedScope,
                            selectedGroupId,
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    enabled = selectedScope == "personal" || selectedGroupId != null,
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Text("Создать список", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// Keep the old dialog for backwards compatibility within the screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CreateListDialog(
    groups: List<Group> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (title: String, type: String, icon: String?, scope: String, groupId: String?) -> Unit,
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
