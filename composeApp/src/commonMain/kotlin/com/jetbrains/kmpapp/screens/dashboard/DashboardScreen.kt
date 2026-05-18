package com.jetbrains.kmpapp.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetbrains.kmpapp.ui.LocalCozyExtraColors
import com.jetbrains.kmpapp.ui.LocalCozyShapes
import com.jetbrains.kmpapp.ui.components.CozyAvatar
import com.jetbrains.kmpapp.ui.components.CozyCard
import com.jetbrains.kmpapp.ui.components.pet.PetAvatar
import com.jetbrains.kmpapp.ui.models.Palette
import com.jetbrains.kmpapp.ui.models.Task

@Composable
fun DashboardContent(
    state: DashboardState,
    onIntent: (DashboardIntent) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    modifier: Modifier = Modifier,
) {
    val extras = LocalCozyExtraColors.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
    ) {
            // 1. Greeting
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = state.greetingFirstLine,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 32.sp,
                    )
                    Text(
                        text = state.greetingSecondLine,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 32.sp,
                    )
                    Text(
                        text = state.dateLabel,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
                state.me?.let { me ->
                    Box(modifier = Modifier.clickable { onIntent(DashboardIntent.OpenProfile) }) {
                        CozyAvatar(letter = me.initial, palette = me.avatarPalette, size = 44.dp)
                    }
                }
            }

            // 2. Context segmented control
            Row(
                modifier = Modifier
                    .padding(start = 24.dp, top = 20.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, LocalCozyShapes.current.cardLarge)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                DashboardContext.entries.forEach { ctx ->
                    val sel = ctx == state.context
                    Box(
                        modifier = Modifier
                            .clip(LocalCozyShapes.current.card)
                            .background(if (sel) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { onIntent(DashboardIntent.SwitchContext(ctx)) }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    ) {
                        Text(
                            text = ctx.label,
                            color = if (sel) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            // 3. Today header
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(top = 24.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = "СЕГОДНЯ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "${state.todayTasks.size} задач",
                    fontSize = 12.sp,
                    color = extras.textTer,
                )
            }
            Spacer(Modifier.height(8.dp))

            // 4. Tasks
            state.todayTasks.forEach { task ->
                TaskRow(task = task, onTaskClick = { onIntent(DashboardIntent.OpenTask(task.id)) })
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(12.dp))

            // 5. Pet widget
            state.pet?.let { pet ->
                PetMiniWidget(
                    pet = pet,
                    quote = state.petQuote ?: "Молодец, ты движешься",
                    modifier = Modifier.padding(horizontal = 24.dp),
                    onClick = { onIntent(DashboardIntent.OpenPet) },
                )
                Spacer(Modifier.height(12.dp))
            }

            // 6. Activity row
            state.lastActivity?.let { activity ->
                ActivityRow(activity, modifier = Modifier.padding(horizontal = 24.dp))
            }

            Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun TaskRow(task: Task, onTaskClick: () -> Unit) {
    CozyCard(
        onClick = onTaskClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        if (task.isDone) MaterialTheme.colorScheme.primary
                        else Color.Transparent
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (task.isDone) {
                    Text("✓", color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${task.emoji ?: ""} ${task.title}".trim(),
                    color = when {
                        task.isDone -> MaterialTheme.colorScheme.onSurfaceVariant
                        task.isOverdue -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onBackground
                    },
                    fontSize = 15.sp, fontWeight = FontWeight.Medium,
                    textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None,
                )
                Text(
                    text = task.listType.displayName,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}

@Composable
private fun PetMiniWidget(pet: com.jetbrains.kmpapp.ui.models.Pet, quote: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val extras = LocalCozyExtraColors.current

    CozyCard(
        modifier = modifier.fillMaxWidth(),
        background = MaterialTheme.colorScheme.surface,
        radius = 22.dp,
        contentPadding = 14.dp,
        onClick = onClick,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PetAvatar(species = pet.species, size = 72.dp, accent = true)
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = pet.name, fontSize = 15.sp,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Text(
                        text = " · ${pet.species.displayName} · ${pet.stage.displayName}",
                        fontSize = 11.sp, color = extras.textTer,
                    )
                }
                Text(
                    text = "«$quote»",
                    fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LevelBar(
                        progress = pet.level / 12f,
                        accent = extras.ochre,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "${pet.level}/12",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = extras.ochre,
                    )
                }
            }
        }
    }
}

@Composable
private fun LevelBar(progress: Float, accent: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(5.dp)
            .clip(LocalCozyShapes.current.pill)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .clip(LocalCozyShapes.current.pill)
                .background(accent)
        )
    }
}

@Composable
private fun ActivityRow(activity: FamilyActivity, modifier: Modifier = Modifier) {
    CozyCard(modifier = modifier.fillMaxWidth(), bordered = true) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row {
                activity.members.forEachIndexed { i, m ->
                    val offset = if (i == 0) 0.dp else (-8).dp
                    Box(modifier = Modifier.offset(x = offset)) {
                        CozyAvatar(letter = m.initial, palette = m.avatarPalette, size = 28.dp)
                    }
                }
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = activity.message,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = activity.when_,
                fontSize = 11.sp,
                color = LocalCozyExtraColors.current.textTer,
            )
        }
    }
}
