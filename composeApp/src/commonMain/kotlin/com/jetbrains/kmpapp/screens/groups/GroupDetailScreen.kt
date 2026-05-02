package com.jetbrains.kmpapp.screens.groups

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import com.jetbrains.kmpapp.ui.listColorForType
import com.jetbrains.kmpapp.ui.listEmojiForType
import com.jetbrains.kmpapp.ui.toComposeColor
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
    var showAddListDialog by remember { mutableStateOf(false) }
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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            // Green header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary),
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .align(Alignment.TopEnd)
                            .padding(top = 0.dp)
                            .background(Color.White.copy(alpha = 0.07f), CircleShape),
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = 12.dp, bottom = 20.dp),
                    ) {
                        // Top row: back + name + menu
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Surface(
                                onClick = navigateBack,
                                modifier = Modifier.size(36.dp),
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.15f),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("‹", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    group?.title ?: groupName,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                )
                                if (members.isNotEmpty()) {
                                    Text(
                                        "${members.size} участника",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                                    )
                                }
                            }
                            Box {
                                Surface(
                                    onClick = { menuExpanded = true },
                                    modifier = Modifier.size(36.dp),
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.15f),
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("⚙️", fontSize = 18.sp)
                                    }
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
                                            text = { Text("Удалить группу", color = Color(0xFFD32F2F)) },
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
                        }

                        // Member avatars row
                        if (members.isNotEmpty()) {
                            Spacer(Modifier.height(14.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy((-8).dp),
                            ) {
                                members.take(5).forEachIndexed { index, member ->
                                    MemberAvatar(
                                        displayName = member.displayName ?: member.userId,
                                        modifier = Modifier.offset(x = (index * (-4)).dp),
                                    )
                                }
                                if (isOwner || isAdmin) {
                                    Spacer(Modifier.width(4.dp))
                                    Surface(
                                        onClick = { viewModel.createInvite() },
                                        modifier = Modifier.size(36.dp),
                                        shape = CircleShape,
                                        color = Color.White.copy(alpha = 0.2f),
                                        border = androidx.compose.foundation.BorderStroke(2.dp, Color.White.copy(alpha = 0.4f)),
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("+", fontSize = 20.sp, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Light)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Invite code banner (shown when we have a current invite or can create one)
            if (currentInvite != null) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                "🔗 Код: ${currentInvite!!.token}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f),
                            )
                            TextButton(onClick = {
                                clipboardManager.setText(AnnotatedString(currentInvite!!.token))
                            }) {
                                Text("Поделиться →", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Lists section header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        "Списки группы",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    TextButton(onClick = { showAddListDialog = true }) {
                        Text("+ Создать", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Lists
            if (groupLists.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Нет списков. Нажмите + чтобы создать.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(groupLists, key = { "list_${it.id}" }) { list ->
                    GroupListCard(list = list, onClick = { navigateToListDetail(list.id) })
                }
            }

            // Work hours (только для type=work)
            if (group?.type == "work") {
                item {
                    Spacer(Modifier.height(16.dp))
                    Surface(
                        onClick = { if (isOwner) showWorkHoursDialog = true },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Рабочее время", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            val g = group
                            val hours = if (g?.workHoursStart != null && g.workHoursEnd != null) "${g.workHoursStart} – ${g.workHoursEnd}" else "Не задано"
                            val days = g?.workDays?.takeIf { it.isNotEmpty() }?.joinToString(", ") { dayLabel(it) } ?: "—"
                            Text(hours, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Дни: $days", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (isOwner) Text("Нажмите, чтобы изменить", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            // Action buttons
            item {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Surface(
                        onClick = { showMembersSheet = true },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("👥 Участники", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                    Surface(
                        onClick = {
                            val g = group
                            if (g != null) navigateToChat(g.id, g.title, members.size)
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primary,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("💬 Чат", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
                Spacer(Modifier.height(80.dp))
            }
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
                    Text("Удалить", color = Color(0xFFD32F2F))
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
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(token, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, letterSpacing = 4.sp)
                            Text("Действует до: ${currentInvite!!.expiresAt.take(10)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                TextButton(onClick = { clipboardManager.setText(AnnotatedString(token)); showInviteDialog = false }) {
                    Text("Скопировать код")
                }
            },
            dismissButton = {
                TextButton(onClick = { clipboardManager.setText(AnnotatedString(deepLink)); showInviteDialog = false }) {
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
                                onClick = { viewModel.transferOwnership(member.userId); showTransferDialog = false },
                                modifier = Modifier.fillMaxWidth(),
                            ) { Text(member.displayName ?: member.userId) }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showTransferDialog = false }) { Text("Отмена") } },
        )
    }

    // Add list dialog
    if (showAddListDialog) {
        var listTitle by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddListDialog = false },
            title = { Text("Новый список в группе") },
            text = {
                OutlinedTextField(
                    value = listTitle,
                    onValueChange = { listTitle = it },
                    label = { Text("Название") },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.createListInGroup(listTitle.ifBlank { "Новый список" })
                    showAddListDialog = false
                }) { Text("Создать") }
            },
            dismissButton = { TextButton(onClick = { showAddListDialog = false }) { Text("Отмена") } },
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
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    "Участники",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
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
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun MemberAvatar(displayName: String, modifier: Modifier = Modifier) {
    val colors = listOf(
        Color(0xFF5B7C5A), Color(0xFF42A5F5), Color(0xFFFF7043),
        Color(0xFFAB47BC), Color(0xFFFFA726),
    )
    val color = colors[displayName.hashCode().and(0x7FFFFFFF) % colors.size]
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(color)
            .border(2.dp, Color.White.copy(alpha = 0.4f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            displayName.take(2).uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
    }
}

@Composable
private fun MemberRow(member: GroupMember) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MemberAvatar(displayName = member.displayName ?: member.userId)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                member.displayName ?: member.userId,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (member.displayName != null) {
                Text(
                    member.userId,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        val roleLabel = when (member.role) {
            "owner" -> "Владелец"
            "admin" -> "Админ"
            "mentor" -> "Наставник"
            else -> "Участник"
        }
        Text(
            roleLabel,
            fontSize = 12.sp,
            color = if (member.role == "owner") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (member.role == "owner") FontWeight.Bold else FontWeight.Normal,
        )
    }
}

@Composable
private fun GroupListCard(
    list: TodoList,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listColor = list.color?.toComposeColor() ?: listColorForType(list.type, isSystemInDarkTheme())
    val listIcon = list.icon ?: listEmojiForType(list.type)
    val doneCount = list.doneCount
    val totalCount = list.totalCount

    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(listColor.copy(alpha = 0.13f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(listIcon, fontSize = 22.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    list.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (doneCount != null && totalCount != null && totalCount > 0) {
                    val progress = (doneCount.toFloat() / totalCount).coerceIn(0f, 1f)
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
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

@Composable
private fun WorkHoursDialog(
    initialStart: String,
    initialEnd: String,
    initialDays: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (start: String?, end: String?, days: List<String>?) -> Unit,
) {
    var start by remember { mutableStateOf(initialStart) }
    var end by remember { mutableStateOf(initialEnd) }
    val days = remember { mutableStateOf(initialDays.toSet()) }
    val allDays = listOf("mon", "tue", "wed", "thu", "fri", "sat", "sun")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Рабочее время") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = start,
                        onValueChange = { start = it },
                        label = { Text("Начало") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = end,
                        onValueChange = { end = it },
                        label = { Text("Конец") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                }
                Text("Формат HH:MM. Дни:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    allDays.forEach { d ->
                        val selected = d in days.value
                        Surface(
                            onClick = {
                                days.value = if (selected) days.value - d else days.value + d
                            },
                            shape = CircleShape,
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(36.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    dayLabel(d),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val orderedDays = allDays.filter { it in days.value }
                onConfirm(start.takeIf { it.isNotBlank() }, end.takeIf { it.isNotBlank() }, orderedDays.takeIf { it.isNotEmpty() })
            }) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
    )
}
