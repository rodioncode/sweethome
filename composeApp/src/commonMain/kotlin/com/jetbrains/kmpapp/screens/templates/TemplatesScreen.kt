package com.jetbrains.kmpapp.screens.templates

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jetbrains.kmpapp.data.suggestions.ChoreTemplate
import com.jetbrains.kmpapp.ui.PrimaryGreen
import com.jetbrains.kmpapp.ui.PrimaryGreenLight
import org.koin.compose.viewmodel.koinViewModel

// Popular template card data for the 2-col grid (static, matching Figma)
private data class PopularTemplate(
    val emoji: String,
    val title: String,
    val subtitle: String,
    val taskCount: Int,
    val bgColor: Color,
)

private val popularTemplates = listOf(
    PopularTemplate("🛒", "Список покупок", "Продукты по категориям", 12, Color(0xFFE8F5E9)),
    PopularTemplate("🧹", "Уборка дома", "Уборка по комнатам", 8, Color(0xFFFFF8E1)),
    PopularTemplate("🧳", "Чемодан", "Вещи для отпуска", 20, Color(0xFFE3F2FD)),
    PopularTemplate("🎂", "День рождения", "Организация праздника", 15, Color(0xFFFCE4EC)),
)

private data class TemplateCategory(
    val emoji: String,
    val title: String,
    val count: Int,
)

private val allCategories = listOf(
    TemplateCategory("🏠", "Дом и быт", 14),
    TemplateCategory("✈️", "Путешествия", 8),
    TemplateCategory("📚", "Учёба", 6),
    TemplateCategory("🎉", "Праздники", 4),
)

private val filterChips = listOf(
    "🏠 Дом",
    "✈️ Путешествия",
    "📚 Учёба",
)

@Composable
fun TemplatesContent(
    contentPadding: PaddingValues = PaddingValues(),
    modifier: Modifier = Modifier,
) {
    val viewModel = koinViewModel<TemplatesViewModel>()
    val templates by viewModel.templates.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedChip by remember { mutableStateOf<Int?>(0) } // default first selected

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding),
    ) {
        if (isLoading && templates.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryGreen)
            }
            return@Column
        }

        LazyColumn(
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            // Search bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Поиск шаблонов...") },
                    singleLine = true,
                    leadingIcon = { Text("🔍", fontSize = 16.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                )
            }

            // Category filter chips
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(filterChips.size) { index ->
                        FilterChip(
                            selected = selectedChip == index,
                            onClick = { selectedChip = if (selectedChip == index) null else index },
                            label = { Text(filterChips[index]) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryGreen,
                                selectedLabelColor = Color.White,
                            ),
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // Popular templates section
            item {
                Text(
                    text = "Популярные шаблоны",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
                Spacer(Modifier.height(8.dp))
            }

            // 2-column grid of popular templates
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    for (row in popularTemplates.chunked(2)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            for (template in row) {
                                PopularTemplateCard(
                                    template = template,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            // Fill remaining space if odd number
                            if (row.size == 1) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // All categories section
            item {
                Spacer(Modifier.height(24.dp))
                Text(
                    text = "Все категории",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
                Spacer(Modifier.height(4.dp))
            }

            items(allCategories) { category ->
                Surface(
                    onClick = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = com.jetbrains.kmpapp.ui.SurfaceWhite,
                    border = androidx.compose.foundation.BorderStroke(1.dp, com.jetbrains.kmpapp.ui.DividerColor),
                    shadowElevation = 1.dp,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp, 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(com.jetbrains.kmpapp.ui.SurfaceVariantCream, RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(text = category.emoji, fontSize = 24.sp)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = category.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "${category.count} шаблонов",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = PrimaryGreen,
                        ) {
                            Text(
                                "Открыть",
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = com.jetbrains.kmpapp.ui.OnPrimaryWhite,
                            )
                        }
                    }
                }
            }

            // Dynamic templates from API (if loaded)
            if (templates.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = "Шаблоны дел по дому",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    )
                }
                items(templates, key = { it.id }) { template ->
                    ChoreTemplateCard(
                        template = template,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun PopularTemplateCard(
    template: PopularTemplate,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = template.bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
        ) {
            Text(text = template.emoji, fontSize = 28.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                text = template.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = template.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.7f),
            ) {
                Text(
                    text = "${template.taskCount} задач",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                )
            }
        }
    }
}

@Composable
private fun ChoreTemplateCard(
    template: ChoreTemplate,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = categoryEmoji(template.category), fontSize = 18.sp)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = template.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
                if (!template.description.isNullOrBlank()) {
                    Text(
                        text = template.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.secondaryContainer,
            ) {
                Text(
                    text = intervalLabel(template.intervalDays),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                )
            }
        }
    }
}

private fun categoryEmoji(category: String) = when (category) {
    "kitchen" -> "🍳"
    "bathroom" -> "🚿"
    "living_room" -> "🛋"
    "bedroom" -> "🛏"
    "outdoor" -> "🌿"
    "laundry" -> "👕"
    "shopping" -> "🛒"
    else -> "🏠"
}

private fun intervalLabel(days: Int) = when (days) {
    1 -> "Ежедневно"
    7 -> "Еженедельно"
    14 -> "2 нед."
    30 -> "Ежемесячно"
    else -> "${days}д."
}
