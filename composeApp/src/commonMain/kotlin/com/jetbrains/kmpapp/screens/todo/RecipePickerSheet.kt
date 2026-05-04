package com.jetbrains.kmpapp.screens.todo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetbrains.kmpapp.ui.SweetHomeShapes
import com.jetbrains.kmpapp.ui.SweetHomeSpacing

/**
 * Простая модель рецепта (G-10). Реальный репозиторий рецептов появится позже —
 * пока используем seed.
 */
data class Recipe(
    val id: String,
    val title: String,
    val emoji: String,
    val cuisine: String,
    val durationLabel: String,
    val difficultyLabel: String,
    val ingredients: List<String>,
)

private val SEED_RECIPES = listOf(
    Recipe("r1", "Паста Карбонара", "🍝", "Итальянская", "30 мин", "Легко", listOf("Спагетти", "Бекон", "Яйца", "Пармезан", "Перец")),
    Recipe("r2", "Том Ям", "🍲", "Азиатская", "45 мин", "Средне", listOf("Креветки", "Лемонграсс", "Кокосовое молоко", "Рис")),
    Recipe("r3", "Цезарь с курицей", "🥗", "Салаты", "20 мин", "Легко", listOf("Курица", "Салат-ромэн", "Гренки", "Пармезан")),
    Recipe("r4", "Тирамису", "🍰", "Десерты", "40 мин", "Средне", listOf("Маскарпоне", "Кофе", "Печенье савоярди", "Какао")),
    Recipe("r5", "Боул с лососем", "🍱", "Азиатская", "25 мин", "Легко", listOf("Лосось", "Рис", "Авокадо", "Огурец", "Соевый соус")),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipePickerSheet(
    onPicked: (Recipe) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var query by remember { mutableStateOf("") }
    var selectedCuisine by remember { mutableStateOf("Все") }
    var selectedRecipeId by remember { mutableStateOf<String?>(null) }

    val cuisines = listOf("Все") + SEED_RECIPES.map { it.cuisine }.distinct()
    val filtered = SEED_RECIPES.filter { recipe ->
        (selectedCuisine == "Все" || recipe.cuisine == selectedCuisine) &&
            (query.isBlank() || recipe.title.contains(query, ignoreCase = true))
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = SweetHomeShapes.BottomSheet,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = SweetHomeSpacing.bottomSheetPaddingH,
                vertical = SweetHomeSpacing.xl,
            ),
            verticalArrangement = Arrangement.spacedBy(SweetHomeSpacing.xl),
        ) {
            Text(
                "Выбрать рецепт",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Найти рецепт", fontSize = 14.sp) },
                leadingIcon = {
                    Icon(Icons.Outlined.Search, contentDescription = null, modifier = Modifier.size(20.dp))
                },
                shape = SweetHomeShapes.Medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                ),
                singleLine = true,
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(SweetHomeSpacing.sm)) {
                items(cuisines) { c ->
                    FilterChip(
                        selected = c == selectedCuisine,
                        onClick = { selectedCuisine = c },
                        label = { Text(c, fontSize = 13.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                    )
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(SweetHomeSpacing.sm),
                modifier = Modifier.height(360.dp),
            ) {
                items(filtered, key = { it.id }) { recipe ->
                    RecipeRow(
                        recipe = recipe,
                        selected = selectedRecipeId == recipe.id,
                        onClick = { selectedRecipeId = recipe.id },
                    )
                }
            }

            Button(
                onClick = {
                    selectedRecipeId?.let { id ->
                        SEED_RECIPES.firstOrNull { it.id == id }?.let(onPicked)
                    }
                },
                enabled = selectedRecipeId != null,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = SweetHomeShapes.Button,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) { Text("Добавить в список", fontWeight = FontWeight.Bold) }

            Spacer(Modifier.height(SweetHomeSpacing.xl))
        }
    }
}

@Composable
private fun RecipeRow(
    recipe: Recipe,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = SweetHomeShapes.Card,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(
            modifier = Modifier.padding(SweetHomeSpacing.base),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(SweetHomeSpacing.avatarHero)
                    .clip(SweetHomeShapes.Medium)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Text(recipe.emoji, fontSize = 32.sp)
            }
            Spacer(Modifier.width(SweetHomeSpacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    recipe.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(SweetHomeSpacing.xxs))
                Row(horizontalArrangement = Arrangement.spacedBy(SweetHomeSpacing.sm)) {
                    MetaChip("⏱ ${recipe.durationLabel}")
                    MetaChip("👨‍🍳 ${recipe.difficultyLabel}")
                }
                Spacer(Modifier.height(SweetHomeSpacing.xxs))
                Text(
                    "${recipe.ingredients.size} ингредиентов",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary),
            )
        }
    }
}

@Composable
private fun MetaChip(text: String) {
    Surface(
        shape = SweetHomeShapes.Pill,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = SweetHomeSpacing.sm, vertical = 2.dp),
        )
    }
}

