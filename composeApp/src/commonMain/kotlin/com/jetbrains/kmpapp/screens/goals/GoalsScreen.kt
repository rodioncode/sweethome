package com.jetbrains.kmpapp.screens.goals

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jetbrains.kmpapp.data.goals.Goal
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun GoalsScreen(
    navigateBack: () -> Unit,
    navigateToGoal: (String) -> Unit,
) {
    val vm = koinViewModel<GoalsViewModel>()
    val goals by vm.goals.collectAsStateWithLifecycle()
    val workspaceId by vm.familyWorkspaceId.collectAsStateWithLifecycle()
    var creating by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            if (workspaceId != null) {
                FloatingActionButton(onClick = { creating = true }) {
                    Text("+", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Surface(onClick = navigateBack, modifier = Modifier.size(36.dp), shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("‹", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Text("🎯 Цели", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }

            if (workspaceId == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Создайте семейное пространство, чтобы ставить цели", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(24.dp))
                }
            } else if (goals.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🎯", fontSize = 40.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("Целей пока нет — нажмите +", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 8.dp), contentPadding = PaddingValues(bottom = 100.dp)) {
                    items(goals, key = { it.id }) { goal ->
                        GoalCard(goal = goal, onClick = { navigateToGoal(goal.id) })
                    }
                }
            }
        }
    }

    if (creating) {
        EditGoalDialog(
            initial = null,
            onDismiss = { creating = false },
            onConfirm = { title, desc, deadline ->
                vm.create(title, desc, deadline)
                creating = false
            },
        )
    }
}

@Composable
private fun GoalCard(goal: Goal, onClick: () -> Unit) {
    val total = goal.steps.size
    val done = goal.steps.count { it.isDone }
    val progress = if (total == 0) (if (goal.isDone) 1f else 0f) else done.toFloat() / total
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (goal.isDone) "✅" else "🎯", fontSize = 22.sp)
                Spacer(Modifier.size(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(goal.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    val sub = buildList {
                        if (total > 0) add("$done из $total шагов")
                        goal.deadline?.takeIf { it.isNotBlank() }?.let { add("до $it") }
                    }.joinToString(" · ")
                    if (sub.isNotEmpty()) Text(sub, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (total > 0 || goal.isDone) {
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.outlineVariant,
                    strokeCap = StrokeCap.Round,
                )
            }
        }
    }
}

@Composable
internal fun EditGoalDialog(
    initial: Goal?,
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String?, deadline: String?) -> Unit,
) {
    var title by remember { mutableStateOf(initial?.title.orEmpty()) }
    var description by remember { mutableStateOf(initial?.description.orEmpty()) }
    var deadline by remember { mutableStateOf(initial?.deadline.orEmpty()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Новая цель" else "Изменить цель") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Название") }, singleLine = true)
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Описание (опц.)") })
                OutlinedTextField(value = deadline, onValueChange = { deadline = it }, label = { Text("Дедлайн YYYY-MM-DD (опц.)") }, singleLine = true)
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(title.trim(), description.trim().ifBlank { null }, deadline.trim().ifBlank { null }) },
                enabled = title.isNotBlank(),
            ) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
    )
}
