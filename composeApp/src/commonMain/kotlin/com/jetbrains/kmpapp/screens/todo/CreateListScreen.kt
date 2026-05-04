package com.jetbrains.kmpapp.screens.todo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import org.koin.compose.viewmodel.koinViewModel

private data class ListTypeOption(
    val type: String,
    val emoji: String,
    val label: String,
    val desc: String,
)

private val listTypeOptions = listOf(
    ListTypeOption("shopping", "🛒", "Список покупок", "Продукты, товары, покупки"),
    ListTypeOption("home_chores", "🏠", "Домашние дела", "Уборка, ремонт, расписание"),
    ListTypeOption("general_todos", "✅", "Задачи", "Дела с дедлайнами"),
    ListTypeOption("study", "📚", "Учёба", "Курсы, книги, обучение"),
    ListTypeOption("travel", "✈️", "Поездка", "Вещи, документы, маршрут"),
    ListTypeOption("wishlist", "🎁", "Список желаний", "Подарки, мечты, хотелки"),
    ListTypeOption("media", "🎬", "Книги/Фильмы", "Что почитать и посмотреть"),
    ListTypeOption("custom", "📝", "Произвольный", "Что-то своё"),
)

private val listColorPresets = listOf(
    Color(0xFFFF7043) to "#FF7043",
    Color(0xFF42A5F5) to "#42A5F5",
    Color(0xFF66BB6A) to "#66BB6A",
    Color(0xFFAB47BC) to "#AB47BC",
    Color(0xFFFFA726) to "#FFA726",
    Color(0xFFEC407A) to "#EC407A",
    Color(0xFF78909C) to "#78909C",
)

private val listIconOptions = listOf(
    "📋", "🛍", "📦", "🎯", "💡", "🌿",
    "🏋️", "🍳", "💊", "🐾", "🎵", "💼",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateListScreen(
    initialWorkspaceId: String?,
    onCreated: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val viewModel = koinViewModel<TodoListsViewModel>()
    val groups by viewModel.groups.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    var step by remember { mutableStateOf(1) }
    var selectedType by remember { mutableStateOf<ListTypeOption?>(null) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedColorIndex by remember { mutableStateOf(0) }
    var selectedIcon by remember { mutableStateOf<String?>(null) }
    val personalWorkspace = groups.firstOrNull { it.type == "personal" }
    val nonPersonalWorkspaces = groups.filter { it.type != "personal" }
    var selectedWorkspaceId by remember(personalWorkspace, initialWorkspaceId) {
        mutableStateOf(initialWorkspaceId ?: personalWorkspace?.id ?: "")
    }
    var groupExpanded by remember { mutableStateOf(false) }
    var submitted by remember { mutableStateOf(false) }

    // Закрываем экран после успешного создания (когда репозиторий перестал ругаться).
    LaunchedEffect(submitted, error) {
        if (submitted && error == null) {
            onCreated()
        }
    }

    val selectedColor = listColorPresets[selectedColorIndex].first
    val selectedColorHex = listColorPresets[selectedColorIndex].second

    Scaffold(
        contentWindowInsets = WindowInsets.systemBars,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (step == 1) "Тип списка" else "Новый список",
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (step == 2) step = 1 else onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    Text(
                        "$step/2",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 16.dp),
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = step * 0.5f)
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.primary),
                )
            }

            if (step == 1) {
                Step1TypePicker(
                    selectedType = selectedType,
                    onSelect = { type ->
                        selectedType = type
                        if (selectedIcon == null) selectedIcon = type.emoji
                        step = 2
                    },
                )
            } else {
                val type = selectedType ?: run {
                    step = 1
                    return@Column
                }
                Step2Configure(
                    typeOption = type,
                    title = title,
                    onTitleChange = { title = it },
                    description = description,
                    onDescriptionChange = { description = it },
                    selectedColor = selectedColor,
                    selectedColorHex = selectedColorHex,
                    selectedColorIndex = selectedColorIndex,
                    onColorIndexChange = { selectedColorIndex = it },
                    selectedIcon = selectedIcon,
                    onIconChange = { selectedIcon = it },
                    personalWorkspace = personalWorkspace,
                    nonPersonalWorkspaces = nonPersonalWorkspaces,
                    selectedWorkspaceId = selectedWorkspaceId,
                    onWorkspaceChange = { selectedWorkspaceId = it },
                    groupExpanded = groupExpanded,
                    onGroupExpandedChange = { groupExpanded = it },
                    onCreate = {
                        if (selectedWorkspaceId.isNotBlank() && title.isNotBlank()) {
                            viewModel.createList(
                                title = title,
                                type = type.type,
                                workspaceId = selectedWorkspaceId,
                                icon = selectedIcon,
                                color = selectedColorHex,
                            )
                            submitted = true
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun Step1TypePicker(
    selectedType: ListTypeOption?,
    onSelect: (ListTypeOption) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            "Что будем отслеживать?",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 10.dp),
        )
        listTypeOptions.forEach { option ->
            val isSelected = selectedType?.type == option.type
            Surface(
                onClick = { onSelect(option) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline,
                ),
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
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(12.dp),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(option.emoji, fontSize = 22.sp)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            option.label,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            option.desc,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text("›", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Step2Configure(
    typeOption: ListTypeOption,
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    selectedColor: Color,
    selectedColorHex: String,
    selectedColorIndex: Int,
    onColorIndexChange: (Int) -> Unit,
    selectedIcon: String?,
    onIconChange: (String?) -> Unit,
    personalWorkspace: Group?,
    nonPersonalWorkspaces: List<Group>,
    selectedWorkspaceId: String,
    onWorkspaceChange: (String) -> Unit,
    groupExpanded: Boolean,
    onGroupExpandedChange: (Boolean) -> Unit,
    onCreate: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            "${typeOption.emoji} ${typeOption.label} · ${typeOption.desc}",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // Title field
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            FieldLabel("НАЗВАНИЕ *")
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                placeholder = {
                    Text(
                        "Например: Продукты на неделю",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
            )
        }

        // Description field
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            FieldLabel("ОПИСАНИЕ")
            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                placeholder = {
                    Text(
                        "Для чего этот список…",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                maxLines = 2,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
            )
        }

        // Workspace selector
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FieldLabel("ПРОСТРАНСТВО")
            val allWorkspaces = buildList {
                personalWorkspace?.let { add(it.id to "Личное") }
                nonPersonalWorkspaces.forEach { add(it.id to it.title) }
            }
            if (allWorkspaces.size <= 1) {
                allWorkspaces.firstOrNull()?.let { (_, label) ->
                    FilterChip(selected = true, onClick = {}, label = { Text(label) })
                }
            } else {
                ExposedDropdownMenuBox(
                    expanded = groupExpanded,
                    onExpandedChange = onGroupExpandedChange,
                ) {
                    OutlinedTextField(
                        value = allWorkspaces.find { it.first == selectedWorkspaceId }?.second
                            ?: "Выберите пространство",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = groupExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        shape = MaterialTheme.shapes.medium,
                    )
                    ExposedDropdownMenu(
                        expanded = groupExpanded,
                        onDismissRequest = { onGroupExpandedChange(false) },
                    ) {
                        allWorkspaces.forEach { (id, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    onWorkspaceChange(id)
                                    onGroupExpandedChange(false)
                                },
                            )
                        }
                    }
                }
            }
        }

        // Icon picker
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FieldLabel("ИКОНКА")
            val iconRows = listIconOptions.chunked(6)
            iconRows.forEach { rowIcons ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowIcons.forEach { emoji ->
                        val isSelected = selectedIcon == emoji
                        Surface(
                            onClick = { onIconChange(if (isSelected) null else emoji) },
                            modifier = Modifier.size(40.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isSelected) {
                                androidx.compose.foundation.BorderStroke(
                                    1.5.dp,
                                    MaterialTheme.colorScheme.primary,
                                )
                            } else null,
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
            FieldLabel("ЦВЕТ")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listColorPresets.forEachIndexed { index, (color, _) ->
                    val isSelected = selectedColorIndex == index
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(color, CircleShape)
                            .then(
                                if (isSelected) {
                                    Modifier.border(
                                        3.dp,
                                        MaterialTheme.colorScheme.onSurface,
                                        CircleShape,
                                    )
                                } else Modifier,
                            )
                            .clickable { onColorIndexChange(index) },
                    )
                }
            }
        }

        // Preview card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outline,
            ),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            ) {
                Box(modifier = Modifier.width(4.dp).fillMaxSize().background(selectedColor))
                Column(modifier = Modifier.weight(1f).padding(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(selectedIcon ?: typeOption.emoji, fontSize = 18.sp)
                        Text(
                            title.ifBlank { "Название списка" },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "0 элементов",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // Create button
        Surface(
            onClick = onCreate,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            color = if (title.isNotBlank()) selectedColor else MaterialTheme.colorScheme.outline,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    "Создать список",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 0.3.sp,
    )
}
