package com.jetbrains.kmpapp.screens.kid

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetbrains.kmpapp.ui.KidTheme
import com.jetbrains.kmpapp.ui.LocalKidColors
import com.jetbrains.kmpapp.ui.LocalKidTypography
import com.jetbrains.kmpapp.ui.components.kid.KidCard
import com.jetbrains.kmpapp.ui.components.kid.KidSpeechBubble
import com.jetbrains.kmpapp.ui.components.pet.PetAvatar
import com.jetbrains.kmpapp.ui.models.Species

data class KidStreakDay(
    val label: String,
    val isDone: Boolean,
)

data class KidStreakState(
    val streakCount: Int = 0,
    val days: List<KidStreakDay> = emptyList(),
    val petSpecies: Species = Species.RACCOON,
    val encouragement: String = "",
)

@Composable
fun KidStreakScreen(
    state: KidStreakState,
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
            Text(
                text = "Серия дней",
                style = type.heading,
                color = colors.ink,
            )

            Spacer(Modifier.height(32.dp))

            // Big streak number
            KidCard(tilt = -1f) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                ) {
                    Text(
                        text = "🔥",
                        fontSize = 48.sp,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "${state.streakCount}",
                        style = type.heading,
                        color = colors.ink,
                        fontSize = 56.sp,
                    )
                    Text(
                        text = "дней подряд",
                        style = type.caption,
                        color = colors.inkSec,
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Calendar dots
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                state.days.forEach { day ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 4.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(
                                    if (day.isDone) colors.grass
                                    else colors.inkSec.copy(alpha = 0.2f)
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (day.isDone) {
                                Text("✓", color = colors.cream, style = type.caption)
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = day.label,
                            style = type.caption,
                            color = colors.inkSec,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // Pet encouragement
            PetAvatar(species = state.petSpecies, size = 100.dp, accent = true)

            Spacer(Modifier.height(12.dp))

            if (state.encouragement.isNotEmpty()) {
                KidSpeechBubble {
                    Text(state.encouragement, style = type.body, color = colors.ink)
                }
            }

            Spacer(Modifier.weight(1f))
        }
    }
}
