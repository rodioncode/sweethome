package com.jetbrains.kmpapp.screens.family

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jetbrains.kmpapp.data.groups.GroupMember
import com.jetbrains.kmpapp.data.lists.TodoItem
import com.jetbrains.kmpapp.data.lists.TodoList
import com.jetbrains.kmpapp.ui.PriorityHigh
import com.jetbrains.kmpapp.ui.PriorityLow
import com.jetbrains.kmpapp.ui.PriorityMedium
import com.jetbrains.kmpapp.ui.SweetHomeShapes
import com.jetbrains.kmpapp.ui.SweetHomeSpacing
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun FamilyContent(
    contentPadding: PaddingValues,
    onSpaceClick: (groupId: String, groupName: String) -> Unit,
    onListClick: (String) -> Unit,
    navigateToGamification: () -> Unit = {},
    navigateToShop: () -> Unit = {},
    navigateToGoals: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val viewModel = koinViewModel<FamilyViewModel>()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val familySpace by viewModel.familySpace.collectAsStateWithLifecycle()
    val familyLists by viewModel.familyLists.collectAsStateWithLifecycle()
    val balance by viewModel.balance.collectAsStateWithLifecycle()
    val isCreating by viewModel.isCreating.collectAsStateWithLifecycle()
    val isCreatingRoom by viewModel.isCreatingRoom.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val rooms by viewModel.rooms.collectAsStateWithLifecycle()
    val selectedRoomId by viewModel.selectedRoomId.collectAsStateWithLifecycle()
    val filters by viewModel.filters.collectAsStateWithLifecycle()
    val filteredChores by viewModel.filteredChores.collectAsStateWithLifecycle()
    val familyMembers by viewModel.familyMembers.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showCreateRoomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(familySpace) {
        if (familySpace != null) viewModel.loadFamilyLists()
    }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding),
    ) {
        SnackbarHost(snackbarHostState)

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (familySpace == null) {
            EmptyFamilyState(
                isCreating = isCreating,
                onCreateClick = { showCreateDialog = true },
            )
        } else {
            FamilyHomeContent(
                spaceName = familySpace!!.title,
                memberCount = familyMembers.size,
                lists = familyLists,
                balance = balance,
                rooms = rooms,
                selectedRoomId = selectedRoomId,
                filters = filters,
                filteredChores = filteredChores,
                familyMembers = familyMembers,
                onRoomSelect = viewModel::selectRoom,
                onAddRoom = { showCreateRoomSheet = true },
                onTogglePriority = viewModel::togglePriority,
                onToggleAssignee = viewModel::toggleAssignee,
                onToggleStatus = viewModel::toggleStatus,
                onResetFilters = viewModel::resetFilters,
                onChoreClick = { item -> onListClick(item.listId) },
                onSettingsClick = { onSpaceClick(familySpace!!.id, familySpace!!.title) },
                onListClick = onListClick,
                onSpaceClick = { onSpaceClick(familySpace!!.id, familySpace!!.title) },
                onGamificationClick = navigateToGamification,
                onShopClick = navigateToShop,
                onGoalsClick = navigateToGoals,
            )
        }
    }

    if (showCreateDialog) {
        CreateFamilySpaceDialog(
            isLoading = isCreating,
            onDismiss = { showCreateDialog = false },
            onConfirm = { name ->
                viewModel.createFamilySpace(name)
                showCreateDialog = false
            },
        )
    }

    if (showCreateRoomSheet) {
        CreateRoomSheet(
            isLoading = isCreatingRoom,
            onDismiss = { showCreateRoomSheet = false },
            onConfirm = { name ->
                viewModel.createRoom(name)
                showCreateRoomSheet = false
            },
        )
    }
}

// ─── Empty state ───

@Composable
private fun EmptyFamilyState(
    isCreating: Boolean,
    onCreateClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Top bar
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = SweetHomeSpacing.md, vertical = 18.dp),
            ) {
                Text(
                    text = "Мой дом",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        // Illustration — concentric circles with house emoji
        item {
            Spacer(Modifier.height(SweetHomeSpacing.xxl))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(220.dp),
            ) {
                // Outer circle
                Surface(
                    modifier = Modifier.size(220.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                ) {}
                // Middle circle
                Surface(
                    modifier = Modifier.size(160.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                ) {}
                // Center
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("🏠", fontSize = 36.sp)
                    }
                }
            }
        }

        // Title
        item {
            Spacer(Modifier.height(SweetHomeSpacing.xl))
            Text(
                text = "У вас ещё нет\nсемейного дома",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
        }

        // Subtitle
        item {
            Spacer(Modifier.height(SweetHomeSpacing.sm))
            Text(
                text = "Создайте пространство для близких —\nделитесь списками, следите за задачами\nи общайтесь вместе",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = SweetHomeSpacing.xl),
            )
        }

        // Feature cards row
        item {
            Spacer(Modifier.height(SweetHomeSpacing.lg))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SweetHomeSpacing.lg),
                horizontalArrangement = Arrangement.spacedBy(SweetHomeSpacing.xs),
            ) {
                FeatureCard("📋", "Общие списки", Modifier.weight(1f))
                FeatureCard("✅", "Задачи для всех", Modifier.weight(1f))
                FeatureCard("💬", "Семейный чат", Modifier.weight(1f))
            }
        }

        // Create button
        item {
            Spacer(Modifier.height(SweetHomeSpacing.lg))
            Surface(
                onClick = onCreateClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SweetHomeSpacing.md)
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 6.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isCreating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Text(
                            text = "Создать семейный дом",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }
            }
        }

        // Join by code
        item {
            Spacer(Modifier.height(SweetHomeSpacing.md))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SweetHomeSpacing.md),
                shape = SweetHomeShapes.Card,
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .padding(horizontal = SweetHomeSpacing.md),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(
                        text = "Вступить по коду приглашения",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Spacer(Modifier.height(SweetHomeSpacing.lg))
        }
    }
}

@Composable
private fun FeatureCard(
    emoji: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.height(72.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(emoji, fontSize = 22.sp)
            Spacer(Modifier.height(SweetHomeSpacing.xxs))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ─── Family Home (has data) ───

@Composable
private fun FamilyHomeContent(
    spaceName: String,
    memberCount: Int,
    lists: List<TodoList>,
    balance: Int,
    rooms: List<RoomUi>,
    selectedRoomId: String?,
    filters: RoomFilters,
    filteredChores: List<TodoItem>,
    familyMembers: List<GroupMember>,
    onRoomSelect: (String?) -> Unit,
    onAddRoom: () -> Unit,
    onTogglePriority: (String) -> Unit,
    onToggleAssignee: (String) -> Unit,
    onToggleStatus: (RoomStatusFilter) -> Unit,
    onResetFilters: () -> Unit,
    onChoreClick: (TodoItem) -> Unit,
    onSettingsClick: () -> Unit,
    onListClick: (String) -> Unit,
    onSpaceClick: () -> Unit,
    onGamificationClick: () -> Unit = {},
    onShopClick: () -> Unit = {},
    onGoalsClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        // Green header with mini stats inside
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary),
            ) {
                // Decorative circle
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .align(Alignment.TopEnd)
                        .background(Color.White.copy(alpha = 0.05f), CircleShape),
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = SweetHomeSpacing.lg)
                        .padding(top = 14.dp, bottom = 16.dp),
                ) {
                    // Top row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Семейное пространство",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.65f),
                            )
                            Text(
                                spaceName,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            com.jetbrains.kmpapp.ui.components.BalancePill(balance = balance)
                            HeaderIconButton("🎯", onClick = onGoalsClick)
                            HeaderIconButton("🏆", onClick = onGamificationClick)
                            HeaderIconButton("⚙️", onClick = onSettingsClick)
                        }
                    }
                    // Mini stats grid
                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        listOf(
                            "0" to "выполнено",
                            lists.size.toString() to "списка",
                            "0" to "задач сегодня",
                        ).forEach { (value, label) ->
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White.copy(alpha = 0.12f),
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                                    Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f), modifier = Modifier.padding(top = 1.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Nav cards
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SweetHomeSpacing.lg, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                listOf(
                    Triple("📋", "Списки",    Color(0xFFE8F3E8)),
                    Triple("💬", "Чат",       Color(0xFFFFF3E0)),
                    Triple("⚙️", "Настройки", Color(0xFFF3E5F5)),
                ).forEach { (emoji, label, bg) ->
                    NavCard(
                        emoji = emoji,
                        label = label,
                        bgColor = bg,
                        onClick = onSpaceClick,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        // Rooms section (G-05)
        item {
            Text(
                "Комнаты",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = SweetHomeSpacing.lg, vertical = SweetHomeSpacing.xs),
            )
        }
        item {
            RoomTabsRow(
                rooms = rooms,
                selectedId = selectedRoomId,
                onSelect = onRoomSelect,
                onAdd = onAddRoom,
            )
        }
        item {
            FilterChipsRow(
                filters = filters,
                members = familyMembers,
                onTogglePriority = onTogglePriority,
                onToggleAssignee = onToggleAssignee,
                onToggleStatus = onToggleStatus,
                onResetFilters = onResetFilters,
            )
        }
        if (filteredChores.isEmpty()) {
            item { EmptyRoomState() }
        } else {
            items(filteredChores, key = { it.id }) { item ->
                ChoreItemCard(
                    item = item,
                    list = lists.firstOrNull { it.id == item.listId },
                    memberNames = familyMembers.associate { it.userId to (it.displayName ?: it.userId.take(8)) },
                    onClick = { onChoreClick(item) },
                )
            }
        }

        // Gamification banner
        item {
            Surface(
                onClick = onGamificationClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SweetHomeSpacing.lg),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF3D5C3C),
                shadowElevation = 2.dp,
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(
                        "🏆",
                        fontSize = 60.sp,
                        modifier = Modifier.align(Alignment.CenterEnd),
                        color = Color.White.copy(alpha = 0.15f),
                    )
                    Column {
                        Text("Семейный рейтинг", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                        Text(
                            "Посмотреть результаты →",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
        }

        // Family shop banner
        item {
            Spacer(Modifier.height(10.dp))
            Surface(
                onClick = onShopClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SweetHomeSpacing.lg),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFD4956B),
                shadowElevation = 2.dp,
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(
                        "🛍",
                        fontSize = 60.sp,
                        modifier = Modifier.align(Alignment.CenterEnd),
                        color = Color.White.copy(alpha = 0.15f),
                    )
                    Column {
                        Text("Семейный магазин", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                        Text(
                            "Трать баллы на награды!",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
        }

        // Activity section
        item {
            Spacer(Modifier.height(SweetHomeSpacing.lg))
            Text(
                "Активность сегодня",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = SweetHomeSpacing.lg),
            )
            Spacer(Modifier.height(SweetHomeSpacing.xs))
        }

        item {
            ActivityCard(
                emoji = "✅",
                text = "Нет недавней активности",
                time = "",
            )
        }

        // Lists section
        if (lists.isNotEmpty()) {
            item {
                Spacer(Modifier.height(SweetHomeSpacing.lg))
                Text(
                    "Списки семьи",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = SweetHomeSpacing.lg),
                )
                Spacer(Modifier.height(SweetHomeSpacing.xs))
            }
            items(lists, key = { it.id }) { list ->
                FamilyListCard(list = list, onClick = { onListClick(list.id) })
            }
        }

        // Chat button
        item {
            Spacer(Modifier.height(SweetHomeSpacing.lg))
            Surface(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SweetHomeSpacing.lg)
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 4.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "💬  Открыть чат семьи",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
            Spacer(Modifier.height(SweetHomeSpacing.lg))
        }
    }
}

@Composable
private fun HeaderIconButton(emoji: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(36.dp),
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.15f),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(emoji, fontSize = 18.sp)
        }
    }
}

@Composable
private fun StatsBar(
    completedCount: Int,
    activeListsCount: Int,
    todayTasksCount: Int,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = SweetHomeSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatColumn(
                value = completedCount.toString(),
                line1 = "Выполнено",
                line2 = "за неделю",
                modifier = Modifier.weight(1f),
            )
            VerticalDivider()
            StatColumn(
                value = activeListsCount.toString(),
                line1 = "Активных",
                line2 = "списка",
                modifier = Modifier.weight(1f),
            )
            VerticalDivider()
            StatColumn(
                value = todayTasksCount.toString(),
                line1 = "Задач",
                line2 = "на сегодня",
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun StatColumn(
    value: String,
    line1: String,
    line2: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = line1,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Text(
            text = line2,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun VerticalDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(1.dp)
            .height(40.dp)
            .background(MaterialTheme.colorScheme.outline),
    )
}

@Composable
private fun ActivityCard(
    emoji: String,
    text: String,
    time: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = SweetHomeSpacing.lg, vertical = SweetHomeSpacing.xxs),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = SweetHomeSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(emoji, fontSize = 18.sp)
                }
            }
            Spacer(Modifier.width(SweetHomeSpacing.sm))
            Text(
                text = text,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (time.isNotBlank()) {
                Text(
                    text = time,
                    fontSize = 11.sp,
                    color = Color(0xFFAAAAA0),
                )
            }
        }
    }
}

@Composable
private fun NavCard(
    emoji: String,
    label: String,
    bgColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(84.dp),
        shape = SweetHomeShapes.Card,
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(emoji, fontSize = 28.sp)
            Spacer(Modifier.height(SweetHomeSpacing.xs))
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

// ─── Family list card ───

private fun listTypeToEmoji(type: String) = when (type) {
    "shopping" -> "🛒"
    "home_chores" -> "🏠"
    else -> "📋"
}

@Composable
private fun FamilyListCard(
    list: TodoList,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = SweetHomeSpacing.lg, vertical = SweetHomeSpacing.xxs)
            .clickable(onClick = onClick),
        shape = SweetHomeShapes.Card,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(SweetHomeSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = list.icon?.takeIf { it.isNotBlank() } ?: listTypeToEmoji(list.type),
                        fontSize = 18.sp,
                    )
                }
            }
            Spacer(Modifier.width(SweetHomeSpacing.sm))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = list.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = list.type.let {
                        when (it) {
                            "shopping" -> "Покупки"
                            "home_chores" -> "Дела по дому"
                            else -> "Общий"
                        }
                    },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = "›",
                fontSize = 20.sp,
                color = Color(0xFFBDBDBD),
            )
        }
    }
}

// ─── Create dialog ───

@Composable
private fun CreateFamilySpaceDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by remember { mutableStateOf("Наш дом") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Семейное пространство") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Название") },
                singleLine = true,
                enabled = !isLoading,
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name.ifBlank { "Наш дом" }) },
                enabled = !isLoading,
            ) { Text("Создать") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        },
    )
}

// ─── Rooms section (G-05) ───

@Composable
private fun RoomTabsRow(
    rooms: List<RoomUi>,
    selectedId: String?,
    onSelect: (String?) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = SweetHomeSpacing.lg, vertical = SweetHomeSpacing.xxs),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "__all__") {
            RoomTab(
                emoji = "🏠",
                label = "Все",
                selected = selectedId == null,
                onClick = { onSelect(null) },
            )
        }
        items(rooms, key = { "room_${it.id}" }) { room ->
            RoomTab(
                emoji = room.emoji,
                label = room.name,
                selected = selectedId == room.id,
                onClick = { onSelect(room.id) },
            )
        }
        item(key = "__add__") {
            RoomTab(
                emoji = "＋",
                label = "Добавить",
                selected = false,
                onClick = onAdd,
                isAddAction = true,
            )
        }
    }
}

@Composable
private fun RoomTab(
    emoji: String,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    isAddAction: Boolean = false,
) {
    val bg = when {
        selected -> MaterialTheme.colorScheme.primary
        isAddAction -> Color.Transparent
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val fg = when {
        selected -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        onClick = onClick,
        shape = SweetHomeShapes.Chip,
        color = bg,
        border = if (isAddAction && !selected) {
            androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        } else null,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = SweetHomeSpacing.md, vertical = SweetHomeSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = emoji, fontSize = 14.sp)
            Spacer(Modifier.width(SweetHomeSpacing.xxs))
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = fg,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterChipsRow(
    filters: RoomFilters,
    members: List<GroupMember>,
    onTogglePriority: (String) -> Unit,
    onToggleAssignee: (String) -> Unit,
    onToggleStatus: (RoomStatusFilter) -> Unit,
    onResetFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var priorityExpanded by remember { mutableStateOf(false) }
    var assigneeExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = SweetHomeSpacing.lg, vertical = SweetHomeSpacing.xxs),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "f_priority") {
            Box {
                FilterPillButton(
                    label = "Приоритет",
                    activeCount = filters.priorities.size,
                    expanded = priorityExpanded,
                    onClick = { priorityExpanded = !priorityExpanded },
                )
                DropdownMenu(
                    expanded = priorityExpanded,
                    onDismissRequest = { priorityExpanded = false },
                ) {
                    FilterDropdownItem("⬆️ Высокий", "high" in filters.priorities) { onTogglePriority("high") }
                    FilterDropdownItem("➡️ Средний", "medium" in filters.priorities) { onTogglePriority("medium") }
                    FilterDropdownItem("⬇️ Низкий", "low" in filters.priorities) { onTogglePriority("low") }
                    FilterDropdownItem("— Без приоритета", PRIORITY_NONE_KEY in filters.priorities) {
                        onTogglePriority(PRIORITY_NONE_KEY)
                    }
                }
            }
        }
        item(key = "f_assignee") {
            Box {
                FilterPillButton(
                    label = "Исполнитель",
                    activeCount = filters.assignees.size,
                    expanded = assigneeExpanded,
                    onClick = { assigneeExpanded = !assigneeExpanded },
                )
                DropdownMenu(
                    expanded = assigneeExpanded,
                    onDismissRequest = { assigneeExpanded = false },
                ) {
                    FilterDropdownItem("Не назначен", ASSIGNEE_UNASSIGNED_KEY in filters.assignees) {
                        onToggleAssignee(ASSIGNEE_UNASSIGNED_KEY)
                    }
                    if (members.isNotEmpty()) HorizontalDivider()
                    members.forEach { m ->
                        val name = m.displayName?.takeIf { it.isNotBlank() } ?: m.userId.take(8)
                        FilterDropdownItem(name, m.userId in filters.assignees) {
                            onToggleAssignee(m.userId)
                        }
                    }
                }
            }
        }
        item(key = "f_status") {
            Box {
                val statusActive = filters.statuses != setOf(RoomStatusFilter.ACTIVE)
                FilterPillButton(
                    label = "Статус",
                    activeCount = if (statusActive) filters.statuses.size else 0,
                    expanded = statusExpanded,
                    onClick = { statusExpanded = !statusExpanded },
                )
                DropdownMenu(
                    expanded = statusExpanded,
                    onDismissRequest = { statusExpanded = false },
                ) {
                    FilterDropdownItem("Активные", RoomStatusFilter.ACTIVE in filters.statuses) {
                        onToggleStatus(RoomStatusFilter.ACTIVE)
                    }
                    FilterDropdownItem("Просроченные", RoomStatusFilter.OVERDUE in filters.statuses) {
                        onToggleStatus(RoomStatusFilter.OVERDUE)
                    }
                    FilterDropdownItem("Выполненные", RoomStatusFilter.DONE in filters.statuses) {
                        onToggleStatus(RoomStatusFilter.DONE)
                    }
                }
            }
        }
        if (!filters.isDefault()) {
            item(key = "f_reset") {
                FilterChip(
                    selected = false,
                    onClick = onResetFilters,
                    label = { Text("✕ Сбросить", fontSize = 13.sp) },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterPillButton(
    label: String,
    activeCount: Int,
    expanded: Boolean,
    onClick: () -> Unit,
) {
    val isActive = activeCount > 0
    FilterChip(
        selected = isActive,
        onClick = onClick,
        label = {
            Text(
                text = if (isActive) "$label · $activeCount" else label,
                fontSize = 13.sp,
            )
        },
        trailingIcon = {
            Text(
                text = if (expanded) "▴" else "▾",
                fontSize = 12.sp,
                modifier = Modifier.padding(end = 4.dp),
            )
        },
        colors = FilterChipDefaults.filterChipColors(),
    )
}

@Composable
private fun FilterDropdownItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(
                            if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            RoundedCornerShape(4.dp),
                        )
                        .border(
                            1.dp,
                            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(4.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (selected) {
                        Text("✓", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.width(SweetHomeSpacing.sm))
                Text(text, fontSize = 14.sp)
            }
        },
        onClick = onClick,
    )
}

@Composable
private fun EmptyRoomState(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = SweetHomeSpacing.lg, vertical = SweetHomeSpacing.xs),
        shape = SweetHomeShapes.Card,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SweetHomeSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = "🧹", fontSize = 28.sp)
            Spacer(Modifier.height(SweetHomeSpacing.xxs))
            Text(
                "Здесь пусто",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                "Нет дел, попадающих под фильтры",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ChoreItemCard(
    item: TodoItem,
    list: TodoList?,
    memberNames: Map<String, String>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = SweetHomeSpacing.lg, vertical = SweetHomeSpacing.xxs)
            .clickable(onClick = onClick),
        shape = SweetHomeShapes.Card,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
    ) {
        Row(
            modifier = Modifier.padding(SweetHomeSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // priority dot
            choreItemAccent(item.priority)?.let { color ->
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(color, CircleShape),
                )
                Spacer(Modifier.width(SweetHomeSpacing.xs))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val parts = buildList {
                    list?.let { add(it.title) }
                    item.assignedTo?.takeIf { it.isNotBlank() }?.let { id ->
                        add("→ ${memberNames[id] ?: id.take(8)}")
                    }
                    item.dueAt?.takeIf { it.isNotBlank() }?.let { add("⏱ ${it.take(10)}") }
                }
                if (parts.isNotEmpty()) {
                    Text(
                        text = parts.joinToString(" · "),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (item.isDone) {
                Text("✓", fontSize = 16.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun choreItemAccent(priority: String?): Color? = when (priority) {
    "high" -> PriorityHigh
    "medium" -> PriorityMedium
    "low" -> PriorityLow
    else -> null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateRoomSheet(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("📁") }

    val close: () -> Unit = {
        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SweetHomeSpacing.lg, vertical = SweetHomeSpacing.sm),
        ) {
            Text(
                "Новая комната",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = SweetHomeSpacing.xs),
            )
            Text(
                "Иконка определяется по названию автоматически. Можно выбрать вручную (отображается только в этом устройстве).",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(SweetHomeSpacing.sm))
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    selectedEmoji = roomEmojiFor(it)
                },
                label = { Text("Название") },
                placeholder = { Text("Кухня, Спальня, …") },
                singleLine = true,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(SweetHomeSpacing.sm))
            Text("Иконка", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(SweetHomeSpacing.xs))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(RoomEmojiPresets) { emoji ->
                    Surface(
                        onClick = { selectedEmoji = emoji },
                        shape = CircleShape,
                        color = if (selectedEmoji == emoji) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(40.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(emoji, fontSize = 18.sp)
                        }
                    }
                }
            }
            Spacer(Modifier.height(SweetHomeSpacing.lg))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SweetHomeSpacing.sm),
            ) {
                TextButton(
                    onClick = close,
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading,
                ) { Text("Отмена") }
                Surface(
                    onClick = {
                        if (name.isNotBlank()) {
                            onConfirm(name.trim())
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = SweetHomeShapes.Chip,
                    color = if (name.isNotBlank()) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        } else {
                            Text(
                                "Создать",
                                fontWeight = FontWeight.SemiBold,
                                color = if (name.isNotBlank()) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(SweetHomeSpacing.md))
        }
    }
}
