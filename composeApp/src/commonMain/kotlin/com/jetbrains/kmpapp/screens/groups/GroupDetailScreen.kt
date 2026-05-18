package com.jetbrains.kmpapp.screens.groups

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jetbrains.kmpapp.data.groups.GroupMember
import com.jetbrains.kmpapp.data.groups.Invite
import com.jetbrains.kmpapp.data.lists.TodoList
import com.jetbrains.kmpapp.ui.LocalCozyExtraColors
import com.jetbrains.kmpapp.ui.LocalCozyShapes
import com.jetbrains.kmpapp.ui.components.CozyAvatar
import com.jetbrains.kmpapp.ui.components.CozyCard
import com.jetbrains.kmpapp.ui.components.CozyTopBar
import com.jetbrains.kmpapp.ui.listColorForType
import com.jetbrains.kmpapp.ui.listEmojiForType
import com.jetbrains.kmpapp.ui.toComposeColor
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    groupId: String,
    groupName: String,
    navigateBack: () -> Unit,
    navigateToListDetail: (String) -> Unit,
    navigateToLinkEmail: () -> Unit,
    navigateToChat: (workspaceId: String, title: String, memberCount: Int) -> Unit = { _, _, _ -> },
    navigateToCreateList: (workspaceId: String) -> Unit = {},
) {
    val viewModel = koinViewModel<GroupDetailViewModel>()
    val group by viewModel.group.collectAsStateWithLifecycle()
    val groupLists by viewModel.groupLists.collectAsStateWithLifecycle()
    val members by viewModel.members.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboardManager = LocalClipboardManager.current

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showInviteDialog by remember { mutableStateOf(false) }
    var currentInvite by remember { mutableStateOf<Invite?>(null) }
    var showTransferDialog by remember { mutableStateOf(false) }
    var showMembersSheet by remember { mutableStateOf(false) }
    var showWorkHoursDialog by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(groupId) { viewModel.load(groupId) }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is GroupDetailUiEvent.ShowInvite -> {
                    currentInvite = event.invite
                    showInviteDialog = true
                }
                is GroupDetailUiEvent.NavigateToLinkEmail -> navigateToLinkEmail()
                is GroupDetailUiEvent.GroupDeleted -> navigateBack()
                is GroupDetailUiEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    val isOwner = group?.role == "owner"
    val isAdmin = group?.role == "admin"
    val extras = LocalCozyExtraColors.current
    val shapes = LocalCozyShapes.current

    val visuals = remember(group?.type) {
        when (group?.type) {
            "family" -> Triple("🏡", "Семья", null)
            "work" -> Triple("💼", "Работа", null)
            "group" -> Triple("👥", "Группа", null)
            "mentoring" -> Triple("🎓", "Наставничество", null)
            "hobby" -> Triple("🏃", "Хобби", null)
            "study" -> Triple("📚", "Учёба", null)
            else -> Triple(group?.icon ?: "👤", "Группа", null)
        }
    }
    val heroBg: Color = when (group?.type) {
        "hobby" -> extras.coralSoft
        "work" -> extras.lavenderSoft
        "study" -> extras.ochreSoft
        "mentoring" -> extras.lavenderSoft
        else -> MaterialTheme.colorScheme.primaryContainer
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            // TopBar
            item {
                CozyTopBar(
                    onBack = navigateBack,
                    action = {
                        Box {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { menuExpanded = true },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("⋯", fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
                            }
                            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                                if (isOwner || isAdmin) {
                                    DropdownMenuItem(
                                        text = { Text("Пригласить") },
                                        onClick = { menuExpanded = false; viewModel.createInvite() },
                                    )
                                }
                                if (isOwner) {
                                    DropdownMenuItem(
                                        text = { Text("Передать роль владельца") },
                                        onClick = { menuExpanded = false; showTransferDialog = true },
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                "Удалить группу",
                                                color = MaterialTheme.colorScheme.error,
                                            )
                                        },
                                        onClick = { menuExpanded = false; showDeleteConfirm = true },
                                    )
                                }
                                if (!isOwner) {
                                    DropdownMenuItem(
                                        text = { Text("Выйти из группы") },
                                        onClick = { menuExpanded = false; viewModel.leaveGroup() },
                                    )
                                }
                            }
                        }
                    },
                )
            }

            // Hero — avatar + title + meta
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(shapes.cardLarge)
                            .background(heroBg),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(visuals.first, fontSize = 40.sp)
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(
                        group?.title ?: groupName,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${members.size} участников · ${groupLists.size} списков · ${visuals.second.lowercase()}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Action buttons
            item {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ActionButton(
                        label = "👥 Участники",
                        modifier = Modifier.weight(1f),
                        primary = false,
                        onClick = { showMembersSheet = true },
                    )
                    ActionButton(
                        label = "💬 Чат",
                        modifier = Modifier.weight(1f),
                        primary = true,
                        onClick = {
                            val g = group
                            if (g != null) navigateToChat(g.id, g.title, members.size)
                        },
                    )
                }
            }

            // Members section
            if (members.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(24.dp))
                    SectionLabel(label = "УЧАСТНИКИ", modifier = Modifier.padding(horizontal = 24.dp))
                    Spacer(Modifier.height(8.dp))
                }
                item {
                    CozyCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        contentPadding = 4.dp,
                        radius = 18.dp,
                    ) {
                        Column {
                            members.forEachIndexed { idx, member ->
                                MemberRow(member = member)
                                if (idx < members.size - 1) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(start = 60.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Lists section
            item {
                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "СПИСКИ ГРУППЫ · ${groupLists.size}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.2.sp,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        "+ Создать",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable { navigateToCreateList(groupId) }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
                Spacer(Modifier.height(8.dp))
            }

            if (groupLists.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 20.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "Нет списков. Нажмите + чтобы создать.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                items(groupLists, key = { "l_${it.id}" }) { list ->
                    GroupListCard(
                        list = list,
                        onClick = { navigateToListDetail(list.id) },
                    )
                }
            }

            // Invite code card
            if (currentInvite != null) {
                item {
                    Spacer(Modifier.height(20.dp))
                    InviteCard(
                        token = currentInvite!!.token,
                        modifier = Modifier.padding(horizontal = 24.dp),
                        onCopy = { clipboardManager.setText(AnnotatedString(currentInvite!!.token)) },
                    )
                }
            } else if (isOwner || isAdmin) {
                item {
                    Spacer(Modifier.height(20.dp))
                    CozyCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        onClick = { viewModel.createInvite() },
                        background = MaterialTheme.colorScheme.primaryContainer,
                        contentPadding = 16.dp,
                        radius = 18.dp,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🔗", fontSize = 24.sp)
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "КОД ПРИГЛАШЕНИЯ",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 1.2.sp,
                                )
                                Text(
                                    "Создать новый код",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.padding(top = 4.dp),
                                )
                            }
                            Text("→", fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            // Work hours (type=work)
            if (group?.type == "work") {
                item {
                    Spacer(Modifier.height(20.dp))
                    val g = group
                    CozyCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        onClick = if (isOwner) ({ showWorkHoursDialog = true }) else null,
                        bordered = true,
                        contentPadding = 16.dp,
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                "Рабочее время",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            val hours = if (g?.workHoursStart != null && g.workHoursEnd != null) "${g.workHoursStart} – ${g.workHoursEnd}" else "Не задано"
                            val days = g?.workDays?.takeIf { it.isNotEmpty() }?.joinToString(", ") { dayLabel(it) } ?: "—"
                            Text(hours, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Дни: $days", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            workHoursStatus(g?.workHoursStart, g?.workHoursEnd, g?.workDays)?.let { (label, isWork) ->
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isWork) extras.success else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            if (isOwner) {
                                Text(
                                    "Нажмите, чтобы изменить",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                }
            }

            // Leave button (non-owner)
            if (!isOwner && group != null) {
                item {
                    Spacer(Modifier.height(20.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .height(48.dp)
                            .clip(shapes.button)
                            .border(1.dp, extras.coral, shapes.button)
                            .clickable { viewModel.leaveGroup() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "Покинуть группу",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = extras.coral,
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    // Delete confirmation
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Удалить группу?") },
            text = { Text("Все списки группы будут удалены. Это действие необратимо.") },
            confirmButton = {
                TextButton(onClick = { showDeleteConfirm = false; viewModel.deleteGroup() }) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Отмена") } },
        )
    }

    // Invite dialog
    if (showInviteDialog && currentInvite != null) {
        val token = currentInvite!!.token
        val deepLink = "sweethome://invite/$token"
        AlertDialog(
            onDismissRequest = { showInviteDialog = false },
            title = { Text("Приглашение в группу") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CozyCard(
                        modifier = Modifier.fillMaxWidth(),
                        background = MaterialTheme.colorScheme.primaryContainer,
                        contentPadding = 16.dp,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                token,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                letterSpacing = 4.sp,
                            )
                            Text(
                                "Действует до: ${currentInvite!!.expiresAt.take(10)}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }
                    }
                    Text(
                        "Поделитесь кодом или ссылкой. Участник вводит код на экране «Вступить по коду».",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    clipboardManager.setText(AnnotatedString(token))
                    showInviteDialog = false
                }) {
                    Text("Скопировать код")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    clipboardManager.setText(AnnotatedString(deepLink))
                    showInviteDialog = false
                }) {
                    Text("Скопировать ссылку")
                }
            },
        )
    }

    // Transfer ownership dialog
    if (showTransferDialog) {
        val nonOwnerMembers = members.filter { it.role != "owner" }
        AlertDialog(
            onDismissRequest = { showTransferDialog = false },
            title = { Text("Передать роль владельца") },
            text = {
                if (nonOwnerMembers.isEmpty()) {
                    Text("Нет участников для передачи роли")
                } else {
                    Column {
                        nonOwnerMembers.forEach { member ->
                            TextButton(
                                onClick = {
                                    viewModel.transferOwnership(member.userId)
                                    showTransferDialog = false
                                },
                                modifier = Modifier.fillMaxWidth(),
                            ) { Text(member.displayName ?: member.userId) }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showTransferDialog = false }) { Text("Отмена") }
            },
        )
    }

    // Work hours editor
    if (showWorkHoursDialog) {
        val g = group
        WorkHoursDialog(
            initialStart = g?.workHoursStart ?: "09:00",
            initialEnd = g?.workHoursEnd ?: "18:00",
            initialDays = g?.workDays ?: listOf("mon", "tue", "wed", "thu", "fri"),
            onDismiss = { showWorkHoursDialog = false },
            onConfirm = { s, e, d ->
                viewModel.updateWorkHours(s, e, d)
                showWorkHoursDialog = false
            },
        )
    }

    // Members bottom sheet
    if (showMembersSheet) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { showMembersSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = shapes.sheet,
        ) {
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    "Участники",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
                if (members.isEmpty()) {
                    Text(
                        "Нет участников",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 24.dp),
                    )
                } else {
                    members.forEach { member ->
                        MemberRow(member = member)
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun SectionLabel(label: String, modifier: Modifier = Modifier) {
    Text(
        text = label,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 1.2.sp,
        modifier = modifier,
    )
}

@Composable
private fun ActionButton(
    label: String,
    primary: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shapes = LocalCozyShapes.current
    val bg = if (primary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val fg = if (primary) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(shapes.button)
            .background(bg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = fg)
    }
}

@Composable
private fun memberColor(seed: String): Color {
    val extras = LocalCozyExtraColors.current
    val palette = listOf(
        MaterialTheme.colorScheme.primary,
        extras.lavender,
        extras.coral,
        extras.ochre,
        extras.primaryLight,
    )
    return palette[(seed.hashCode() and 0x7FFFFFFF) % palette.size]
}

@Composable
private fun MemberRow(member: GroupMember) {
    val displayName = member.displayName ?: member.userId
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CozyAvatar(
            letter = displayName.firstOrNull()?.uppercase() ?: "?",
            color = memberColor(displayName),
            size = 40.dp,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                displayName,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            val roleLabel = when (member.role) {
                "owner" -> "Владелец"
                "admin" -> "Админ"
                "mentor" -> "Наставник"
                else -> "Участник"
            }
            Text(
                roleLabel,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        if (member.role == "owner") {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(
                    "OWNER",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun InviteCard(
    token: String,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shapes = LocalCozyShapes.current
    CozyCard(
        modifier = modifier.fillMaxWidth(),
        background = MaterialTheme.colorScheme.primaryContainer,
        contentPadding = 16.dp,
        radius = 18.dp,
    ) {
        Column {
            Text(
                "КОД ПРИГЛАШЕНИЯ",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.2.sp,
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 6-cell display
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    val padded = token.take(6).padEnd(6, ' ')
                    for (i in 0 until 6) {
                        val ch = padded[i].toString()
                        Text(
                            text = ch,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            letterSpacing = 2.sp,
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(shapes.button)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable(onClick = onCopy)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                ) {
                    Text(
                        "Копировать",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Действителен 7 дней",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun GroupListCard(
    list: TodoList,
    onClick: () -> Unit,
) {
    val listColor = list.color?.toComposeColor() ?: listColorForType(list.type, isSystemInDarkTheme())
    val listIcon = list.icon ?: listEmojiForType(list.type)
    val doneCount = list.doneCount
    val totalCount = list.totalCount

    CozyCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 4.dp),
        onClick = onClick,
        bordered = true,
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
                    .clip(LocalCozyShapes.current.chip)
                    .background(listColor.copy(alpha = 0.13f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(listIcon, fontSize = 22.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    list.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (doneCount != null && totalCount != null && totalCount > 0) {
                    val progress = (doneCount.toFloat() / totalCount).coerceIn(0f, 1f)
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = listColor,
                        trackColor = MaterialTheme.colorScheme.outlineVariant,
                        strokeCap = StrokeCap.Round,
                    )
                }
            }
            if (doneCount != null && totalCount != null) {
                Text(
                    "$doneCount/$totalCount",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun dayLabel(code: String): String = when (code) {
    "mon" -> "Пн"; "tue" -> "Вт"; "wed" -> "Ср"; "thu" -> "Чт"
    "fri" -> "Пт"; "sat" -> "Сб"; "sun" -> "Вс"; else -> code
}

private fun parseHHmm(value: String?): Pair<Int, Int>? {
    if (value.isNullOrBlank()) return null
    val parts = value.split(":")
    if (parts.size != 2) return null
    val h = parts[0].toIntOrNull() ?: return null
    val m = parts[1].toIntOrNull() ?: return null
    if (h !in 0..23 || m !in 0..59) return null
    return h to m
}

private fun formatHHmm(hour: Int, minute: Int): String =
    "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"

private fun DayOfWeek.toCode(): String = when (this) {
    DayOfWeek.MONDAY -> "mon"; DayOfWeek.TUESDAY -> "tue"; DayOfWeek.WEDNESDAY -> "wed"
    DayOfWeek.THURSDAY -> "thu"; DayOfWeek.FRIDAY -> "fri"; DayOfWeek.SATURDAY -> "sat"
    DayOfWeek.SUNDAY -> "sun"
    else -> "mon"
}

private fun workHoursStatus(start: String?, end: String?, days: List<String>?): Pair<String, Boolean>? {
    val s = parseHHmm(start) ?: return null
    val e = parseHHmm(end) ?: return null
    val ldt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val today = ldt.dayOfWeek.toCode()
    val isWorkDay = days.isNullOrEmpty() || today in days
    val nowMin = ldt.hour * 60 + ldt.minute
    val startMin = s.first * 60 + s.second
    val endMin = e.first * 60 + e.second
    val isWorkHour = if (startMin <= endMin) {
        nowMin in startMin until endMin
    } else {
        nowMin >= startMin || nowMin < endMin
    }
    val active = isWorkDay && isWorkHour
    return (if (active) "🟢 Сейчас рабочее время" else "🌙 Сейчас нерабочее время") to active
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkHoursDialog(
    initialStart: String,
    initialEnd: String,
    initialDays: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (start: String?, end: String?, days: List<String>?) -> Unit,
) {
    val (sH, sM) = parseHHmm(initialStart) ?: (9 to 0)
    val (eH, eM) = parseHHmm(initialEnd) ?: (18 to 0)
    val startState = rememberTimePickerState(initialHour = sH, initialMinute = sM, is24Hour = true)
    val endState = rememberTimePickerState(initialHour = eH, initialMinute = eM, is24Hour = true)
    val days = remember { mutableStateOf(initialDays.toSet()) }
    val allDays = listOf("mon", "tue", "wed", "thu", "fri", "sat", "sun")
    var editingStart by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Рабочее время") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TimeChip(
                        label = "Начало",
                        time = formatHHmm(startState.hour, startState.minute),
                        selected = editingStart,
                        onClick = { editingStart = true },
                        modifier = Modifier.weight(1f),
                    )
                    TimeChip(
                        label = "Конец",
                        time = formatHHmm(endState.hour, endState.minute),
                        selected = !editingStart,
                        onClick = { editingStart = false },
                        modifier = Modifier.weight(1f),
                    )
                }
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    if (editingStart) TimePicker(state = startState) else TimePicker(state = endState)
                }
                Text("Дни недели:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    allDays.forEach { d ->
                        val selected = d in days.value
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable {
                                    days.value = if (selected) days.value - d else days.value + d
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                dayLabel(d),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val orderedDays = allDays.filter { it in days.value }
                onConfirm(
                    formatHHmm(startState.hour, startState.minute),
                    formatHHmm(endState.hour, endState.minute),
                    orderedDays.takeIf { it.isNotEmpty() },
                )
            }) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
    )
}

@Composable
private fun TimeChip(
    label: String,
    time: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shapes = LocalCozyShapes.current
    Box(
        modifier = modifier
            .clip(shapes.chip)
            .background(
                if (selected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outlineVariant,
                shape = shapes.chip,
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                time,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}
