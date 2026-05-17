package com.jetbrains.kmpapp.screens.kid

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.jetbrains.kmpapp.ui.components.kid.KidBigButton
import com.jetbrains.kmpapp.ui.components.kid.KidAccent
import com.jetbrains.kmpapp.ui.components.pet.PetAvatar
import com.jetbrains.kmpapp.ui.models.Species

data class KidDoneState(
    val earnedStars: Int = 1,
    val petSpecies: Species = Species.RACCOON,
)

@Composable
fun KidDoneScreen(
    state: KidDoneState,
    onNext: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    KidTheme {
        val colors = LocalKidColors.current
        val type = LocalKidTypography.current

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(colors.cream),
            contentAlignment = Alignment.Center,
        ) {
            // Confetti emojis scattered around
            Text(
                text = "🎉",
                fontSize = 40.sp,
                modifier = Modifier.align(Alignment.TopStart).padding(32.dp),
            )
            Text(
                text = "⭐",
                fontSize = 32.sp,
                modifier = Modifier.align(Alignment.TopEnd).padding(40.dp),
            )
            Text(
                text = "🎊",
                fontSize = 36.sp,
                modifier = Modifier.align(Alignment.BottomStart).padding(48.dp),
            )
            Text(
                text = "✨",
                fontSize = 28.sp,
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 60.dp),
            )
            Text(
                text = "🌟",
                fontSize = 34.sp,
                modifier = Modifier.align(Alignment.BottomEnd).padding(56.dp),
            )

            // Main content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp),
            ) {
                PetAvatar(species = state.petSpecies, size = 140.dp, accent = true)

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "Ура! +${state.earnedStars} ⭐",
                    style = type.heading,
                    color = colors.sun,
                    textAlign = TextAlign.Center,
                    fontSize = 32.sp,
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Отличная работа!",
                    style = type.body,
                    color = colors.ink,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(32.dp))

                KidBigButton(
                    onClick = onNext,
                    accent = KidAccent.SUN,
                    icon = "👉",
                ) {
                    Text("Дальше", style = type.button)
                }
            }
        }
    }
}
