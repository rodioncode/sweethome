package com.jetbrains.kmpapp.screens.todo

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jetbrains.kmpapp.data.groups.Group
import com.jetbrains.kmpapp.data.groups.WorkspaceType
import com.jetbrains.kmpapp.data.lists.TodoList
import com.jetbrains.kmpapp.ui.LocalCozyExtraColors
import com.jetbrains.kmpapp.ui.LocalCozyShapes
import com.jetbrains.kmpapp.ui.components.CozyChip
import com.jetbrains.kmpapp.ui.components.EmptyHero
import com.jetbrains.kmpapp.ui.components.SweetHomeListCard
import com.jetbrains.kmpapp.ui.listColorForType
import com.jetbrains.kmpapp.ui.listEmojiForType
import com.jetbrains.kmpapp.ui.toComposeColor
import org.koin.compose.viewmodel.koinViewModel

private data class TypeFilter(val key: String?, val label: String)

private val FILTER_OPTIONS = listOf(
    TypeFilter(null, "Все"),
    TypeFilter("shopping", "Покупки"),
    TypeFilter("home_chores", "Домашние"),
    TypeFilter("general_todos", "Задачи"),
    TypeFilter("wishlist", "Вишлист"),
    TypeFilter("media", "Медиа"),
)

@Composable
internal fun TodoListsContent(
    lists: List<TodoList>,           // legacy param kept for caller; not used (VM is source of truth)
    groups: List<Group> = emptyList(),
    contentPadding: PaddingValues,
    onListClick: (String) -> Unit,
    onCreateList: () -> Unit,
    onNavigateToTemplates: () -> Unit,
    isGuest: Boolean = false,
    navigateToLinkEmail: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val viewModel = koinViewModel<TodoListsViewModel>()
    val allGroups by viewModel.groups.collectAsStateWithLifecycle()
    val activeWs by viewModel.activeWorkspace.collectAsStateWithLifecycle()
    val visibleLists by viewModel.visibleLists.collectAsStateWithLifecycle()
    val typeFilter by viewModel.typeFilter.collectAsStateWithLifecycle()
    val query by viewModel.searchQuery.collectAsStateWithLifecycle()

    var showWorkspaceSheet by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    val workspaces = remember(allGroups) { allGroups.filter { it.archivedAt == null } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding),
    ) {
        // Top bar: workspace dropdown + actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            WorkspaceDropdownTrigger(
                workspace = activeWs,
                onClick = { showWorkspaceSheet = true },
                modifier = Modifier.weight(1f),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconCircleButton(
                    text = if (showSearch) "×" else "🔍",
                    onClick = {
                        showSearch = !showSearch
                        if (!showSearch) viewModel.setSearchQuery("")
                    },
                )
            }
        }

        if (showSearch) {
            SearchBar(
                value = query,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
            )
        }

        // Templates entry banner
        TemplatesBanner(
            onClick = onNavigateToTemplates,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
        )

        Spacer(Modifier.height(4.dp))

        // Filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FILTER_OPTIONS.forEach { option ->
                CozyChip(
                    label = option.label,
                    selected = typeFilter == option.key,
                    onClick = {
                        viewModel.setTypeFilter(if (typeFilter == option.key) null else option.key)
                    },
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        AnimatedContent(visibleLists.isNotEmpty()) { hasLists ->
            if (hasLists) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (isGuest && navigateToLinkEmail != null) {
                        item("guest") { GuestLinkEmailBanner(onLinkEmail = navigateToLinkEmail) }
                    }
                    val pinned = visibleLists.filter { it.pinnedAt != null }
                    val regular = visibleLists.filter { it.pinnedAt == null }
                    if (pinned.isNotEmpty()) {
                        item("h_pin") { SectionLabel("📌 ЗАКРЕПЛЕНО · ${pinned.size}") }
                        items(pinned.chunked(2), key = { it.joinToString("_") { l -> "pin_${l.id}" } }) { row ->
                            ListsRow(row, onListClick, viewModel::togglePinned)
                        }
                    }
                    if (regular.isNotEmpty()) {
                        if (pinned.isNotEmpty()) {
                            item("h_all") { SectionLabel("СПИСКИ · ${regular.size}") }
                        }
                        items(regular.chunked(2), key = { it.joinToString("_") { l -> l.id } }) { row ->
                            ListsRow(row, onListClick, viewModel::togglePinned)
                        }
                    }
                }
            } else {
                EmptyTodoListsContent(
                    modifier = Modifier.fillMaxSize(),
                    onCreateList = onCreateList,
                    isGuest = isGuest,
                    navigateToLinkEmail = navigateToLinkEmail,
                    workspaceName = activeWs?.title,
                )
            }
        }
    }

    if (showWorkspaceSheet) {
        WorkspaceSelectorSheet(
            workspaces = workspaces,
            activeId = activeWs?.id,
            onSelect = { id ->
                viewModel.selectWorkspace(id)
                showWorkspaceSheet = false
            },
            onDismiss = { showWorkspaceSheet = false },
        )
    }
}

@Composable
private fun WorkspaceDropdownTrigger(
    workspace: Group?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shapes = LocalCozyShapes.current
    Row(
        modifier = modifier
            .clip(shapes.chip)
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(workspaceEmoji(workspace), fontSize = 18.sp)
        Text(
            workspace?.title ?: "Все списки",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text("▾", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun workspaceEmoji(workspace: Group?): String = when (workspace?.type) {
    WorkspaceType.PERSONAL -> "🌿"
    WorkspaceType.FAMILY -> "🏡"
    WorkspaceType.WORK -> "💼"
    WorkspaceType.MENTORING -> "🎓"
    "hobby" -> "🎯"
    "study" -> "📚"
    else -> "👥"
}

@Composable
private fun IconCircleButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
private fun SearchBar(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val shapes = LocalCozyShapes.current
    val extras = LocalCozyExtraColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(shapes.chip)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shapes.chip)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("🔍", fontSize = 14.sp, color = extras.textTer)
        Box(modifier = Modifier.weight(1f)) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { inner ->
                    if (value.isEmpty()) {
                        Text("Поиск списков…", fontSize = 13.sp, color = extras.textTer)
                    }
                    inner()
                },
            )
        }
        if (value.isNotEmpty()) {
            Text(
                "×",
                fontSize = 16.sp,
                color = extras.textTer,
                modifier = Modifier.clickable { onValueChange("") },
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 1.2.sp,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
    )
}

@Composable
private fun ListsRow(
    row: List<TodoList>,
    onListClick: (String) -> Unit,
    onTogglePin: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        row.forEach { list ->
            val listColor: Color = list.color?.toComposeColor()
                ?: listColorForType(list.type, isSystemInDarkTheme())
            Box(modifier = Modifier.weight(1f)) {
                ListCardWithPin(
                    list = list,
                    listColor = listColor,
                    onClick = { onListClick(list.id) },
                    onTogglePin = { onTogglePin(list.id) },
                )
            }
        }
        if (row.size == 1) {
            Box(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun ListCardWithPin(
    list: TodoList,
    listColor: Color,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
) {
    Box {
        SweetHomeListCard(
            title = list.title,
            onClick = onClick,
            icon = list.icon ?: listEmojiForType(list.type),
            listColor = listColor,
            doneCount = list.doneCount,
            totalCount = list.totalCount,
        )
        val isPinned = list.pinnedAt != null
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp)
                .size(28.dp)
                .clip(CircleShape)
                .background(
                    if (isPinned) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                )
                .clickable(onClick = onTogglePin),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                if (isPinned) "📌" else "☆",
                fontSize = if (isPinned) 13.sp else 16.sp,
                color = if (isPinned) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkspaceSelectorSheet(
    workspaces: List<Group>,
    activeId: String?,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val shapes = LocalCozyShapes.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = shapes.sheet,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                "Ваши пространства",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            workspaces.forEach { ws ->
                WorkspaceRow(
                    workspace = ws,
                    selected = ws.id == activeId,
                    onClick = { onSelect(ws.id) },
                )
            }
        }
    }
}

@Composable
private fun WorkspaceRow(
    workspace: Group,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val shapes = LocalCozyShapes.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shapes.chip)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(workspaceEmoji(workspace), fontSize = 22.sp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                workspace.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                workspaceTypeLabel(workspace.type),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (selected) {
            Text("✓", fontSize = 18.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
    }
}

private fun workspaceTypeLabel(type: String): String = when (type) {
    WorkspaceType.PERSONAL -> "Личное"
    WorkspaceType.FAMILY -> "Семья"
    WorkspaceType.WORK -> "Работа"
    WorkspaceType.MENTORING -> "Наставничество"
    "hobby" -> "Хобби"
    "study" -> "Учёба"
    WorkspaceType.GROUP -> "Группа"
    else -> "Пространство"
}

@Composable
private fun TemplatesBanner(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = LocalCozyShapes.current.button,
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text("📋", fontSize = 18.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Шаблоны",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = "Готовые списки и задачи",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                )
            }
            Text(
                "→",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun GuestLinkEmailBanner(
    onLinkEmail: () -> Unit,
    modifier: Modifier = Modifier,
) {
    com.jetbrains.kmpapp.ui.components.CozyCard(
        modifier = modifier.fillMaxWidth(),
        background = MaterialTheme.colorScheme.secondaryContainer,
        contentPadding = 12.dp,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Привяжите email, чтобы не\nпотерять свои данные",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
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
    workspaceName: String? = null,
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
            EmptyHero(emoji = "🏠", decor = listOf("✨", "🌿", "☁️", "🍃"))
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (workspaceName != null) "Здесь пока пусто" else "Нет списков",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = if (workspaceName != null) {
                    "В пространстве «$workspaceName» нет списков. Создайте первый."
                } else {
                    "Нажмите + чтобы создать первый список дел"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Surface(
                onClick = onCreateList,
                shape = LocalCozyShapes.current.button,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.height(44.dp),
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "Создать список",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}
