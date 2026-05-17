package com.jetbrains.kmpapp.screens.kid

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetbrains.kmpapp.ui.KidTheme
import com.jetbrains.kmpapp.ui.LocalKidColors
import com.jetbrains.kmpapp.ui.LocalKidTypography
import com.jetbrains.kmpapp.ui.components.kid.KidBigButton
import com.jetbrains.kmpapp.ui.components.kid.KidAccent
import com.jetbrains.kmpapp.ui.components.kid.KidCard
import com.jetbrains.kmpapp.ui.components.kid.KidSpeechBubble
import com.jetbrains.kmpapp.ui.components.kid.KidStarPill
import com.jetbrains.kmpapp.ui.components.pet.PetAvatar
import com.jetbrains.kmpapp.ui.models.Pet
import com.jetbrains.kmpapp.ui.models.Species

data class KidHomeState(
    val childName: String = "",
    val stars: Int = 0,
    val pet: Pet? = null,
    val petSpeech: String = "",
    val pendingTasksCount: Int = 0,
)

@Composable
fun KidHomeScreen(
    state: KidHomeState,
    onTasksTap: () -> Unit = {},
    onShopTap: () -> Unit = {},
    onPetTap: () -> Unit = {},
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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Привет, ${state.childName}!",
                    style = type.heading,
                    color = colors.ink,
                )
                Spacer(Modifier.weight(1f))
                KidStarPill(count = state.stars, big = true, onClick = onShopTap)
            }

            Spacer(Modifier.height(24.dp))

            // Pet section
            state.pet?.let { pet ->
                PetAvatar(species = pet.species, size = 120.dp, accent = true)
                Spacer(Modifier.height(12.dp))
                if (state.petSpeech.isNotEmpty()) {
                    KidSpeechBubble {
                        Text(state.petSpeech, style = type.body, color = colors.ink)
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // Tasks card
            KidCard(tilt = -1f) {
                Column {
                    Text(
                        text = "Задания на сегодня",
                        style = type.title,
                        color = colors.ink,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${state.pendingTasksCount} осталось",
                        style = type.caption,
                        color = colors.inkSec,
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            KidBigButton(
                onClick = onTasksTap,
                accent = KidAccent.GRASS,
                icon = "✅",
            ) {
                Text("Показать задания", style = type.button)
            }

            Spacer(Modifier.height(12.dp))

            KidBigButton(
                onClick = onShopTap,
                accent = KidAccent.SUN,
                icon = "🛍️",
            ) {
                Text("Магазин", style = type.button)
            }
        }
    }
}
