package com.jetbrains.kmpapp.screens.pet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.jetbrains.kmpapp.ui.LocalCozyExtraColors
import com.jetbrains.kmpapp.ui.LocalCozyShapes
import com.jetbrains.kmpapp.ui.components.CozyCard
import com.jetbrains.kmpapp.ui.components.CozyTopBar
import com.jetbrains.kmpapp.ui.components.pet.PetAvatar
import com.jetbrains.kmpapp.ui.models.Pet

data class PetAccessoryItem(
    val id: String,
    val emoji: String,
    val name: String,
    val slot: String,
    val unlocked: Boolean,
    val equipped: Boolean,
    val price: Int,
    val reason: String?,
)

enum class WardrobeTab(val id: String, val label: String) {
    WARDROBE("wardrobe", "Гардероб"),
    STAGES  ("stages",   "Эволюции"),
    SCENES  ("scenes",   "Локации"),
}

@Composable
fun PetWardrobeScreen(
    pet: Pet,
    accessories: List<PetAccessoryItem>,
    onEquip: (accessoryId: String) -> Unit,
    onBuy: (accessoryId: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val extras = LocalCozyExtraColors.current
    val shapes = LocalCozyShapes.current
    var tab by remember { mutableStateOf(WardrobeTab.WARDROBE) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        CozyTopBar(onBack = onBack, title = "Альбом ${pet.name}")

        // Mini header
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PetAvatar(species = pet.species, size = 56.dp, accent = true)
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${pet.name} · ${pet.stage.displayName}",
                    fontSize = 16.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                val unlocked = accessories.count { it.unlocked }
                Text(
                    text = "Открыто $unlocked из ${accessories.size} аксессуаров",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }

        // Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, shapes.chip)
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            WardrobeTab.entries.forEach { t ->
                val sel = t == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(shapes.button)
                        .background(if (sel) MaterialTheme.colorScheme.surface else androidx.compose.ui.graphics.Color.Transparent)
                        .clickable { tab = t }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = t.label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (sel) MaterialTheme.colorScheme.onBackground
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        if (tab == WardrobeTab.WARDROBE) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(accessories) { acc ->
                    AccessoryGridItem(
                        acc = acc,
                        onClick = {
                            if (!acc.unlocked) onBuy(acc.id) else onEquip(acc.id)
                        },
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (tab == WardrobeTab.STAGES) "Все стадии в карточке питомца" else "Локации — coming soon",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun AccessoryGridItem(
    acc: PetAccessoryItem,
    onClick: () -> Unit,
) {
    val extras = LocalCozyExtraColors.current
    val shapes = LocalCozyShapes.current
    val borderColor =
        if (acc.equipped) extras.ochre
        else if (acc.unlocked) MaterialTheme.colorScheme.outlineVariant
        else MaterialTheme.colorScheme.outlineVariant
    val bg = if (acc.equipped) extras.ochreSoft else MaterialTheme.colorScheme.surface

    Column(
        modifier = Modifier
            .clip(shapes.cardLarge)
            .background(bg)
            .border(
                width = if (acc.equipped) 1.5.dp else 1.dp,
                color = borderColor,
                shape = shapes.cardLarge,
            )
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = if (acc.unlocked) acc.emoji else "🔒",
            fontSize = 32.sp,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = acc.name,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        if (!acc.unlocked) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${acc.price} ⭐",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = extras.ochre,
            )
        } else if (acc.equipped) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = "НАДЕТО",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = extras.ochre,
            )
        }
    }
}
