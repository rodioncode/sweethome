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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jetbrains.kmpapp.data.templates.ListTemplate
import com.jetbrains.kmpapp.data.templates.TemplateVisibility
import com.jetbrains.kmpapp.ui.SweetHomeShapes
import com.jetbrains.kmpapp.ui.SweetHomeSpacing
import com.jetbrains.kmpapp.ui.listColorForType
import com.jetbrains.kmpapp.ui.listEmojiForType
import org.koin.compose.viewmodel.koinViewModel

private data class ScopeFilter(val key: String?, val emoji: String, val label: String)

private val scopeFilters = listOf(
    ScopeFilter(null, "📚", "Все"),
    ScopeFilter("shopping", "🛒", "Покупки"),
    ScopeFilter("home_chores", "🏠", "Дом"),
    ScopeFilter("general_todos", "✅", "Задачи"),
    ScopeFilter("travel", "✈️", "Путешествия"),
    ScopeFilter("study", "📖", "Учёба"),
)

@Composable
fun TemplatesScreen(
    contentPadding: PaddingValues,
    navigateToTemplateDetail: (id: String, title: String, scope: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel = koinViewModel<TemplatesViewModel>()
    val tab by viewModel.tab.collectAsStateWithLifecycle()
    val scope by viewModel.scope.collectAsStateWithLifecycle()
    val templates by viewModel.templates.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(top = SweetHomeSpacing.sm),
    ) {
        Text(
            "Шаблоны",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = SweetHomeSpacing.lg),
        )
        Spacer(Modifier.height(SweetHomeSpacing.sm))
        TemplatesTabsRow(selected = tab, onSelect = viewModel::setTab)
        Spacer(Modifier.height(SweetHomeSpacing.xs))
        ScopeFiltersRow(selected = scope, onSelect = viewModel::setScope)
        Spacer(Modifier.height(SweetHomeSpacing.sm))

        if (isLoading && templates.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (templates.isEmpty()) {
            EmptyState(tab = tab, scope = scope)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    horizontal = SweetHomeSpacing.lg,
                    vertical = SweetHomeSpacing.xs,
                ),
                verticalArrangement = Arrangement.spacedBy(SweetHomeSpacing.sm),
            ) {
                items(templates, key = { it.id }) { template ->
                    TemplateCard(
                        template = template,
                        onClick = {
                            navigateToTemplateDetail(template.id, template.title, template.scope)
                        },
                        onToggleFavorite = { viewModel.toggleFavorite(template) },
                    )
                }
                item { Spacer(Modifier.height(SweetHomeSpacing.xxl)) }
            }
        }
    }
}

@Composable
private fun TemplatesTabsRow(
    selected: TemplatesTab,
    onSelect: (TemplatesTab) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SweetHomeSpacing.lg),
        horizontalArrangement = Arrangement.spacedBy(SweetHomeSpacing.xs),
    ) {
        TemplatesTab.entries.forEach { t ->
            TabPill(
                label = when (t) {
                    TemplatesTab.PUBLIC -> "Публичные"
                    TemplatesTab.MINE -> "Мои"
                    TemplatesTab.FAVORITES -> "Избранное"
                },
                selected = selected == t,
                onClick = { onSelect(t) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun TabPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = SweetHomeShapes.Chip,
        color = if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Box(
            modifier = Modifier.padding(vertical = SweetHomeSpacing.xs),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (selected) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ScopeFiltersRow(
    selected: String?,
    onSelect: (String?) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = SweetHomeSpacing.lg),
        horizontalArrangement = Arrangement.spacedBy(SweetHomeSpacing.xs),
    ) {
        items(scopeFilters, key = { it.key ?: "__all__" }) { filter ->
            val isSelected = selected == filter.key
            Surface(
                onClick = { onSelect(filter.key) },
                shape = SweetHomeShapes.Chip,
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface,
                border = if (isSelected) null
                else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = SweetHomeSpacing.sm, vertical = SweetHomeSpacing.xxs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(filter.emoji, fontSize = 13.sp)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = filter.label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun TemplateCard(
    template: ListTemplate,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val accent = listColorForType(template.scope, isDark = false)
    val emoji = listEmojiForType(template.scope)
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = SweetHomeShapes.Card,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SweetHomeSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(emoji, fontSize = 22.sp)
            }
            Spacer(Modifier.width(SweetHomeSpacing.sm))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = template.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Spacer(Modifier.width(SweetHomeSpacing.xs))
                    visibilityBadge(template)?.let { badge ->
                        VisibilityChip(label = badge.first, color = badge.second)
                    }
                }
                template.description?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (template.category.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "· ${template.category}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                }
            }
            Spacer(Modifier.width(SweetHomeSpacing.xs))
            Surface(
                onClick = onToggleFavorite,
                shape = CircleShape,
                color = Color.Transparent,
                modifier = Modifier.size(36.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (template.isFavorite) "★" else "☆",
                        fontSize = 20.sp,
                        color = if (template.isFavorite) Color(0xFFFFA726)
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun VisibilityChip(label: String, color: Color) {
    Surface(shape = SweetHomeShapes.Chip, color = color.copy(alpha = 0.14f)) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = color,
            modifier = Modifier.padding(horizontal = SweetHomeSpacing.xs, vertical = 2.dp),
        )
    }
}

private fun visibilityBadge(t: ListTemplate): Pair<String, Color>? = when {
    t.visibility == TemplateVisibility.PENDING -> "⏳ Pending" to Color(0xFFFFA726)
    t.userId != null && !t.isSystem && t.visibility == TemplateVisibility.PRIVATE ->
        "✦ Моё" to Color(0xFF5B7C5A)
    t.isSystem -> "Системный" to Color(0xFF7A7A7A)
    else -> null
}

@Composable
private fun EmptyState(tab: TemplatesTab, scope: String?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(SweetHomeSpacing.xxl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = when (tab) {
                TemplatesTab.PUBLIC -> "📚"
                TemplatesTab.MINE -> "✦"
                TemplatesTab.FAVORITES -> "★"
            },
            fontSize = 40.sp,
        )
        Spacer(Modifier.height(SweetHomeSpacing.sm))
        Text(
            text = when (tab) {
                TemplatesTab.PUBLIC -> if (scope != null) "Нет публичных шаблонов в этой категории"
                else "Публичных шаблонов пока нет"
                TemplatesTab.MINE -> "Вы ещё не создавали шаблоны"
                TemplatesTab.FAVORITES -> "Нет избранных шаблонов"
            },
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(SweetHomeSpacing.xs))
        Text(
            text = when (tab) {
                TemplatesTab.MINE -> "Откройте список → меню → «Сохранить как шаблон»"
                else -> "Зайдите позже или создайте свой"
            },
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
