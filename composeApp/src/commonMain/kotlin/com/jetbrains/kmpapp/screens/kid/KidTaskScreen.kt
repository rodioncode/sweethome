package com.jetbrains.kmpapp.screens.kid

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.jetbrains.kmpapp.ui.KidTheme
import com.jetbrains.kmpapp.ui.LocalKidColors
import com.jetbrains.kmpapp.ui.LocalKidTypography
import com.jetbrains.kmpapp.ui.components.kid.KidBigButton
import com.jetbrains.kmpapp.ui.components.kid.KidAccent
import com.jetbrains.kmpapp.ui.components.kid.KidCard
import com.jetbrains.kmpapp.ui.components.kid.KidStarPill

data class KidTask(
    val id: String,
    val title: String,
    val emoji: String,
    val rewardStars: Int,
    val isDone: Boolean = false,
)

data class KidTaskState(
    val tasks: List<KidTask> = emptyList(),
    val stars: Int = 0,
)

@Composable
fun KidTaskScreen(
    state: KidTaskState,
    onTaskDone: (String) -> Unit = {},
    onDone: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    KidTheme {
        val colors = LocalKidColors.current
        val type = LocalKidTypography.current

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(colors.cream)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Задания на сегодня",
                    style = type.heading,
                    color = colors.ink,
                )
                Spacer(Modifier.weight(1f))
                KidStarPill(count = state.stars)
            }

            Spacer(Modifier.height(20.dp))

            // Task list
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.tasks, key = { it.id }) { task ->
                    KidCard(tilt = if (task.isDone) 0f else 1f) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !task.isDone) { onTaskDone(task.id) }
                                .padding(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = task.emoji,
                                style = type.heading,
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = task.title,
                                style = type.body,
                                color = if (task.isDone) colors.inkSec else colors.ink,
                                textDecoration = if (task.isDone) TextDecoration.LineThrough else null,
                                modifier = Modifier.weight(1f),
                            )
                            Spacer(Modifier.width(8.dp))
                            KidStarPill(count = task.rewardStars)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            KidBigButton(
                onClick = onDone,
                accent = KidAccent.GRASS,
                icon = "✅",
            ) {
                Text("Готово!", style = type.button)
            }
        }
    }
}
