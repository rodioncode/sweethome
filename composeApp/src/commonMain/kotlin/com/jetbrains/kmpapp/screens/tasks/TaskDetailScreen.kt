package com.jetbrains.kmpapp.screens.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetbrains.kmpapp.ui.LocalCozyExtraColors
import com.jetbrains.kmpapp.ui.LocalCozyShapes
import com.jetbrains.kmpapp.ui.LocalCozySpacing
import com.jetbrains.kmpapp.ui.components.CozyAvatar
import com.jetbrains.kmpapp.ui.components.CozyCard
import com.jetbrains.kmpapp.ui.components.CozyTopBar
import com.jetbrains.kmpapp.ui.components.MetaRow
import com.jetbrains.kmpapp.ui.components.resolve
import com.jetbrains.kmpapp.ui.components.resolveContainer
import com.jetbrains.kmpapp.ui.models.FamilyMember
import com.jetbrains.kmpapp.ui.models.Priority
import com.jetbrains.kmpapp.ui.models.Subtask
import com.jetbrains.kmpapp.ui.models.Task

data class TaskComment(
    val id: String,
    val author: FamilyMember,
    val text: String,
    val timeAgo: String,
)

@Composable
fun TaskDetailScreen(
    task: Task,
    onToggleDone: () -> Unit,
    onToggleSubtask: (subtaskId: String) -> Unit,
    onAddSubtask: (title: String) -> Unit,
    onAddComment: (text: String) -> Unit,
    onEdit: () -> Unit,
    onBack: () -> Unit,
    comments: List<TaskComment> = emptyList(),
    modifier: Modifier = Modifier,
) {
    val spacing = LocalCozySpacing.current
    val shapes = LocalCozyShapes.current
    val extras = LocalCozyExtraColors.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        CozyTopBar(
            onBack = onBack,
            title = task.listType.displayName,
            action = {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable(onClick = onEdit),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "✎",
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
            },
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.xxl),
        ) {
            // Hero: emoji + title + status checkbox
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = spacing.sm),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(spacing.md),
            ) {
                if (!task.emoji.isNullOrBlank()) {
                    Text(
                        text = task.emoji ?: "",
                        fontSize = 32.sp,
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            task.isOverdue -> extras.coral
                            task.isDone -> extras.textTer
                            else -> MaterialTheme.colorScheme.onBackground
                        },
                        textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None,
                    )
                    if (task.isOverdue) {
                        Spacer(Modifier.height(spacing.xs))
                        Box(
                            modifier = Modifier
                                .background(extras.coralSoft, shapes.chip)
                                .padding(horizontal = spacing.xs, vertical = spacing.xxs),
                        ) {
                            Text(
                                text = "Просрочено",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = extras.coral,
                            )
                        }
                    }
                }
                // Status checkbox 28dp
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            if (task.isDone) MaterialTheme.colorScheme.primary else Color.Transparent,
                        )
                        .then(
                            if (!task.isDone)
                                Modifier.border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            else Modifier,
                        )
                        .clickable(onClick = onToggleDone),
                    contentAlignment = Alignment.Center,
                ) {
                    if (task.isDone) {
                        Text(
                            text = "✓",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            Spacer(Modifier.height(spacing.xl))

            // Meta card — due, priority, assignee, list type
            CozyCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = 0.dp,
            ) {
                Column {
                    val dueText = task.typedFields["due"]
                    MetaRow(
                        icon = "📅",
                        title = "Срок",
                        value = dueText ?: "Не задан",
                    )
                    DividerLine()

                    MetaRow(
                        icon = "⚡",
                        title = "Приоритет",
                        valueAdornment = {
                            PriorityBadge(task.priority)
                        },
                    )
                    DividerLine()

                    MetaRow(
                        icon = "👤",
                        title = "Кому",
                        valueAdornment = {
                            val assignee = task.assignee
                            if (assignee != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                                ) {
                                    CozyAvatar(
                                        letter = assignee.initial,
                                        palette = assignee.avatarPalette,
                                        size = 22.dp,
                                    )
                                    Text(
                                        text = assignee.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onBackground,
                                    )
                                }
                            } else {
                                Text(
                                    text = "Не назначено",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        },
                    )
                    DividerLine()

                    MetaRow(
                        icon = "🏷",
                        title = "Тип",
                        valueAdornment = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(spacing.xxs),
                                modifier = Modifier
                                    .background(task.listType.palette.resolveContainer(), shapes.chip)
                                    .padding(horizontal = spacing.xs, vertical = spacing.xxs),
                            ) {
                                Text(task.listType.emoji, fontSize = 12.sp)
                                Text(
                                    text = task.listType.displayName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = task.listType.palette.resolve(),
                                )
                            }
                        },
                    )
                }
            }

            Spacer(Modifier.height(spacing.xl))

            // Subtasks
            CozyCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = spacing.md,
            ) {
                Column {
                    val doneCount = task.subtasks.count { it.isDone }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Подзадачи",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = "$doneCount/${task.subtasks.size}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Spacer(Modifier.height(spacing.sm))

                    task.subtasks.forEach { sub ->
                        SubtaskRow(
                            subtask = sub,
                            onToggle = { onToggleSubtask(sub.id) },
                        )
                    }

                    InlineAddSubtask(onAdd = onAddSubtask)
                }
            }

            if (!task.note.isNullOrBlank()) {
                Spacer(Modifier.height(spacing.xl))
                CozyCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = spacing.md,
                ) {
                    Column {
                        Text(
                            text = "ЗАМЕТКА",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = extras.textTer,
                        )
                        Spacer(Modifier.height(spacing.xs))
                        Text(
                            text = task.note ?: "",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }
            }

            Spacer(Modifier.height(spacing.xl))

            // Comments
            CozyCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = spacing.md,
            ) {
                Column {
                    Text(
                        text = "Комментарии",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(Modifier.height(spacing.sm))

                    if (comments.isEmpty()) {
                        Text(
                            text = "Пока нет комментариев",
                            fontSize = 13.sp,
                            color = extras.textTer,
                        )
                    } else {
                        comments.forEachIndexed { idx, c ->
                            CommentRow(c)
                            if (idx < comments.lastIndex) {
                                Spacer(Modifier.height(spacing.sm))
                            }
                        }
                    }

                    Spacer(Modifier.height(spacing.sm))
                    InlineAddComment(onSend = onAddComment)
                }
            }

            if (task.reward > 0) {
                Spacer(Modifier.height(spacing.xl))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer, shapes.card)
                        .padding(horizontal = spacing.lg, vertical = spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                ) {
                    Text(text = "⭐", fontSize = 16.sp)
                    Text(
                        text = "+${task.reward}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "награда за выполнение",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(spacing.xxxl))
        }
    }
}

@Composable
private fun DividerLine() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant),
    )
}

@Composable
private fun PriorityBadge(priority: Priority) {
    val extras = LocalCozyExtraColors.current
    val shapes = LocalCozyShapes.current
    val spacing = LocalCozySpacing.current

    val (bg, fg) = when (priority) {
        Priority.LOW -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        Priority.MEDIUM -> extras.ochreSoft to extras.ochre
        Priority.HIGH -> extras.coralSoft to extras.coral
        Priority.URGENT -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.error
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.xxs),
        modifier = Modifier
            .background(bg, shapes.chip)
            .padding(horizontal = spacing.xs, vertical = spacing.xxs),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(fg),
        )
        Text(
            text = priority.displayName,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = fg,
        )
    }
}

@Composable
private fun SubtaskRow(
    subtask: Subtask,
    onToggle: () -> Unit,
) {
    val spacing = LocalCozySpacing.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(
                    if (subtask.isDone) MaterialTheme.colorScheme.primary else Color.Transparent,
                )
                .then(
                    if (!subtask.isDone)
                        Modifier.border(1.5.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    else Modifier,
                )
                .clickable(onClick = onToggle),
            contentAlignment = Alignment.Center,
        ) {
            if (subtask.isDone) {
                Text(
                    text = "✓",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Text(
            text = subtask.title,
            fontSize = 13.sp,
            color = if (subtask.isDone)
                LocalCozyExtraColors.current.textTer
            else
                MaterialTheme.colorScheme.onBackground,
            textDecoration = if (subtask.isDone) TextDecoration.LineThrough else TextDecoration.None,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun InlineAddSubtask(onAdd: (String) -> Unit) {
    val spacing = LocalCozySpacing.current
    val shapes = LocalCozyShapes.current
    var text by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .border(1.5.dp, MaterialTheme.colorScheme.outline, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "+",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        BasicTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier
                .weight(1f)
                .background(MaterialTheme.colorScheme.surfaceVariant, shapes.button)
                .padding(horizontal = spacing.sm, vertical = spacing.xs),
            textStyle = TextStyle(
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Medium,
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                if (text.isNotBlank()) {
                    onAdd(text.trim())
                    text = ""
                }
            }),
            decorationBox = { inner ->
                if (text.isEmpty()) {
                    Text(
                        text = "Добавить подзадачу",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                inner()
            },
        )
    }
}

@Composable
private fun CommentRow(c: TaskComment) {
    val spacing = LocalCozySpacing.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        CozyAvatar(
            letter = c.author.initial,
            palette = c.author.avatarPalette,
            size = 32.dp,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = c.author.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = c.text,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 2.dp),
            )
            Text(
                text = c.timeAgo,
                fontSize = 11.sp,
                color = LocalCozyExtraColors.current.textTer,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

@Composable
private fun InlineAddComment(onSend: (String) -> Unit) {
    val spacing = LocalCozySpacing.current
    val shapes = LocalCozyShapes.current
    var text by remember { mutableStateOf("") }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.xs),
    ) {
        BasicTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier
                .weight(1f)
                .background(MaterialTheme.colorScheme.surfaceVariant, shapes.button)
                .padding(horizontal = spacing.sm, vertical = spacing.xs)
                .heightIn(min = 32.dp),
            textStyle = TextStyle(
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground,
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = {
                if (text.isNotBlank()) {
                    onSend(text.trim())
                    text = ""
                }
            }),
            decorationBox = { inner ->
                if (text.isEmpty()) {
                    Text(
                        text = "Написать комментарий…",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                inner()
            },
        )
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    if (text.isNotBlank()) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant,
                )
                .clickable(enabled = text.isNotBlank()) {
                    if (text.isNotBlank()) {
                        onSend(text.trim())
                        text = ""
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "→",
                color = if (text.isNotBlank()) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
