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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.draw.clip
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
import com.jetbrains.kmpapp.ui.LocalCozyExtraColors
import com.jetbrains.kmpapp.ui.LocalCozyShapes
import com.jetbrains.kmpapp.ui.LocalCozySpacing
import com.jetbrains.kmpapp.ui.components.CozyAvatar
import com.jetbrains.kmpapp.ui.components.CozyCard
import com.jetbrains.kmpapp.ui.components.CozyChip
import com.jetbrains.kmpapp.ui.components.CozyTopBar
import com.jetbrains.kmpapp.ui.components.EmptyHero
import com.jetbrains.kmpapp.ui.components.pet.PetAvatar
import com.jetbrains.kmpapp.ui.components.pet.PetSceneTile
import com.jetbrains.kmpapp.ui.models.Mood
import com.jetbrains.kmpapp.ui.models.Palette
import com.jetbrains.kmpapp.ui.models.Pet
import com.jetbrains.kmpapp.ui.models.Species
import com.jetbrains.kmpapp.ui.models.Stage
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

// ─── Entry point ───

@Composable
internal fun FamilyContent(
    contentPadding: PaddingValues,
    onSpaceClick: (groupId: String, groupName: String) -> Unit,
    onListClick: (String) -> Unit,
    navigateToGamification: () -> Unit = {},
    navigateToShop: () -> Unit = {},
    navigateToGoals: () -> Unit = {},
    navigateToJoinByCode: () -> Unit = {},
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
            .background(MaterialTheme.colorScheme.background)
            .padding(contentPadding),
    ) {
        SnackbarHost(snackbarHostState)

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            familySpace == null -> EmptyFamilyState(
                isCreating = isCreating,
                onCreateClick = { showCreateDialog = true },
                onJoinByCodeClick = navigateToJoinByCode,
            )
            else -> FamilyHomeContent(
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
    onJoinByCodeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalCozySpacing.current
    val shapes = LocalCozyShapes.current
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(spacing.huge))
        EmptyHero(emoji = "🏠", size = 140.dp)
        Spacer(Modifier.height(spacing.xxxl))
        Text(
            "У вас ещё нет\nсемейного дома",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(spacing.sm))
        Text(
            "Создайте пространство для близких —\nделитесь списками, следите за задачами\nи общайтесь вместе",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(spacing.xl))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.xs),
        ) {
            FeatureCard("📋", "Общие\nсписки", Modifier.weight(1f))
            FeatureCard("✅", "Задачи\nдля всех", Modifier.weight(1f))
            FeatureCard("💬", "Семейный\nчат", Modifier.weight(1f))
        }
        Spacer(Modifier.height(spacing.xl))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(shapes.button)
                .background(MaterialTheme.colorScheme.primary)
                .clickable(enabled = !isCreating, onClick = onCreateClick),
            contentAlignment = Alignment.Center,
        ) {
            if (isCreating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Text(
                    "Создать семейный дом",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
        Spacer(Modifier.height(spacing.sm))
        CozyCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = onJoinByCodeClick,
            bordered = true,
            contentPadding = spacing.md,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🔗", fontSize = 16.sp)
                Spacer(Modifier.width(spacing.xs))
                Text(
                    "Вступить по коду приглашения",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )
                Text("→", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun FeatureCard(emoji: String, label: String, modifier: Modifier = Modifier) {
    val spacing = LocalCozySpacing.current
    CozyCard(
        modifier = modifier.height(80.dp),
        bordered = true,
        contentPadding = spacing.xs,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(emoji, fontSize = 22.sp)
            Spacer(Modifier.height(spacing.xxs))
            Text(
                label,
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
    onGamificationClick: () -> Unit,
    onShopClick: () -> Unit,
    onGoalsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalCozySpacing.current
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = spacing.huge),
    ) {
        item {
            FamilyHeader(
                spaceName = spaceName,
                memberCount = memberCount,
                members = familyMembers,
                onSettingsClick = onSettingsClick,
            )
        }
        item { Spacer(Modifier.height(spacing.lg)) }

        item {
            PetGuardianScene(
                modifier = Modifier.padding(horizontal = spacing.lg),
                balance = balance,
            )
        }
        item { Spacer(Modifier.height(spacing.lg)) }

        item {
            FamilyListsCarousel(
                lists = lists,
                onListClick = onListClick,
            )
        }
        item { Spacer(Modifier.height(spacing.lg)) }

        item {
            FeatureGrid(
                memberCount = memberCount,
                balance = balance,
                onGamificationClick = onGamificationClick,
                onShopClick = onShopClick,
                onGoalsClick = onGoalsClick,
                onSettingsClick = onSettingsClick,
            )
        }
        item { Spacer(Modifier.height(spacing.xl)) }

        item {
            SectionLabel("КОМНАТЫ")
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
    }
}

// ─── Header ───

@Composable
private fun FamilyHeader(
    spaceName: String,
    memberCount: Int,
    members: List<GroupMember>,
    onSettingsClick: () -> Unit,
) {
    val spacing = LocalCozySpacing.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.xxl, vertical = spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Семья 🏡",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(spacing.xxs))
            Text(
                "$spaceName · $memberCount ${memberCountWord(memberCount)}",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        OverlapAvatars(members = members)
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(onClick = onSettingsClick),
            contentAlignment = Alignment.Center,
        ) {
            Text("⚙", fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
        }
    }
}

private fun memberCountWord(n: Int): String = when (n % 10) {
    1 -> if (n % 100 == 11) "человек" else "человек"
    2, 3, 4 -> if (n % 100 in 12..14) "человек" else "человека"
    else -> "человек"
}

@Composable
private fun OverlapAvatars(members: List<GroupMember>) {
    val palettes = listOf(Palette.CORAL, Palette.LAVENDER, Palette.OCHRE, Palette.PRIMARY)
    val visible = members.take(3)
    Row {
        visible.forEachIndexed { i, m ->
            val letter = (m.displayName ?: m.userId).firstOrNull()?.toString()?.uppercase() ?: "?"
            Box(
                modifier = Modifier
                    .offset(x = (-8 * i).dp)
                    .border(2.dp, MaterialTheme.colorScheme.background, CircleShape),
            ) {
                CozyAvatar(
                    letter = letter,
                    palette = palettes[i % palettes.size],
                    size = 32.dp,
                )
            }
        }
    }
}

// ─── Pet guardian scene ───

@Composable
private fun PetGuardianScene(
    modifier: Modifier = Modifier,
    balance: Int,
) {
    val spacing = LocalCozySpacing.current
    val extras = LocalCozyExtraColors.current
    val guardian = remember {
        Pet(
            id = "guardian",
            name = "Большой Енот",
            species = Species.RACCOON,
            stage = Stage.ADULT,
            level = 14,
            mood = Mood.HAPPY,
        )
    }
    val familyPets = listOf(Species.RACCOON, Species.FOX, Species.CAT, Species.HEDGIE)

    CozyCard(
        modifier = modifier.fillMaxWidth(),
        radius = 22.dp,
        contentPadding = spacing.lg,
        background = extras.ochreSoft,
    ) {
        Column {
            Text(
                "НАША БЕРЛОГА",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp,
            )
            Spacer(Modifier.height(spacing.xs))
            Text(
                "${guardian.name} · ур. ${guardian.level}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(spacing.md))

            // Scene with the guardian and 4 personal pets in corners
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                contentAlignment = Alignment.Center,
            ) {
                PetSceneTile(pet = guardian, size = 200.dp)

                // Four mini pet avatars in corners
                Box(
                    modifier = Modifier.size(240.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        PetAvatar(
                            species = familyPets[0],
                            size = 44.dp,
                            accent = true,
                            modifier = Modifier.align(Alignment.TopStart),
                        )
                        PetAvatar(
                            species = familyPets[1],
                            size = 44.dp,
                            accent = true,
                            modifier = Modifier.align(Alignment.TopEnd),
                        )
                        PetAvatar(
                            species = familyPets[2],
                            size = 44.dp,
                            accent = true,
                            modifier = Modifier.align(Alignment.BottomStart),
                        )
                        PetAvatar(
                            species = familyPets[3],
                            size = 44.dp,
                            accent = true,
                            modifier = Modifier.align(Alignment.BottomEnd),
                        )
                    }
                }
            }

            Spacer(Modifier.height(spacing.md))
            // Level / streak bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.xs),
            ) {
                Text("🌿", fontSize = 14.sp)
                Text(
                    "Дружные",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.outline),
                )
                Text(
                    "18 / 22 дел недели",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(spacing.xs))
            ProgressBar(progress = 0.82f, valueLabel = "$balance⭐")
        }
    }
}

@Composable
private fun ProgressBar(progress: Float, valueLabel: String) {
    val spacing = LocalCozySpacing.current
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surface),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.primary),
            )
        }
        Text(
            valueLabel,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

// ─── Lists carousel ───

@Composable
private fun FamilyListsCarousel(
    lists: List<TodoList>,
    onListClick: (String) -> Unit,
) {
    val spacing = LocalCozySpacing.current
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.xxl),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "СПИСКИ СЕМЬИ",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(spacing.sm))
        if (lists.isEmpty()) {
            Text(
                "Пока списков нет",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = spacing.xxl),
            )
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = spacing.lg),
                horizontalArrangement = Arrangement.spacedBy(spacing.xs),
            ) {
                items(lists, key = { it.id }) { list ->
                    ListChipCard(list = list, onClick = { onListClick(list.id) })
                }
            }
        }
    }
}

@Composable
private fun ListChipCard(list: TodoList, onClick: () -> Unit) {
    val spacing = LocalCozySpacing.current
    val extras = LocalCozyExtraColors.current
    val tileBg = when (list.type) {
        "shopping" -> extras.coralSoft
        "home_chores" -> MaterialTheme.colorScheme.primaryContainer
        "wishlist" -> extras.lavenderSoft
        "study" -> extras.ochreSoft
        else -> extras.ochreSoft
    }
    CozyCard(
        modifier = Modifier.width(140.dp),
        onClick = onClick,
        bordered = true,
        contentPadding = spacing.sm,
    ) {
        Column {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(tileBg),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    list.icon?.takeIf { it.isNotBlank() } ?: listTypeToEmoji(list.type),
                    fontSize = 18.sp,
                )
            }
            Spacer(Modifier.height(spacing.xs))
            Text(
                list.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                listTypeLabel(list.type),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun listTypeToEmoji(type: String) = when (type) {
    "shopping" -> "🛒"
    "home_chores" -> "🏡"
    "wishlist" -> "🎁"
    "study" -> "📚"
    else -> "📋"
}

private fun listTypeLabel(type: String) = when (type) {
    "shopping" -> "Покупки"
    "home_chores" -> "Дом"
    "wishlist" -> "Вишлист"
    "study" -> "Учёба"
    else -> "Общий"
}

// ─── Feature grid ───

@Composable
private fun FeatureGrid(
    memberCount: Int,
    balance: Int,
    onGamificationClick: () -> Unit,
    onShopClick: () -> Unit,
    onGoalsClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    val spacing = LocalCozySpacing.current
    val extras = LocalCozyExtraColors.current

    data class Feature(
        val emoji: String,
        val title: String,
        val subtitle: String,
        val bgColor: Color,
        val accent: Color,
        val onClick: () -> Unit,
    )

    val features = listOf(
        Feature("🏆", "Рейтинг", "Открыть таблицу", extras.ochreSoft, extras.ochre, onGamificationClick),
        Feature("⭐", "Магазин наград", "$balance баллов", extras.coralSoft, extras.coral, onShopClick),
        Feature("🎯", "Цели семьи", "Что важно", extras.lavenderSoft, extras.lavender, onGoalsClick),
        Feature("⚙️", "Настройки", "$memberCount уч.", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.primary, onSettingsClick),
    )

    Column(modifier = Modifier.padding(horizontal = spacing.lg)) {
        features.chunked(2).forEach { pair ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = spacing.xxs),
                horizontalArrangement = Arrangement.spacedBy(spacing.xs),
            ) {
                pair.forEach { f ->
                    FeatureTile(
                        emoji = f.emoji,
                        title = f.title,
                        subtitle = f.subtitle,
                        bgColor = f.bgColor,
                        accent = f.accent,
                        onClick = f.onClick,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun FeatureTile(
    emoji: String,
    title: String,
    subtitle: String,
    bgColor: Color,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalCozySpacing.current
    CozyCard(
        modifier = modifier,
        onClick = onClick,
        bordered = true,
        contentPadding = spacing.md,
    ) {
        Column {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center,
            ) {
                Text(emoji, fontSize = 20.sp)
            }
            Spacer(Modifier.height(spacing.xs))
            Text(
                title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                subtitle,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = accent,
            )
        }
    }
}

// ─── Section label helper ───

@Composable
private fun SectionLabel(text: String) {
    val spacing = LocalCozySpacing.current
    Text(
        text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(horizontal = spacing.xxl, vertical = spacing.xs),
    )
}

// ─── Create family space dialog ───

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
    val spacing = LocalCozySpacing.current
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = spacing.lg, vertical = spacing.xxs),
        horizontalArrangement = Arrangement.spacedBy(spacing.xs),
    ) {
        item(key = "__all__") {
            CozyChip(
                label = "🏠 Все",
                selected = selectedId == null,
                onClick = { onSelect(null) },
            )
        }
        items(rooms, key = { "room_${it.id}" }) { room ->
            CozyChip(
                label = "${room.emoji} ${room.name}",
                selected = selectedId == room.id,
                onClick = { onSelect(room.id) },
            )
        }
        item(key = "__add__") {
            CozyChip(
                label = "＋ Добавить",
                selected = false,
                onClick = onAdd,
            )
        }
    }
}

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
    val spacing = LocalCozySpacing.current
    var priorityExpanded by remember { mutableStateOf(false) }
    var assigneeExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = spacing.lg, vertical = spacing.xxs),
        horizontalArrangement = Arrangement.spacedBy(spacing.xs),
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
                CozyChip(
                    label = "✕ Сбросить",
                    onClick = onResetFilters,
                )
            }
        }
    }
}

@Composable
private fun FilterPillButton(
    label: String,
    activeCount: Int,
    expanded: Boolean,
    onClick: () -> Unit,
) {
    val isActive = activeCount > 0
    val arrow = if (expanded) "▴" else "▾"
    val text = if (isActive) "$label · $activeCount $arrow" else "$label $arrow"
    CozyChip(label = text, selected = isActive, onClick = onClick)
}

@Composable
private fun FilterDropdownItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val spacing = LocalCozySpacing.current
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
                            if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(4.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (selected) {
                        Text(
                            "✓",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                Spacer(Modifier.width(spacing.sm))
                Text(text, fontSize = 14.sp)
            }
        },
        onClick = onClick,
    )
}

@Composable
private fun EmptyRoomState(modifier: Modifier = Modifier) {
    val spacing = LocalCozySpacing.current
    CozyCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.lg, vertical = spacing.xs),
        background = MaterialTheme.colorScheme.surfaceVariant,
        contentPadding = spacing.xxl,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("🧹", fontSize = 28.sp)
            Spacer(Modifier.height(spacing.xxs))
            Text(
                "Здесь пусто",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
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
    val spacing = LocalCozySpacing.current
    CozyCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.lg, vertical = spacing.xxs),
        onClick = onClick,
        bordered = true,
        contentPadding = spacing.sm,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            choreItemAccent(item.priority)?.let { color ->
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(color),
                )
                Spacer(Modifier.width(spacing.xs))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
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
                        parts.joinToString(" · "),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (item.isDone) {
                Text(
                    "✓",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun choreItemAccent(priority: String?): Color? = when (priority) {
    "high" -> MaterialTheme.colorScheme.error
    "medium" -> LocalCozyExtraColors.current.ochre
    "low" -> LocalCozyExtraColors.current.success
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
    val spacing = LocalCozySpacing.current
    val shapes = LocalCozyShapes.current

    val close: () -> Unit = {
        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = shapes.sheet,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.xxl, vertical = spacing.sm),
        ) {
            Text(
                "Новая комната",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(vertical = spacing.xs),
            )
            Text(
                "Иконка определяется по названию автоматически. Можно выбрать вручную (отображается только в этом устройстве).",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(spacing.sm))
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
            Spacer(Modifier.height(spacing.sm))
            Text("Иконка", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(spacing.xs))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                items(RoomEmojiPresets) { emoji ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (selectedEmoji == emoji) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant,
                            )
                            .clickable { selectedEmoji = emoji },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(emoji, fontSize = 18.sp)
                    }
                }
            }
            Spacer(Modifier.height(spacing.xl))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                TextButton(
                    onClick = close,
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading,
                ) { Text("Отмена") }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(shapes.button)
                        .background(
                            if (name.isNotBlank()) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant,
                        )
                        .clickable(enabled = name.isNotBlank()) {
                            if (name.isNotBlank()) onConfirm(name.trim())
                        },
                    contentAlignment = Alignment.Center,
                ) {
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
            Spacer(Modifier.height(spacing.lg))
        }
    }
}
