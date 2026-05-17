package com.jetbrains.kmpapp.screens.kid

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.jetbrains.kmpapp.ui.KidTheme
import com.jetbrains.kmpapp.ui.LocalKidColors
import com.jetbrains.kmpapp.ui.LocalKidTypography
import com.jetbrains.kmpapp.ui.components.kid.KidBigButton
import com.jetbrains.kmpapp.ui.components.kid.KidAccent
import com.jetbrains.kmpapp.ui.components.kid.KidCard
import com.jetbrains.kmpapp.ui.components.kid.KidSpeechBubble
import com.jetbrains.kmpapp.ui.components.pet.PetAvatar
import com.jetbrains.kmpapp.ui.models.Species

data class KidPetState(
    val petName: String = "",
    val species: Species = Species.RACCOON,
    val moodEmoji: String = "😊",
    val speech: String = "",
    val level: Int = 1,
    val levelProgress: Float = 0.5f,
    val accessories: List<String> = emptyList(),
)

@Composable
fun KidPetScreen(
    state: KidPetState,
    onPet: () -> Unit = {},
    onDressUp: () -> Unit = {},
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
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Pet name and mood
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = state.petName,
                    style = type.heading,
                    color = colors.ink,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = state.moodEmoji,
                    style = type.heading,
                )
            }

            Spacer(Modifier.height(20.dp))

            // Large pet avatar
            PetAvatar(species = state.species, size = 160.dp, accent = true)

            Spacer(Modifier.height(16.dp))

            // Speech bubble
            if (state.speech.isNotEmpty()) {
                KidSpeechBubble {
                    Text(state.speech, style = type.body, color = colors.ink)
                }
            }

            Spacer(Modifier.height(20.dp))

            // Level progress
            KidCard {
                Column {
                    Text(
                        text = "Уровень ${state.level}",
                        style = type.title,
                        color = colors.ink,
                    )
                    Spacer(Modifier.height(8.dp))
                    // Simple progress bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.inkSec.copy(alpha = 0.15f)),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(state.levelProgress)
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.grass),
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Accessories
            if (state.accessories.isNotEmpty()) {
                KidCard {
                    Column {
                        Text("Аксессуары", style = type.title, color = colors.ink)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = state.accessories.joinToString("  "),
                            style = type.body,
                            color = colors.ink,
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(Modifier.weight(1f)) {
                    KidBigButton(
                        onClick = onPet,
                        accent = KidAccent.CANDY,
                        icon = "🤗",
                    ) {
                        Text("Погладить", style = type.button)
                    }
                }
                Box(Modifier.weight(1f)) {
                    KidBigButton(
                        onClick = onDressUp,
                        accent = KidAccent.SKY,
                        icon = "👗",
                    ) {
                        Text("Нарядить", style = type.button)
                    }
                }
            }
        }
    }
}
