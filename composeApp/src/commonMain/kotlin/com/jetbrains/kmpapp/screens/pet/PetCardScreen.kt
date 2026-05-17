package com.jetbrains.kmpapp.screens.pet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import com.jetbrains.kmpapp.ui.LocalCozyShapes
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetbrains.kmpapp.ui.LocalCozyExtraColors
import com.jetbrains.kmpapp.ui.components.CozyCard
import com.jetbrains.kmpapp.ui.components.CozyTopBar
import com.jetbrains.kmpapp.ui.components.pet.PetSceneTile
import com.jetbrains.kmpapp.ui.models.Pet
import com.jetbrains.kmpapp.ui.models.Stage

@Composable
fun PetCardScreen(
    pet: Pet?,
    onBack: () -> Unit,
    onWardrobe: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val extras = LocalCozyExtraColors.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
    ) {
        CozyTopBar(title = pet?.name ?: "Питомец", onBack = onBack)

        if (pet != null) {
            // Scene
            Box(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                PetSceneTile(pet = pet, size = 220.dp)
            }

            Spacer(Modifier.height(20.dp))

            // Name + species + stage
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = pet.name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "${pet.species.displayName} · ${pet.stage.displayName} · ${pet.mood.displayName}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            Spacer(Modifier.height(24.dp))

            // Level progress
            CozyCard(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Уровень", fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground)
                        Spacer(Modifier.weight(1f))
                        Text("${pet.level}/12", fontSize = 13.sp, fontWeight = FontWeight.Bold,
                            color = extras.ochre)
                    }
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, LocalCozyShapes.current.pill)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(pet.level / 12f)
                                .background(extras.ochre, LocalCozyShapes.current.pill)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Milestones
            CozyCard(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Вехи роста", fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground)
                    Stage.entries.forEach { stage ->
                        val reached = pet.level >= stage.level
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stage.emoji,
                                fontSize = 18.sp,
                                modifier = Modifier.width(28.dp),
                            )
                            Text(
                                text = stage.displayName,
                                fontSize = 13.sp,
                                fontWeight = if (reached) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (reached) MaterialTheme.colorScheme.onBackground
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(Modifier.weight(1f))
                            Text(
                                text = "Ур. ${stage.level}",
                                fontSize = 11.sp,
                                color = extras.textTer,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Wardrobe button
            CozyCard(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                onClick = onWardrobe,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("👗", fontSize = 20.sp)
                    Spacer(Modifier.width(12.dp))
                    Text("Гардероб", fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground)
                    Spacer(Modifier.weight(1f))
                    Text("›", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
