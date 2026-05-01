package com.jetbrains.kmpapp.screens.home

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jetbrains.kmpapp.ui.ListColorAmber
import com.jetbrains.kmpapp.ui.ListColorCoral
import com.jetbrains.kmpapp.ui.ListColorMint
import com.jetbrains.kmpapp.ui.OnPrimaryWhite
import com.jetbrains.kmpapp.ui.PrimaryGreen
import com.jetbrains.kmpapp.ui.SurfaceVariantCream
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
    modifier: Modifier = Modifier,
) {
    val viewModel = koinViewModel<HomeViewModel>()
    val userId by viewModel.userId.collectAsStateWithLifecycle()
    val displayName by viewModel.displayName.collectAsStateWithLifecycle()
    val recentLists by viewModel.recentLists.collectAsStateWithLifecycle()

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
                    color = PrimaryGreen,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        val initial = displayName?.firstOrNull()?.uppercase()
                            ?: userId?.firstOrNull()?.uppercase() ?: "?"
                        Text(
                            text = initial,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OnPrimaryWhite,
                        )
                    }
                }
            }
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
                color = PrimaryGreen,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .padding(horizontal = SweetHomeSpacing.md),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(
                        text = "\uD83C\uDFE0 Семья Ивановых: 3 новых задачи",
                        fontSize = 13.sp,
                        color = OnPrimaryWhite,
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
                        emoji = "\uD83D\uDCDD",
                        label = "Новый список",
                        backgroundColor = SurfaceVariantCream,
                        onClick = onCreateList,
                        modifier = Modifier.weight(1f),
                    )
                    QuickActionCard(
                        emoji = "\uD83C\uDFE0",
                        label = "Мой дом",
                        backgroundColor = Color(0xFFE8F3E8),
                        onClick = onNavigateToHome,
                        modifier = Modifier.weight(1f),
                    )
                    QuickActionCard(
                        emoji = "\uD83D\uDC65",
                        label = "Группа",
                        backgroundColor = Color(0xFFFFF0E6),
                        onClick = onNavigateToGroups,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
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
                    text = "Все \u2192",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = PrimaryGreen,
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
                    colors = CardDefaults.cardColors(containerColor = SurfaceVariantCream),
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
                val color = list.color?.toComposeColor() ?: listColorForType(list.type)
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
private fun QuickActionCard(
    emoji: String,
    label: String,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
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
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

private fun greeting(): String {
    val hour = try {
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour
    } catch (e: Exception) {
        12
    }
    return when {
        hour < 6 -> "Доброй ночи \uD83C\uDF19"
        hour < 12 -> "Доброе утро \u2600\uFE0F"
        hour < 18 -> "Добрый день \uD83D\uDC4B"
        else -> "Добрый вечер \uD83C\uDF06"
    }
}
