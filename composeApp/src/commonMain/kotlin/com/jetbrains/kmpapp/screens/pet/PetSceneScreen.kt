package com.jetbrains.kmpapp.screens.pet

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
import com.jetbrains.kmpapp.ui.LocalCozyExtraColors
import com.jetbrains.kmpapp.ui.LocalCozyShapes
import com.jetbrains.kmpapp.ui.components.CozyCard
import com.jetbrains.kmpapp.ui.components.CozyTopBar
import com.jetbrains.kmpapp.ui.components.pet.PetAvatar
import com.jetbrains.kmpapp.ui.components.pet.PetSceneTile
import com.jetbrains.kmpapp.ui.models.Pet

data class PetActivityEntry(
    val petName: String,
    val emoji: String,
    val text: String,
    val timeAgo: String,
)

@Composable
fun PetSceneScreen(
    guardianPet: Pet?,
    personalPets: List<Pet>,
    activityLog: List<PetActivityEntry>,
    onPetClick: (petId: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val extras = LocalCozyExtraColors.current
    val shapes = LocalCozyShapes.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        CozyTopBar(onBack = onBack, title = "Берлога")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
        ) {
            // Guardian scene
            if (guardianPet != null) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    PetSceneTile(pet = guardianPet, size = 300.dp)
                }
                Text(
                    text = guardianPet.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = "Хранитель семьи · ${guardianPet.species.displayName}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                )
            }

            Spacer(Modifier.height(24.dp))

            // Personal pets row
            Text(
                text = "ЛИЧНЫЕ ПИТОМЦЫ · ${personalPets.size}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 10.dp),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                personalPets.take(4).forEach { pet ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(shapes.chip)
                            .clickable { onPetClick(pet.id) }
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        PetAvatar(species = pet.species, size = 64.dp, accent = true)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = pet.name,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            text = "Ур. ${pet.level}",
                            fontSize = 10.sp,
                            color = extras.textTer,
                        )
                    }
                }
                repeat((4 - personalPets.size).coerceAtLeast(0)) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            Spacer(Modifier.height(20.dp))

            // Energy bar card
            CozyCard(modifier = Modifier.fillMaxWidth(), contentPadding = 14.dp) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⚡", fontSize = 20.sp)
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Энергия семьи",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            Text(
                                text = "12 задач сегодня",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            text = "78%",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = extras.ochre,
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(shapes.pill)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(0.78f)
                                .clip(shapes.pill)
                                .background(extras.ochre),
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Activity log
            Text(
                text = "ПОСЛЕДНИЕ СОБЫТИЯ",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 10.dp),
            )
            CozyCard(modifier = Modifier.fillMaxWidth(), contentPadding = 0.dp) {
                Column {
                    activityLog.forEachIndexed { i, entry ->
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(entry.emoji, fontSize = 18.sp)
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = entry.petName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                )
                                Text(
                                    text = entry.text,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 16.sp,
                                    modifier = Modifier.padding(top = 2.dp),
                                )
                            }
                            Text(
                                text = entry.timeAgo,
                                fontSize = 10.sp,
                                color = extras.textTer,
                            )
                        }
                        if (i < activityLog.lastIndex) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(MaterialTheme.colorScheme.outlineVariant),
                            )
                        }
                    }
                }
            }
        }
    }
}
