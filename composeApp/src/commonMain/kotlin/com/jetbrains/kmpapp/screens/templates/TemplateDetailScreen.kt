package com.jetbrains.kmpapp.screens.templates

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import com.jetbrains.kmpapp.ui.DividerColor
import com.jetbrains.kmpapp.ui.PrimaryGreen
import com.jetbrains.kmpapp.ui.PrimaryGreenLight
import com.jetbrains.kmpapp.ui.SurfaceVariantCream
import com.jetbrains.kmpapp.ui.SurfaceWhite

private data class TemplateSection(val category: String, val items: List<String>)

private val templateSections = listOf(
    TemplateSection("🥛 Молочные", listOf("Молоко 2л", "Кефир 1%", "Масло сливочное")),
    TemplateSection("🥩 Мясо", listOf("Куриная грудка", "Говядина для супа")),
    TemplateSection("🥦 Овощи", listOf("Помидоры", "Огурцы", "Картофель")),
    TemplateSection("🧹 Бытовая химия", listOf("Средство для посуды", "Порошок стиральный")),
)

private val templateTags = listOf("🛒 Покупки", "Еженедельный", "Системный")

@Composable
fun TemplateDetailScreen(
    templateTitle: String = "Список покупок",
    templateIcon: String = "🛒",
    navigateBack: () -> Unit,
    navigateToLists: () -> Unit = {},
) {
    var used by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceVariantCream),
    ) {
        // Green hero header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(PrimaryGreen),
        ) {
            // Decorative circle
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .align(Alignment.TopEnd)
                    .background(Color.White.copy(alpha = 0.06f), CircleShape),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp, bottom = 28.dp),
            ) {
                // Top action bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Surface(
                        onClick = navigateBack,
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.15f),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("‹", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    Surface(
                        onClick = {},
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.15f),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("···", fontSize = 14.sp, color = Color.White)
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Icon + title centered
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Surface(
                        modifier = Modifier.size(72.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.15f),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(templateIcon, fontSize = 36.sp)
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(
                        templateTitle,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    Text(
                        "12 элементов · Системный",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(bottom = 32.dp),
        ) {
            // Description card
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = SurfaceWhite,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor),
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                        Text(
                            "Описание",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 6.dp),
                        )
                        Text(
                            "Готовый шаблон с разбивкой по категориям. Подходит для еженедельного планирования.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 21.sp,
                        )
                    }
                }
            }

            // Tags
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    templateTags.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = PrimaryGreenLight,
                        ) {
                            Text(
                                tag,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGreen,
                            )
                        }
                    }
                }
            }

            // Section header
            item {
                Text(
                    "Задачи в шаблоне",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp).padding(bottom = 12.dp),
                )
            }

            // Template sections
            items(templateSections.size) { sIndex ->
                val section = templateSections[sIndex]
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp)) {
                    Text(
                        section.category,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                    section.items.forEach { item ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = SurfaceWhite,
                            border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .background(SurfaceVariantCream, CircleShape)
                                        .then(
                                            Modifier.background(
                                                Color.Transparent,
                                                CircleShape,
                                            )
                                        ),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .background(Color.Transparent, CircleShape),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .background(SurfaceVariantCream, CircleShape),
                                        )
                                    }
                                }
                                Text(item, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }

            // Use template button
            item {
                Spacer(Modifier.height(8.dp))
                Surface(
                    onClick = {
                        used = true
                        navigateToLists()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = if (used) PrimaryGreenLight else PrimaryGreen,
                    shadowElevation = if (used) 0.dp else 4.dp,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            if (used) "✓ Создаём список..." else "Использовать шаблон",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }
}
