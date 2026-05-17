package com.jetbrains.kmpapp.screens.todo

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetbrains.kmpapp.data.groups.Group
import com.jetbrains.kmpapp.data.lists.TodoList
import com.jetbrains.kmpapp.ui.LocalCozyShapes
import com.jetbrains.kmpapp.ui.components.SweetHomeListCard
import com.jetbrains.kmpapp.ui.listColorForType
import com.jetbrains.kmpapp.ui.listEmojiForType
import com.jetbrains.kmpapp.ui.toComposeColor

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
    onListClick: (String) -> Unit,
    onCreateList: () -> Unit,
    onNavigateToTemplates: () -> Unit,
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
            placeholder = { Text("Поиск списков…") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = MaterialTheme.shapes.medium,
        )

        // Templates entry banner — F3
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
                GroupedLists(
                    lists = filteredLists,
                    groups = groups,
                    isGuest = isGuest,
                    navigateToLinkEmail = navigateToLinkEmail,
                    onListClick = onListClick,
                )
            } else {
                EmptyTodoListsContent(
                    modifier = Modifier.fillMaxSize(),
                    onCreateList = onCreateList,
                    isGuest = isGuest,
                    navigateToLinkEmail = navigateToLinkEmail,
                )
            }
        }
    }
}

@Composable
private fun GroupedLists(
    lists: List<TodoList>,
    groups: List<Group>,
    isGuest: Boolean,
    navigateToLinkEmail: (() -> Unit)?,
    onListClick: (String) -> Unit,
) {
    // Группируем списки по workspaceId. Personal-workspace всегда первым.
    val byWorkspace = lists.groupBy { it.workspaceId }
    val personal = groups.firstOrNull { it.type == "personal" }
    val rest = groups.filter { it.type != "personal" }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (isGuest && navigateToLinkEmail != null) {
            item(key = "guest_banner") {
                GuestLinkEmailBanner(onLinkEmail = navigateToLinkEmail)
            }
        }

        // Личное пространство сверху, если есть.
        personal?.let { renderSection(it, byWorkspace[it.id].orEmpty(), onListClick) }
        // Остальные группы.
        rest.forEach { group ->
            renderSection(group, byWorkspace[group.id].orEmpty(), onListClick)
        }
        // Списки без известного workspace (на случай рассинхронизации) — внизу.
        val knownIds = (listOfNotNull(personal) + rest).map { it.id }.toSet()
        val orphan = lists.filter { it.workspaceId !in knownIds }
        if (orphan.isNotEmpty()) {
            item(key = "orphan_header") { SectionHeader(title = "Прочее", subtitle = null) }
            items(orphan.chunked(2), key = { row -> "orphan_" + row.joinToString { it.id } }) { row ->
                ListsRow(row, onListClick)
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.renderSection(
    group: Group,
    items: List<TodoList>,
    onListClick: (String) -> Unit,
) {
    if (items.isEmpty()) return
    val displayTitle = if (group.type == "personal") "Личные" else group.title
    val subtitle = when (group.type) {
        "personal" -> null
        "family" -> "Семья · ${items.size}"
        "work" -> "Работа · ${items.size}"
        "mentoring" -> "Наставничество · ${items.size}"
        else -> "${items.size}"
    }
    item(key = "h_${group.id}") { SectionHeader(displayTitle, subtitle) }
    items(items.chunked(2), key = { row -> group.id + "_" + row.joinToString { it.id } }) { row ->
        ListsRow(row, onListClick)
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String?) {
    Column(modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        subtitle?.let {
            Text(
                text = it,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ListsRow(row: List<TodoList>, onListClick: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        row.forEach { list ->
            val listColor = list.color?.toComposeColor()
                ?: listColorForType(list.type, isSystemInDarkTheme())
            Box(modifier = Modifier.weight(1f)) {
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
        // Если в ряду одна карточка — добавляем фиктивный weight для выравнивания.
        if (row.size == 1) {
            Box(modifier = Modifier.weight(1f))
        }
    }
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
                    .background(
                        MaterialTheme.colorScheme.surface,
                        CircleShape,
                    ),
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
            Text(
                text = "Нет списков",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Нажмите + чтобы создать первый список дел",
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
