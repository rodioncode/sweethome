package com.jetbrains.kmpapp.screens.family

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetbrains.kmpapp.ui.DividerColor
import com.jetbrains.kmpapp.ui.PrimaryGreen
import com.jetbrains.kmpapp.ui.SurfaceVariantCream
import com.jetbrains.kmpapp.ui.SurfaceWhite

private data class ShopItem(
    val id: String,
    val icon: String,
    val title: String,
    val desc: String,
    val points: Int,
)

private val shopItems = listOf(
    ShopItem("s1", "🍦", "Мороженое", "Мама купит любое мороженое", 30),
    ShopItem("s2", "🎮", "Час игры", "Дополнительный час на приставке", 50),
    ShopItem("s3", "🍕", "Пицца на ужин", "Выбираешь начинку ты!", 80),
    ShopItem("s4", "🎬", "Поход в кино", "Фильм на твой выбор", 120),
    ShopItem("s5", "🛒", "Выходной от уборки", "Один день без домашних дел", 60),
    ShopItem("s6", "🎁", "Подарок", "До 500 ₽ на любой подарок", 200),
)

private val earnRules = listOf(
    "Выполнить задачу" to "+5 ⭐",
    "Выполнить дело по дому" to "+10 ⭐",
    "Стрик 3 дня" to "+15 ⭐",
    "Выполнить всё за день" to "+25 ⭐",
)

private val shopGradient = Brush.linearGradient(listOf(Color(0xFFE8A87C), Color(0xFFD4956B)))

@Composable
fun FamilyShopScreen(
    navigateBack: () -> Unit,
) {
    val myPoints = 142
    var bought by remember { mutableStateOf(emptySet<String>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceVariantCream),
    ) {
        // Orange gradient header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(shopGradient),
        ) {
            // Decorative emoji
            Text(
                "🛍",
                fontSize = 80.sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 0.dp, end = 0.dp),
                color = Color.White.copy(alpha = 0.15f),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 14.dp, bottom = 20.dp),
            ) {
                // Back + title row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(bottom = 14.dp),
                ) {
                    Surface(
                        onClick = navigateBack,
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("‹", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    Text(
                        "Семейный магазин",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }

                // Points + bought count
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text("Мои баллы", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                        Text("⭐ $myPoints", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White.copy(alpha = 0.2f),
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text("Куплено", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                            Text("${bought.size}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(bottom = 32.dp),
        ) {
            // Description
            item {
                Text(
                    "Выполняй задачи — получай баллы — трать их на приятные награды 🎉",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 19.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                )
            }

            // Shop grid (2 columns)
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    shopItems.chunked(2).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            row.forEach { item ->
                                ShopItemCard(
                                    item = item,
                                    myPoints = myPoints,
                                    isBought = item.id in bought,
                                    onBuy = { bought = bought + item.id },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            if (row.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }

            // How to earn
            item {
                Spacer(Modifier.height(20.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = SurfaceWhite,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Как зарабатывать баллы?",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 12.dp),
                        )
                        earnRules.forEachIndexed { index, (action, pts) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(action, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text(pts, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                            }
                            if (index < earnRules.lastIndex) {
                                HorizontalDivider(color = DividerColor)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShopItemCard(
    item: ShopItem,
    myPoints: Int,
    isBought: Boolean,
    onBuy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val canBuy = myPoints >= item.points && !isBought

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = SurfaceWhite,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isBought) PrimaryGreen else DividerColor,
        ),
    ) {
        Box {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp)) {
                Text(item.icon, fontSize = 32.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    item.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    item.desc,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp,
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "⭐ ${item.points}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (canBuy) PrimaryGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Surface(
                        onClick = { if (canBuy) onBuy() },
                        shape = RoundedCornerShape(10.dp),
                        color = when {
                            isBought -> PrimaryGreen.copy(alpha = 0.12f)
                            canBuy -> PrimaryGreen
                            else -> DividerColor
                        },
                    ) {
                        Text(
                            if (isBought) "Готово" else "Купить",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                isBought -> PrimaryGreen
                                canBuy -> Color.White
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                }
            }

            if (isBought) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = PrimaryGreen,
                ) {
                    Text(
                        "✓ Куплено",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
            }
        }
    }
}
