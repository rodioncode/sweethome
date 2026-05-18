package com.jetbrains.kmpapp.screens.empty

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetbrains.kmpapp.ui.LocalCozyShapes
import com.jetbrains.kmpapp.ui.LocalCozySpacing
import com.jetbrains.kmpapp.ui.components.CozyChip
import com.jetbrains.kmpapp.ui.components.EmptyHero

@Composable
fun ListsEmptyScreen(
    onPrimaryAction: () -> Unit,
    onSecondaryAction: () -> Unit = {},
    onSuggestion: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val spacing = LocalCozySpacing.current
    val shapes = LocalCozyShapes.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = spacing.xxl)
            .padding(top = spacing.xxxl, bottom = spacing.xxxl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        EmptyHero(
            emoji = "📋",
            decor = listOf("🛒", "🏡", "✅", "🎁"),
            bgColor = MaterialTheme.colorScheme.primaryContainer,
        )
        Spacer(Modifier.height(spacing.xxl))
        Text(
            text = "Здесь пока пусто",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(spacing.xs))
        Text(
            text = "Создай первый список — покупки, дела, цели",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(spacing.xxl))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .clip(shapes.button)
                .background(MaterialTheme.colorScheme.primary)
                .clickable(onClick = onPrimaryAction)
                .padding(vertical = spacing.md),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Создать список",
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.height(spacing.lg))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.xs, Alignment.CenterHorizontally),
        ) {
            listOf("Покупки", "Дом", "Цели").forEach { label ->
                CozyChip(
                    label = label,
                    selected = false,
                    onClick = { onSuggestion(label) },
                )
            }
        }
    }
}
