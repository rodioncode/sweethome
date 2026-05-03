package com.jetbrains.kmpapp.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jetbrains.kmpapp.ui.PriorityHigh
import com.jetbrains.kmpapp.ui.PriorityLow
import com.jetbrains.kmpapp.ui.PriorityMedium
import com.jetbrains.kmpapp.ui.SweetHomeShapes
import com.jetbrains.kmpapp.ui.SweetHomeSpacing
import com.jetbrains.kmpapp.ui.listColorForType
import com.jetbrains.kmpapp.ui.listEmojiForType
import com.jetbrains.kmpapp.ui.toComposeColor
import com.jetbrains.kmpapp.ui.components.SweetHomeListCard
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeContent(
    contentPadding: PaddingValues,
    navigateToListDetail: (String) -> Unit,
    onCreateList: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToGroups: () -> Unit,
    navigateToProfile: () -> Unit,
    navigateToGoals: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel = koinViewModel<HomeViewModel>()
    val userId by viewModel.userId.collectAsStateWithLifecycle()
    val displayName by viewModel.displayName.collectAsStateWithLifecycle()
    val recentLists by viewModel.recentLists.collectAsStateWithLifecycle()
    val context by viewModel.context.collectAsStateWithLifecycle()
    val todayTasks by viewModel.todayTasks.collectAsStateWithLifecycle()
    val nearestGoal by viewModel.nearestGoal.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentPadding = PaddingValues(vertical = SweetHomeSpacing.md),
        verticalArrangement = Arrangement.spacedBy(SweetHomeSpacing.md),
    ) {
        // --- Header: greeting + avatar ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SweetHomeSpacing.lg),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = greeting(),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    val nameToShow = displayName ?: userId?.take(8)
                    nameToShow?.let {
                        Text(
                            text = it,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
                Surface(
                    onClick = navigateToProfile,
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = CircleShape,
                            ambientColor = Color.Black.copy(alpha = 0.12f),
                            spotColor = Color.Black.copy(alpha = 0.12f),
                        ),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        val initial = displayName?.firstOrNull()?.uppercase()
                            ?: userId?.firstOrNull()?.uppercase() ?: "?"
                        Text(
                            text = initial,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }
            }
        }

        // --- Context pill: Личное / Работа ---
        item {
            ContextPill(
                selected = context,
                onSelect = viewModel::setContext,
                modifier = Modifier.padding(horizontal = SweetHomeSpacing.lg),
            )
        }

        // --- Notification banner ---
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SweetHomeSpacing.lg)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(10.dp),
                        ambientColor = Color.Black.copy(alpha = 0.08f),
                        spotColor = Color.Black.copy(alpha = 0.08f),
                    ),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primary,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .padding(horizontal = SweetHomeSpacing.md),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(
                        text = "🏠 Семья Ивановых: 3 новых задачи",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        }

        // --- Quick actions ---
        item {
            Column(modifier = Modifier.padding(horizontal = SweetHomeSpacing.lg)) {
                Text(
                    text = "Быстрые действия",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = SweetHomeSpacing.sm),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SweetHomeSpacing.sm),
                ) {
                    QuickActionCard(
                        emoji = "📝",
                        label = "Новый список",
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        onClick = onCreateList,
                        modifier = Modifier.weight(1f),
                    )
                    QuickActionCard(
                        emoji = "🏠",
                        label = "Мой дом",
                        backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        onClick = onNavigateToHome,
                        modifier = Modifier.weight(1f),
                    )
                    QuickActionCard(
                        emoji = "👥",
                        label = "Группа",
                        backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        onClick = onNavigateToGroups,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        // --- "Сегодня" section ---
        item {
            Text(
                text = "Сегодня",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = SweetHomeSpacing.lg),
            )
        }
        if (todayTasks.isEmpty()) {
            item {
                TodayEmptyState(
                    context = context,
                    modifier = Modifier.padding(horizontal = SweetHomeSpacing.lg),
                )
            }
        } else {
            items(todayTasks, key = { it.id }) { task ->
                TodayTaskCard(
                    task = task,
                    onClick = { navigateToListDetail(task.listId) },
                    modifier = Modifier.padding(horizontal = SweetHomeSpacing.lg),
                )
            }
        }

        // --- Goals widget ---
        item {
            GoalsWidget(
                goal = nearestGoal,
                onClick = navigateToGoals,
                modifier = Modifier.padding(horizontal = SweetHomeSpacing.lg),
            )
        }

        // --- Recent lists header ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SweetHomeSpacing.lg),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Недавние списки",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Все →",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { /* TODO: navigate to all lists */ },
                )
            }
        }

        // --- Recent lists ---
        if (recentLists.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = SweetHomeSpacing.lg),
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
                        Text(
                            text = "Нет списков",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(Modifier.height(SweetHomeSpacing.xxs))
                        Text(
                            text = "Создайте первый список дел",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        } else {
            items(recentLists, key = { it.id }) { list ->
                val isDark = isSystemInDarkTheme()
                val color = list.color?.toComposeColor() ?: listColorForType(list.type, isDark)
                SweetHomeListCard(
                    title = list.title,
                    onClick = { navigateToListDetail(list.id) },
                    modifier = Modifier.padding(horizontal = SweetHomeSpacing.lg),
                    icon = list.icon ?: listEmojiForType(list.type),
                    listColor = color,
                    doneCount = list.doneCount,
                    totalCount = list.totalCount,
                    categoryLabel = when (list.type) {
                        "shopping" -> "Покупки"
                        "home_chores" -> "Дела"
                        else -> "Задачи"
                    }.takeIf { list.totalCount == null },
                )
            }
        }

        item { Spacer(Modifier.height(SweetHomeSpacing.xxl)) }
    }
}

@Composable
private fun ContextPill(
    selected: DashboardContext,
    onSelect: (DashboardContext) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = SweetHomeShapes.Chip,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            ContextPillSegment(
                label = "Личное",
                emoji = "🏠",
                selected = selected == DashboardContext.PERSONAL,
                onClick = { onSelect(DashboardContext.PERSONAL) },
            )
            ContextPillSegment(
                label = "Работа",
                emoji = "💼",
                selected = selected == DashboardContext.WORK,
                onClick = { onSelect(DashboardContext.WORK) },
            )
        }
    }
}

@Composable
private fun ContextPillSegment(
    label: String,
    emoji: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = SweetHomeShapes.Chip,
        color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
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
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TodayTaskCard(
    task: TodayTaskUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = isSystemInDarkTheme()
    val accent = task.listColorHex?.toComposeColor() ?: listColorForType(task.listType, isDark)
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = SweetHomeShapes.Card,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SweetHomeSpacing.md, vertical = SweetHomeSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // List icon badge
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = task.listIcon, fontSize = 18.sp)
            }
            Spacer(Modifier.width(SweetHomeSpacing.sm))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    priorityDot(task.priority)?.let { dotColor ->
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(dotColor),
                        )
                        Spacer(Modifier.width(SweetHomeSpacing.xxs))
                    }
                    Text(
                        text = task.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = task.listTitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
            Spacer(Modifier.width(SweetHomeSpacing.sm))
            DueBadge(label = task.dueLabel, isOverdue = task.isOverdue)
        }
    }
}

@Composable
private fun DueBadge(label: String, isOverdue: Boolean) {
    val bg = if (isOverdue) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
    val fg = if (isOverdue) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant
    Surface(
        shape = SweetHomeShapes.Chip,
        color = bg,
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = fg,
            modifier = Modifier.padding(horizontal = SweetHomeSpacing.xs, vertical = 4.dp),
        )
    }
}

@Composable
private fun TodayEmptyState(
    context: DashboardContext,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
            Text(text = "🎉", fontSize = 28.sp)
            Spacer(Modifier.height(SweetHomeSpacing.xs))
            Text(
                text = "На сегодня всё чисто",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(SweetHomeSpacing.xxs))
            Text(
                text = when (context) {
                    DashboardContext.PERSONAL -> "Нет задач с дедлайном на сегодня в личном пространстве"
                    DashboardContext.WORK -> "Нет рабочих задач с дедлайном на сегодня"
                },
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun GoalsWidget(
    goal: NearestGoalUi?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = SweetHomeShapes.Card,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SweetHomeSpacing.md),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = "🎯", fontSize = 22.sp)
                }
                Spacer(Modifier.width(SweetHomeSpacing.sm))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (goal == null) "Цели" else goal.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        maxLines = 1,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = if (goal == null) {
                            "Поставьте цель и отслеживайте прогресс"
                        } else {
                            buildString {
                                goal.deadlineLabel?.let { append(it) }
                                if (goal.totalSteps > 0) {
                                    if (isNotEmpty()) append(" · ")
                                    append("${goal.doneSteps}/${goal.totalSteps} шагов")
                                }
                                if (isEmpty()) append("Без дедлайна")
                            }
                        },
                        fontSize = 12.sp,
                        color = if (goal?.isOverdue == true) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                    )
                }
                Text(
                    text = "→",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
            // Прогресс-бар по шагам, если они есть
            if (goal != null && goal.totalSteps > 0) {
                Spacer(Modifier.height(SweetHomeSpacing.xs))
                val progress = goal.doneSteps.toFloat() / goal.totalSteps.toFloat()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(SweetHomeShapes.Chip)
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(6.dp)
                            .background(MaterialTheme.colorScheme.secondary),
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    emoji: String,
    label: String,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Card(
        modifier = modifier
            .height(90.dp)
            .clickable(onClick = onClick),
        shape = SweetHomeShapes.Card,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(SweetHomeSpacing.xs),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(text = emoji, fontSize = 26.sp)
            Spacer(Modifier.height(SweetHomeSpacing.xs))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = contentColor,
            )
        }
    }
}

private fun priorityDot(priority: String?): Color? = when (priority) {
    "high" -> PriorityHigh
    "medium" -> PriorityMedium
    "low" -> PriorityLow
    else -> null
}

private fun greeting(): String {
    val hour = try {
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour
    } catch (e: Exception) {
        12
    }
    return when {
        hour < 6 -> "Доброй ночи 🌙"
        hour < 12 -> "Доброе утро ☀️"
        hour < 18 -> "Добрый день 👋"
        else -> "Добрый вечер 🌆"
    }
}
