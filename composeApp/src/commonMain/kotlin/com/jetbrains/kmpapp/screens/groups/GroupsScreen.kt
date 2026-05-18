package com.jetbrains.kmpapp.screens.groups

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
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
import com.jetbrains.kmpapp.ui.LocalCozyExtraColors
import com.jetbrains.kmpapp.ui.LocalCozyShapes
import com.jetbrains.kmpapp.ui.components.CozyCard
import com.jetbrains.kmpapp.ui.components.EmptyHero
import org.koin.compose.viewmodel.koinViewModel

private data class GroupVisuals(
    val emoji: String,
    val typeLabel: String,
    val tileBg: Color,
)

@Composable
private fun groupVisuals(group: Group): GroupVisuals {
    val extras = LocalCozyExtraColors.current
    return when (group.type) {
        WorkspaceType.FAMILY -> GroupVisuals("🏡", "Семья", MaterialTheme.colorScheme.primaryContainer)
        WorkspaceType.GROUP  -> GroupVisuals("👥", "Группа", MaterialTheme.colorScheme.primaryContainer)
        WorkspaceType.WORK   -> GroupVisuals("💼", "Работа", extras.lavenderSoft)
        WorkspaceType.MENTORING -> GroupVisuals("🎓", "Наставничество", extras.lavenderSoft)
        "hobby" -> GroupVisuals("🎯", "Хобби", extras.coralSoft)
        "study" -> GroupVisuals("📚", "Учёба", extras.ochreSoft)
        WorkspaceType.PERSONAL -> GroupVisuals("🌿", "Личное", extras.surfaceSoft)
        else -> GroupVisuals(group.icon ?: "👤", "Группа", MaterialTheme.colorScheme.primaryContainer)
    }
}

private data class TypeChip(val key: String?, val emoji: String, val label: String)

private val TYPE_CHIPS = listOf(
    TypeChip(null, "✦", "Все"),
    TypeChip(WorkspaceType.FAMILY, "🏡", "Семья"),
    TypeChip(WorkspaceType.WORK, "💼", "Работа"),
    TypeChip("hobby", "🎯", "Хобби"),
    TypeChip(WorkspaceType.GROUP, "👥", "Группа"),
    TypeChip(WorkspaceType.MENTORING, "🎓", "Менторинг"),
    TypeChip("study", "📚", "Учёба"),
)

@Composable
internal fun GroupsContent(
    groups: List<Group>,                       // kept for backwards-compat with MainScreen wiring
    isGuest: Boolean,
    contentPadding: PaddingValues,
    onGroupClick: (Group) -> Unit,
    navigateToLinkEmail: () -> Unit,
    navigateToJoinByCode: () -> Unit,
    onCreateGroup: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val viewModel = koinViewModel<GroupsViewModel>()
    val sections by viewModel.sections.collectAsStateWithLifecycle()
    val query by viewModel.searchQuery.collectAsStateWithLifecycle()
    val typeFilter by viewModel.typeFilter.collectAsStateWithLifecycle()
    val archiveExpanded by viewModel.archiveExpanded.collectAsStateWithLifecycle()

    var actionsForGroup by remember { mutableStateOf<Group?>(null) }

    val totalVisible = sections.pinned.size + sections.personal.size +
        sections.work.size + sections.family.size + sections.archived.size

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .background(MaterialTheme.colorScheme.background),
    ) {
        // Header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Группы",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = groupsSubtitle(sections),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconCircle(
                    text = "⌗",
                    bg = MaterialTheme.colorScheme.surface,
                    fg = MaterialTheme.colorScheme.onBackground,
                    bordered = true,
                    onClick = if (isGuest) navigateToLinkEmail else navigateToJoinByCode,
                )
                if (!isGuest && onCreateGroup != null) {
                    IconCircle(
                        text = "+",
                        bg = MaterialTheme.colorScheme.primary,
                        fg = MaterialTheme.colorScheme.onPrimary,
                        onClick = onCreateGroup,
                    )
                }
            }
        }

        // Search bar
        SearchBar(
            value = query,
            onValueChange = { viewModel.setSearchQuery(it) },
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp),
        )

        // Filter chips
        TypeChipsRow(
            selected = typeFilter,
            onSelect = { viewModel.setTypeFilter(it) },
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp),
        )

        if (totalVisible == 0) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                EmptyHero(emoji = "👨‍👩‍👧", decor = listOf("💬", "🏠", "❤️", "🌟"))
                Spacer(Modifier.height(16.dp))
                Text(
                    when {
                        groups.isEmpty() -> "Нет групп"
                        else -> "Ничего не нашлось"
                    },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    when {
                        isGuest -> "Привяжите email чтобы создавать и вступать в группы"
                        groups.isEmpty() -> "Нажмите + чтобы создать первую группу"
                        else -> "Попробуйте сбросить поиск или фильтр"
                    },
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (sections.pinned.isNotEmpty()) {
                    item("h_pin") { SectionHeader(label = "📌 ЗАКРЕПЛЕНО", count = sections.pinned.size) }
                    items(sections.pinned, key = { "pin_${it.id}" }) { group ->
                        PinCard(
                            group = group,
                            onClick = { onGroupClick(group) },
                            onLongPress = { actionsForGroup = group },
                        )
                    }
                }
                renderSection(sections.family, "🏡 СЕМЬЯ", "fam", onGroupClick) { actionsForGroup = it }
                renderSection(sections.personal, "🌿 ЛИЧНОЕ", "per", onGroupClick) { actionsForGroup = it }
                renderSection(sections.work, "💼 РАБОТА", "wrk", onGroupClick) { actionsForGroup = it }

                if (sections.archived.isNotEmpty()) {
                    item("h_arch") {
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.toggleArchiveExpanded() }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "📦 АРХИВ · ${sections.archived.size}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 1.2.sp,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                text = if (archiveExpanded) "˄" else "˅",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    if (archiveExpanded) {
                        items(sections.archived, key = { "arch_${it.id}" }) { group ->
                            ArchivedRow(
                                group = group,
                                onClick = { onGroupClick(group) },
                                onUnarchive = { viewModel.unarchive(group.id) },
                            )
                        }
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    actionsForGroup?.let { group ->
        GroupActionsSheet(
            group = group,
            onDismiss = { actionsForGroup = null },
            onTogglePin = { viewModel.togglePinned(group.id); actionsForGroup = null },
            onToggleMute = { viewModel.toggleMuted(group.id); actionsForGroup = null },
            onArchive = { viewModel.archive(group.id); actionsForGroup = null },
        )
    }
}

private fun groupsSubtitle(sections: GroupsListState): String {
    val active = sections.pinned.size + sections.personal.size + sections.work.size + sections.family.size
    return when {
        active == 0 && sections.archived.isEmpty() -> "Нет групп"
        active == 0 -> "${sections.archived.size} в архиве"
        sections.archived.isEmpty() -> "$active активных"
        else -> "$active активных · ${sections.archived.size} в архиве"
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.renderSection(
    groups: List<Group>,
    header: String,
    keyPrefix: String,
    onClick: (Group) -> Unit,
    onLongPress: (Group) -> Unit,
) {
    if (groups.isEmpty()) return
    item("h_$keyPrefix") {
        Spacer(Modifier.height(8.dp))
        SectionHeader(label = header, count = groups.size)
    }
    items(groups, key = { "${keyPrefix}_${it.id}" }) { group ->
        GroupRow(
            group = group,
            onClick = { onClick(group) },
            onLongPress = { onLongPress(group) },
        )
    }
}

@Composable
private fun IconCircle(
    text: String,
    bg: Color,
    fg: Color,
    onClick: () -> Unit,
    bordered: Boolean = false,
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(bg)
            .then(
                if (bordered) Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                else Modifier
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, color = fg, fontSize = 18.sp, fontWeight = FontWeight.Light)
    }
}

@Composable
private fun SearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
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
                        Text(
                            "Поиск группы…",
                            fontSize = 13.sp,
                            color = extras.textTer,
                        )
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
private fun TypeChipsRow(
    selected: String?,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shapes = LocalCozyShapes.current
    val extras = LocalCozyExtraColors.current
    androidx.compose.foundation.lazy.LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(TYPE_CHIPS, key = { it.key ?: "all" }) { chip ->
            val isActive = chip.key == selected
            val bg = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
            val fg = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
            Row(
                modifier = Modifier
                    .clip(shapes.chip)
                    .background(bg)
                    .then(
                        if (!isActive) Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, shapes.chip)
                        else Modifier
                    )
                    .clickable { onSelect(chip.key) }
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(chip.emoji, fontSize = 13.sp)
                Text(
                    chip.label,
                    fontSize = 12.sp,
                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                    color = fg,
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(label: String, count: Int) {
    Text(
        text = "$label · $count",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 1.2.sp,
        modifier = Modifier.padding(vertical = 8.dp),
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GroupRow(
    group: Group,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
) {
    val visuals = groupVisuals(group)
    val extras = LocalCozyExtraColors.current
    val shapes = LocalCozyShapes.current
    val isMuted = group.mutedAt != null

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shapes.card)
            .background(MaterialTheme.colorScheme.surface)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress,
            )
            .padding(12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(shapes.chip)
                    .background(visuals.tileBg),
                contentAlignment = Alignment.Center,
            ) {
                Text(visuals.emoji, fontSize = 20.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        group.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isMuted) extras.textTer else MaterialTheme.colorScheme.onBackground,
                    )
                    if (group.role == "owner") {
                        Text("⭐", fontSize = 11.sp)
                    }
                    if (isMuted) {
                        Text("🔕", fontSize = 11.sp)
                    }
                }
                Text(
                    text = visuals.typeLabel,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            Text("⋯", fontSize = 18.sp, color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PinCard(
    group: Group,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
) {
    val visuals = groupVisuals(group)
    val shapes = LocalCozyShapes.current
    val isMuted = group.mutedAt != null
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shapes.card)
            .background(visuals.tileBg)
            .combinedClickable(onClick = onClick, onLongClick = onLongPress)
            .padding(16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(shapes.chip)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center,
            ) {
                Text(visuals.emoji, fontSize = 24.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("📌", fontSize = 11.sp)
                    Text(
                        group.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    if (isMuted) Text("🔕", fontSize = 12.sp)
                }
                Text(
                    text = visuals.typeLabel,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun ArchivedRow(
    group: Group,
    onClick: () -> Unit,
    onUnarchive: () -> Unit,
) {
    val extras = LocalCozyExtraColors.current
    val shapes = LocalCozyShapes.current
    val visuals = groupVisuals(group)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shapes.card)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shapes.card)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(shapes.chip)
                .background(extras.surfaceSoft),
            contentAlignment = Alignment.Center,
        ) {
            Text(visuals.emoji, fontSize = 18.sp)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                group.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = extras.textTer,
            )
            Text(
                "Архив",
                fontSize = 11.sp,
                color = extras.textTer,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Text(
            "Вернуть",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clip(shapes.chip)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .clickable(onClick = onUnarchive)
                .padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupActionsSheet(
    group: Group,
    onDismiss: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleMute: () -> Unit,
    onArchive: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val shapes = LocalCozyShapes.current
    val isPinned = group.pinnedAt != null
    val isMuted = group.mutedAt != null

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
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                group.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            ActionRow(
                emoji = if (isPinned) "📌" else "☆",
                label = if (isPinned) "Открепить" else "Закрепить",
                onClick = onTogglePin,
            )
            ActionRow(
                emoji = if (isMuted) "🔔" else "🔕",
                label = if (isMuted) "Включить уведомления" else "Без уведомлений",
                onClick = onToggleMute,
            )
            ActionRow(
                emoji = "📦",
                label = "В архив",
                onClick = onArchive,
            )
        }
    }
}

@Composable
private fun ActionRow(emoji: String, label: String, onClick: () -> Unit) {
    val shapes = LocalCozyShapes.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shapes.card)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(emoji, fontSize = 18.sp)
        Text(
            label,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CreateGroupDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val groupName by remember { mutableStateOf("") }
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "Создать пространство",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )

            val workspaceTypes = listOf(
                Triple("family", "🏡", "Семья" to "Для совместного ведения домашних дел"),
                Triple("group", "👥", "Группа" to "Команда, соседи, друзья"),
                Triple("work", "💼", "Работа" to "Рабочие задачи и расписание"),
                Triple("mentoring", "🎓", "Наставничество" to "Учитель и ученики"),
            )

            workspaceTypes.forEach { (type, icon, labels) ->
                CozyCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val defaultName = labels.first
                        onConfirm(groupName.ifBlank { defaultName }, type)
                    },
                    bordered = true,
                    background = MaterialTheme.colorScheme.surface,
                    contentPadding = 12.dp,
                    radius = 14.dp,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(shapes.chip)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(icon, fontSize = 22.sp)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                labels.first,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            Text(
                                labels.second,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                        Text("›", fontSize = 18.sp, color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
}
