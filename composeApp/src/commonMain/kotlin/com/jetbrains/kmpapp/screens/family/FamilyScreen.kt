package com.jetbrains.kmpapp.screens.family

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jetbrains.kmpapp.data.lists.TodoList
import com.jetbrains.kmpapp.ui.DividerColor
import com.jetbrains.kmpapp.ui.OnPrimaryWhite
import com.jetbrains.kmpapp.ui.PrimaryGreen
import com.jetbrains.kmpapp.ui.PrimaryGreenLight
import com.jetbrains.kmpapp.ui.SurfaceVariantCream
import com.jetbrains.kmpapp.ui.SweetHomeShapes
import com.jetbrains.kmpapp.ui.SweetHomeSpacing
import com.jetbrains.kmpapp.ui.TextPrimary
import com.jetbrains.kmpapp.ui.TextSecondary
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun FamilyContent(
    contentPadding: PaddingValues,
    onSpaceClick: (groupId: String, groupName: String) -> Unit,
    onListClick: (String) -> Unit,
    navigateToGamification: () -> Unit = {},
    navigateToShop: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val viewModel = koinViewModel<FamilyViewModel>()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val familySpace by viewModel.familySpace.collectAsStateWithLifecycle()
    val familyLists by viewModel.familyLists.collectAsStateWithLifecycle()
    val isCreating by viewModel.isCreating.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showCreateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(familySpace) {
        if (familySpace != null) viewModel.loadFamilyLists()
    }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding),
    ) {
        SnackbarHost(snackbarHostState)

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = PrimaryGreen)
            }
        } else if (familySpace == null) {
            EmptyFamilyState(
                isCreating = isCreating,
                onCreateClick = { showCreateDialog = true },
            )
        } else {
            FamilyHomeContent(
                spaceName = familySpace!!.title,
                memberCount = 0,
                lists = familyLists,
                onSettingsClick = { onSpaceClick(familySpace!!.id, familySpace!!.title) },
                onListClick = onListClick,
                onSpaceClick = { onSpaceClick(familySpace!!.id, familySpace!!.title) },
                onGamificationClick = navigateToGamification,
                onShopClick = navigateToShop,
            )
        }
    }

    if (showCreateDialog) {
        CreateFamilySpaceDialog(
            isLoading = isCreating,
            onDismiss = { showCreateDialog = false },
            onConfirm = { name ->
                viewModel.createFamilySpace(name)
                showCreateDialog = false
            },
        )
    }
}

// ─── Empty state ───

@Composable
private fun EmptyFamilyState(
    isCreating: Boolean,
    onCreateClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Top bar
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = SweetHomeSpacing.md, vertical = 18.dp),
            ) {
                Text(
                    text = "Мой дом",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                )
            }
        }

        // Illustration — concentric circles with house emoji
        item {
            Spacer(Modifier.height(SweetHomeSpacing.xxl))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(220.dp),
            ) {
                // Outer circle
                Surface(
                    modifier = Modifier.size(220.dp),
                    shape = CircleShape,
                    color = PrimaryGreenLight.copy(alpha = 0.15f),
                ) {}
                // Middle circle
                Surface(
                    modifier = Modifier.size(160.dp),
                    shape = CircleShape,
                    color = PrimaryGreenLight.copy(alpha = 0.25f),
                ) {}
                // Center
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    color = PrimaryGreenLight.copy(alpha = 0.4f),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("🏠", fontSize = 36.sp)
                    }
                }
            }
        }

        // Title
        item {
            Spacer(Modifier.height(SweetHomeSpacing.xl))
            Text(
                text = "У вас ещё нет\nсемейного дома",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center,
            )
        }

        // Subtitle
        item {
            Spacer(Modifier.height(SweetHomeSpacing.sm))
            Text(
                text = "Создайте пространство для близких —\nделитесь списками, следите за задачами\nи общайтесь вместе",
                fontSize = 15.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = SweetHomeSpacing.xl),
            )
        }

        // Feature cards row
        item {
            Spacer(Modifier.height(SweetHomeSpacing.lg))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SweetHomeSpacing.lg),
                horizontalArrangement = Arrangement.spacedBy(SweetHomeSpacing.xs),
            ) {
                FeatureCard("📋", "Общие списки", Modifier.weight(1f))
                FeatureCard("✅", "Задачи для всех", Modifier.weight(1f))
                FeatureCard("💬", "Семейный чат", Modifier.weight(1f))
            }
        }

        // Create button
        item {
            Spacer(Modifier.height(SweetHomeSpacing.lg))
            Surface(
                onClick = onCreateClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SweetHomeSpacing.md)
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                color = PrimaryGreen,
                shadowElevation = 6.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isCreating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = OnPrimaryWhite,
                        )
                    } else {
                        Text(
                            text = "Создать семейный дом",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OnPrimaryWhite,
                        )
                    }
                }
            }
        }

        // Join by code
        item {
            Spacer(Modifier.height(SweetHomeSpacing.md))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SweetHomeSpacing.md),
                shape = SweetHomeShapes.Card,
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .padding(horizontal = SweetHomeSpacing.md),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(
                        text = "Вступить по коду приглашения",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryGreen,
                    )
                }
            }
            Spacer(Modifier.height(SweetHomeSpacing.lg))
        }
    }
}

@Composable
private fun FeatureCard(
    emoji: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.height(72.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(emoji, fontSize = 22.sp)
            Spacer(Modifier.height(SweetHomeSpacing.xxs))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ─── Family Home (has data) ───

@Composable
private fun FamilyHomeContent(
    spaceName: String,
    memberCount: Int,
    lists: List<TodoList>,
    onSettingsClick: () -> Unit,
    onListClick: (String) -> Unit,
    onSpaceClick: () -> Unit,
    onGamificationClick: () -> Unit = {},
    onShopClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        // Green header with mini stats inside
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryGreen),
            ) {
                // Decorative circle
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .align(Alignment.TopEnd)
                        .background(Color.White.copy(alpha = 0.05f), CircleShape),
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = SweetHomeSpacing.lg)
                        .padding(top = 14.dp, bottom = 16.dp),
                ) {
                    // Top row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Семейное пространство",
                                fontSize = 12.sp,
                                color = OnPrimaryWhite.copy(alpha = 0.65f),
                            )
                            Text(
                                spaceName,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnPrimaryWhite,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            HeaderIconButton("🏆", onClick = onGamificationClick)
                            HeaderIconButton("⚙️", onClick = onSettingsClick)
                        }
                    }
                    // Mini stats grid
                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        listOf(
                            "0" to "выполнено",
                            lists.size.toString() to "списка",
                            "0" to "задач сегодня",
                        ).forEach { (value, label) ->
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White.copy(alpha = 0.12f),
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = OnPrimaryWhite)
                                    Text(label, fontSize = 10.sp, color = OnPrimaryWhite.copy(alpha = 0.7f), modifier = Modifier.padding(top = 1.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Nav cards
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SweetHomeSpacing.lg, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                listOf(
                    Triple("📋", "Списки",    Color(0xFFE8F3E8)),
                    Triple("💬", "Чат",       Color(0xFFFFF3E0)),
                    Triple("⚙️", "Настройки", Color(0xFFF3E5F5)),
                ).forEach { (emoji, label, bg) ->
                    NavCard(
                        emoji = emoji,
                        label = label,
                        bgColor = bg,
                        onClick = onSpaceClick,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        // Gamification banner
        item {
            Surface(
                onClick = onGamificationClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SweetHomeSpacing.lg),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF3D5C3C),
                shadowElevation = 2.dp,
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(
                        "🏆",
                        fontSize = 60.sp,
                        modifier = Modifier.align(Alignment.CenterEnd),
                        color = Color.White.copy(alpha = 0.15f),
                    )
                    Column {
                        Text("Семейный рейтинг", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = OnPrimaryWhite)
                        Text(
                            "Посмотреть результаты →",
                            fontSize = 13.sp,
                            color = OnPrimaryWhite.copy(alpha = 0.8f),
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
        }

        // Family shop banner
        item {
            Spacer(Modifier.height(10.dp))
            Surface(
                onClick = onShopClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SweetHomeSpacing.lg),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFD4956B),
                shadowElevation = 2.dp,
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(
                        "🛍",
                        fontSize = 60.sp,
                        modifier = Modifier.align(Alignment.CenterEnd),
                        color = Color.White.copy(alpha = 0.15f),
                    )
                    Column {
                        Text("Семейный магазин", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = OnPrimaryWhite)
                        Text(
                            "Трать баллы на награды!",
                            fontSize = 13.sp,
                            color = OnPrimaryWhite.copy(alpha = 0.85f),
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
        }

        // Activity section
        item {
            Spacer(Modifier.height(SweetHomeSpacing.lg))
            Text(
                "Активность сегодня",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = SweetHomeSpacing.lg),
            )
            Spacer(Modifier.height(SweetHomeSpacing.xs))
        }

        item {
            ActivityCard(
                emoji = "✅",
                text = "Нет недавней активности",
                time = "",
            )
        }

        // Lists section
        if (lists.isNotEmpty()) {
            item {
                Spacer(Modifier.height(SweetHomeSpacing.lg))
                Text(
                    "Списки семьи",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = SweetHomeSpacing.lg),
                )
                Spacer(Modifier.height(SweetHomeSpacing.xs))
            }
            items(lists, key = { it.id }) { list ->
                FamilyListCard(list = list, onClick = { onListClick(list.id) })
            }
        }

        // Chat button
        item {
            Spacer(Modifier.height(SweetHomeSpacing.lg))
            Surface(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SweetHomeSpacing.lg)
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                color = PrimaryGreen,
                shadowElevation = 4.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "💬  Открыть чат семьи",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnPrimaryWhite,
                    )
                }
            }
            Spacer(Modifier.height(SweetHomeSpacing.lg))
        }
    }
}

@Composable
private fun HeaderIconButton(emoji: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(36.dp),
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.15f),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(emoji, fontSize = 18.sp)
        }
    }
}

@Composable
private fun StatsBar(
    completedCount: Int,
    activeListsCount: Int,
    todayTasksCount: Int,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = SweetHomeSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatColumn(
                value = completedCount.toString(),
                line1 = "Выполнено",
                line2 = "за неделю",
                modifier = Modifier.weight(1f),
            )
            VerticalDivider()
            StatColumn(
                value = activeListsCount.toString(),
                line1 = "Активных",
                line2 = "списка",
                modifier = Modifier.weight(1f),
            )
            VerticalDivider()
            StatColumn(
                value = todayTasksCount.toString(),
                line1 = "Задач",
                line2 = "на сегодня",
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun StatColumn(
    value: String,
    line1: String,
    line2: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryGreen,
        )
        Text(
            text = line1,
            fontSize = 10.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = line2,
            fontSize = 10.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun VerticalDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(1.dp)
            .height(40.dp)
            .background(DividerColor),
    )
}

@Composable
private fun ActivityCard(
    emoji: String,
    text: String,
    time: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = SweetHomeSpacing.lg, vertical = SweetHomeSpacing.xxs),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = SweetHomeSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(18.dp),
                color = SurfaceVariantCream,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(emoji, fontSize = 18.sp)
                }
            }
            Spacer(Modifier.width(SweetHomeSpacing.sm))
            Text(
                text = text,
                fontSize = 13.sp,
                color = TextPrimary,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (time.isNotBlank()) {
                Text(
                    text = time,
                    fontSize = 11.sp,
                    color = Color(0xFFAAAAA0),
                )
            }
        }
    }
}

@Composable
private fun NavCard(
    emoji: String,
    label: String,
    bgColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(84.dp),
        shape = SweetHomeShapes.Card,
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(emoji, fontSize = 28.sp)
            Spacer(Modifier.height(SweetHomeSpacing.xs))
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
            )
        }
    }
}

// ─── Family list card ───

private fun listTypeToEmoji(type: String) = when (type) {
    "shopping" -> "🛒"
    "home_chores" -> "🏠"
    else -> "📋"
}

@Composable
private fun FamilyListCard(
    list: TodoList,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = SweetHomeSpacing.lg, vertical = SweetHomeSpacing.xxs)
            .clickable(onClick = onClick),
        shape = SweetHomeShapes.Card,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(SweetHomeSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = PrimaryGreenLight.copy(alpha = 0.3f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = list.icon?.takeIf { it.isNotBlank() } ?: listTypeToEmoji(list.type),
                        fontSize = 18.sp,
                    )
                }
            }
            Spacer(Modifier.width(SweetHomeSpacing.sm))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = list.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = list.type.let {
                        when (it) {
                            "shopping" -> "Покупки"
                            "home_chores" -> "Дела по дому"
                            else -> "Общий"
                        }
                    },
                    fontSize = 12.sp,
                    color = TextSecondary,
                )
            }
            Text(
                text = "›",
                fontSize = 20.sp,
                color = Color(0xFFBDBDBD),
            )
        }
    }
}

// ─── Create dialog ───

@Composable
private fun CreateFamilySpaceDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by remember { mutableStateOf("Наш дом") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Семейное пространство") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Название") },
                singleLine = true,
                enabled = !isLoading,
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name.ifBlank { "Наш дом" }) },
                enabled = !isLoading,
            ) { Text("Создать") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        },
    )
}
