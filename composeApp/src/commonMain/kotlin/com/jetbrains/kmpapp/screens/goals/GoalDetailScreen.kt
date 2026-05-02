package com.jetbrains.kmpapp.screens.goals

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jetbrains.kmpapp.data.goals.GoalStep
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun GoalDetailScreen(
    goalId: String,
    navigateBack: () -> Unit,
) {
    val vm = koinViewModel<GoalDetailViewModel>()
    val goal by vm.goal.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf(false) }
    var addingStep by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<GoalStep?>(null) }
    var archiving by remember { mutableStateOf(false) }

    LaunchedEffect(goalId) { vm.load(goalId) }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Surface(onClick = navigateBack, modifier = Modifier.size(36.dp), shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                        Box(contentAlignment = Alignment.Center) { Text("‹", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) }
                    }
                    Text("Цель", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                    Surface(onClick = { editing = true }, modifier = Modifier.size(36.dp), shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                        Box(contentAlignment = Alignment.Center) { Text("✎", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface) }
                    }
                    Spacer(Modifier.size(4.dp))
                    Surface(onClick = { archiving = true }, modifier = Modifier.size(36.dp), shape = CircleShape, color = MaterialTheme.colorScheme.errorContainer) {
                        Box(contentAlignment = Alignment.Center) { Text("✕", fontSize = 14.sp, color = MaterialTheme.colorScheme.error) }
                    }
                }
            }

            val g = goal
            if (g == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Загрузка…", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                return@Scaffold
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text(g.title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        g.description?.takeIf { it.isNotBlank() }?.let {
                            Spacer(Modifier.height(6.dp))
                            Text(it, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        g.deadline?.takeIf { it.isNotBlank() }?.let {
                            Spacer(Modifier.height(6.dp))
                            Text("📅 до $it", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(checked = g.isDone, onCheckedChange = vm::setDone)
                            Spacer(Modifier.size(8.dp))
                            Text(if (g.isDone) "Цель достигнута" else "В работе", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                        if (g.steps.isNotEmpty()) {
                            val total = g.steps.size
                            val done = g.steps.count { it.isDone }
                            Spacer(Modifier.height(10.dp))
                            LinearProgressIndicator(
                                progress = { done.toFloat() / total },
                                modifier = Modifier.fillMaxWidth().height(6.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.outlineVariant,
                                strokeCap = StrokeCap.Round,
                            )
                            Text("$done из $total шагов", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Шаги", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                        Surface(onClick = { addingStep = true }, shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.primary) {
                            Text("+ Шаг", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
                items(g.steps.sortedBy { it.sortOrder }, key = { it.id }) { step ->
                    StepRow(step = step, onToggle = { vm.toggleStep(step) }, onDelete = { deleting = step })
                }
                item { Spacer(Modifier.height(40.dp)) }
            }
        }
    }

    if (editing) {
        EditGoalDialog(
            initial = goal,
            onDismiss = { editing = false },
            onConfirm = { t, d, dl ->
                vm.edit(t, d, dl ?: "")    // пустая строка → очистить deadline
                editing = false
            },
        )
    }
    if (addingStep) {
        var stepTitle by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { addingStep = false },
            title = { Text("Новый шаг") },
            text = { OutlinedTextField(value = stepTitle, onValueChange = { stepTitle = it }, label = { Text("Название") }, singleLine = true) },
            confirmButton = {
                TextButton(onClick = { vm.addStep(stepTitle.trim()); addingStep = false }, enabled = stepTitle.isNotBlank()) { Text("Добавить") }
            },
            dismissButton = { TextButton(onClick = { addingStep = false }) { Text("Отмена") } },
        )
    }
    deleting?.let { step ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Удалить шаг?") },
            text = { Text(step.title) },
            confirmButton = { TextButton(onClick = { vm.deleteStep(step); deleting = null }) { Text("Удалить", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("Отмена") } },
        )
    }
    if (archiving) {
        AlertDialog(
            onDismissRequest = { archiving = false },
            title = { Text("Удалить цель?") },
            text = { Text("Цель будет архивирована. Шаги тоже исчезнут.") },
            confirmButton = {
                TextButton(onClick = { archiving = false; vm.archive(navigateBack) }) { Text("Удалить", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { archiving = false }) { Text("Отмена") } },
        )
    }
}

@Composable
private fun StepRow(step: GoalStep, onToggle: () -> Unit, onDelete: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Surface(
                onClick = onToggle,
                shape = CircleShape,
                color = if (step.isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(28.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (step.isDone) Text("✓", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
            Text(
                step.title,
                fontSize = 14.sp,
                color = if (step.isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                textDecoration = if (step.isDone) TextDecoration.LineThrough else null,
                modifier = Modifier.weight(1f),
            )
            Surface(onClick = onDelete, shape = CircleShape, color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f), modifier = Modifier.size(28.dp)) {
                Box(contentAlignment = Alignment.Center) { Text("✕", fontSize = 12.sp, color = MaterialTheme.colorScheme.error) }
            }
        }
    }
}
