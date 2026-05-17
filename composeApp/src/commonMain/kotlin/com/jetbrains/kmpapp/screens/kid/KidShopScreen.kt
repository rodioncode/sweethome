package com.jetbrains.kmpapp.screens.kid

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetbrains.kmpapp.ui.KidTheme
import com.jetbrains.kmpapp.ui.LocalKidColors
import com.jetbrains.kmpapp.ui.LocalKidTypography
import com.jetbrains.kmpapp.ui.components.kid.KidCard
import com.jetbrains.kmpapp.ui.components.kid.KidStarPill

data class KidShopItem(
    val id: String,
    val title: String,
    val emoji: String,
    val priceStars: Int,
    val category: String = "",
)

data class KidShopState(
    val stars: Int = 0,
    val items: List<KidShopItem> = emptyList(),
)

@Composable
fun KidShopScreen(
    state: KidShopState,
    onBuy: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    KidTheme {
        val colors = LocalKidColors.current
        val type = LocalKidTypography.current

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(colors.cream)
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            // Header with star balance
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Магазин",
                    style = type.heading,
                    color = colors.ink,
                )
                Spacer(Modifier.weight(1f))
                KidStarPill(count = state.stars, big = true)
            }

            Spacer(Modifier.height(20.dp))

            // Shop grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(state.items, key = { it.id }) { item ->
                    KidCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onBuy(item.id) }
                                .padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = item.emoji,
                                fontSize = 36.sp,
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = item.title,
                                style = type.body,
                                color = colors.ink,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(Modifier.height(4.dp))
                            KidStarPill(count = item.priceStars)
                        }
                    }
                }
            }
        }
    }
}
