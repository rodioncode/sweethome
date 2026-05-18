package com.jetbrains.kmpapp.screens.sharing

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
import com.jetbrains.kmpapp.ui.components.CozyCard
import com.jetbrains.kmpapp.ui.components.CozyTopBar
import com.jetbrains.kmpapp.ui.components.EmptyHero
import com.jetbrains.kmpapp.ui.components.MetaRow

data class GiftItem(
    val id: String,
    val name: String,
    val emoji: String,
    val price: String?,
    val store: String?,
)

@Composable
fun BookGiftScreen(
    gift: GiftItem,
    onClaim: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalCozySpacing.current
    val shapes = LocalCozyShapes.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        CozyTopBar(title = "Взять подарок", onBack = onBack)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.xxl)
                .padding(top = spacing.md, bottom = spacing.xxxl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            EmptyHero(
                emoji = gift.emoji,
                size = 160.dp,
                decor = listOf("🎀", "✨", "💌", "🌸"),
                bgColor = MaterialTheme.colorScheme.primaryContainer,
            )
            Spacer(Modifier.height(spacing.xl))
            Text(
                text = gift.name,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(spacing.lg))

            CozyCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = 0.dp,
            ) {
                Column {
                    if (gift.price != null) {
                        MetaRow(
                            icon = "💰",
                            title = "Цена",
                            value = gift.price,
                        )
                    }
                    if (gift.store != null) {
                        MetaRow(
                            icon = "🏬",
                            title = "Магазин",
                            value = gift.store,
                        )
                    }
                }
            }
            Spacer(Modifier.height(spacing.xl))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .clip(shapes.button)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable(onClick = onClaim)
                    .padding(vertical = spacing.md),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Я заберу",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.height(spacing.md))
            Text(
                text = "Получатель не увидит кто",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}
