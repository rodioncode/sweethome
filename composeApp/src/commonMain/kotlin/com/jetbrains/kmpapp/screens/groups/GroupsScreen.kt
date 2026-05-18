package com.jetbrains.kmpapp.screens.groups

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
import com.jetbrains.kmpapp.data.groups.Group
import com.jetbrains.kmpapp.ui.LocalCozyExtraColors
import com.jetbrains.kmpapp.ui.LocalCozyShapes
import com.jetbrains.kmpapp.ui.components.CozyCard
import com.jetbrains.kmpapp.ui.components.EmptyHero

private data class GroupVisuals(
    val emoji: String,
    val typeLabel: String,
    val tileBg: Color,
)

@Composable
private fun groupVisuals(group: Group): GroupVisuals {
    val extras = LocalCozyExtraColors.current
    return when (group.type) {
        "family" -> GroupVisuals("🏡", "Семья", MaterialTheme.colorScheme.primaryContainer)
        "group" -> GroupVisuals("👥", "Группа", MaterialTheme.colorScheme.primaryContainer)
        "work" -> GroupVisuals("💼", "Работа", extras.lavenderSoft)
        "mentoring" -> GroupVisuals("🎓", "Наставничество", extras.lavenderSoft)
        "hobby" -> GroupVisuals("🎯", "Хобби", extras.coralSoft)
        "study" -> GroupVisuals("🎓", "Учёба", extras.ochreSoft)
        "personal" -> GroupVisuals("🌿", "Личное", extras.surfaceSoft)
        else -> GroupVisuals(group.icon ?: "👤", "Группа", MaterialTheme.colorScheme.primaryContainer)
    }
}

@Composable
internal fun GroupsContent(
    groups: List<Group>,
    isGuest: Boolean,
    contentPadding: PaddingValues,
    onGroupClick: (Group) -> Unit,
    navigateToLinkEmail: () -> Unit,
    navigateToJoinByCode: () -> Unit,
    onCreateGroup: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }

    val filtered = remember(groups, query) {
        if (query.isBlank()) groups
        else groups.filter { it.title.contains(query, ignoreCase = true) }
    }

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
                    text = "${groups.size} активных",
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
            onValueChange = { query = it },
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        )

        if (filtered.isEmpty()) {
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
                    if (groups.isEmpty()) "Нет групп" else "Ничего не нашлось",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    when {
                        isGuest -> "Привяжите email чтобы создавать и вступать в группы"
                        groups.isEmpty() -> "Нажмите + чтобы создать первую группу"
                        else -> "Попробуйте сбросить поиск"
                    },
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            val personalTypes = setOf("family", "personal", "hobby", "study", "mentoring")
            val workTypes = setOf("work", "group")
            val personal = filtered.filter { it.type in personalTypes }
            val work = filtered.filter { it.type in workTypes }
            val other = filtered.filter { it.type !in personalTypes && it.type !in workTypes }

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (personal.isNotEmpty()) {
                    item { SectionHeader(label = "🌿 ЛИЧНОЕ", count = personal.size) }
                    items(personal, key = { "p_${it.id}" }) { group ->
                        GroupRow(group = group, onClick = { onGroupClick(group) })
                    }
                }
                if (work.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(8.dp))
                        SectionHeader(label = "💼 РАБОТА", count = work.size)
                    }
                    items(work, key = { "w_${it.id}" }) { group ->
                        GroupRow(group = group, onClick = { onGroupClick(group) })
                    }
                }
                if (other.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(8.dp))
                        SectionHeader(label = "ПРОЧЕЕ", count = other.size)
                    }
                    items(other, key = { "o_${it.id}" }) { group ->
                        GroupRow(group = group, onClick = { onGroupClick(group) })
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
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

@Composable
private fun GroupRow(
    group: Group,
    onClick: () -> Unit,
) {
    val visuals = groupVisuals(group)
    CozyCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
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
                    .size(40.dp)
                    .clip(LocalCozyShapes.current.chip)
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
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    if (group.role == "owner") {
                        Text("⭐", fontSize = 11.sp)
                    }
                }
                Text(
                    text = visuals.typeLabel,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
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

