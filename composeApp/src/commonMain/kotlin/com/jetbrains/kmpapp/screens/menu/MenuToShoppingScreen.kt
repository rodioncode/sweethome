package com.jetbrains.kmpapp.screens.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetbrains.kmpapp.ui.LocalCozyExtraColors
import com.jetbrains.kmpapp.ui.LocalCozyShapes
import com.jetbrains.kmpapp.ui.LocalCozySpacing
import com.jetbrains.kmpapp.ui.components.CozyTopBar

data class IngredientLine(
    val name: String,
    val amount: String,
    val unit: String,
    val sources: List<String>,
    val checked: Boolean = false,
)

data class MenuToShoppingState(
    val ingredients: List<IngredientLine> = emptyList(),
    val isGenerating: Boolean = false,
)

sealed interface MenuToShoppingIntent {
    data class ToggleItem(val index: Int) : MenuToShoppingIntent
    data object CreateList : MenuToShoppingIntent
    data object Back : MenuToShoppingIntent
}

@Composable
fun MenuToShoppingScreen(
    state: MenuToShoppingState,
    onIntent: (MenuToShoppingIntent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val extras = LocalCozyExtraColors.current
    val spacing = LocalCozySpacing.current
    val shapes = LocalCozyShapes.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        CozyTopBar(
            onBack = {
                onIntent(MenuToShoppingIntent.Back)
                onBack()
            },
            title = "Список покупок",
        )

        if (state.isGenerating) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(spacing.md))
                    Text(
                        text = "Собираем ингредиенты...",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = spacing.xxl)
                    .padding(top = spacing.md)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(spacing.xs),
            ) {
                state.ingredients.forEachIndexed { index, item ->
                    IngredientRow(
                        item = item,
                        onToggle = { onIntent(MenuToShoppingIntent.ToggleItem(index)) },
                    )
                }
                Spacer(Modifier.height(80.dp))
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant)
                .padding(horizontal = spacing.xxl, vertical = spacing.sm),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(shapes.button)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable { onIntent(MenuToShoppingIntent.CreateList) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Создать список покупок",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

@Composable
private fun IngredientRow(
    item: IngredientLine,
    onToggle: () -> Unit,
) {
    val extras = LocalCozyExtraColors.current
    val spacing = LocalCozySpacing.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = item.checked,
            onCheckedChange = { onToggle() },
        )
        Spacer(Modifier.width(spacing.xs))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = if (item.checked) extras.textTer else MaterialTheme.colorScheme.onBackground,
                textDecoration = if (item.checked) TextDecoration.LineThrough else TextDecoration.None,
            )
            if (item.sources.isNotEmpty()) {
                Text(
                    text = item.sources.joinToString(", "),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Text(
            text = "${item.amount} ${item.unit}",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (item.checked) extras.textTer else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
