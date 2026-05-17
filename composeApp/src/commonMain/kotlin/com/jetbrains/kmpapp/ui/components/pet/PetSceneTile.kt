package com.jetbrains.kmpapp.ui.components.pet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetbrains.kmpapp.ui.LocalCozyExtraColors
import com.jetbrains.kmpapp.ui.models.Pet
import com.jetbrains.kmpapp.ui.models.Species

@Composable
fun PetSceneTile(
    pet: Pet,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
) {
    val extras = LocalCozyExtraColors.current
    val sceneColor = when (pet.species) {
        Species.RACCOON, Species.BUNNY  -> extras.ochreSoft
        Species.FOX                     -> extras.coralSoft
        Species.CAT                     -> extras.lavenderSoft
        Species.HEDGIE, Species.PANDA   -> MaterialTheme.colorScheme.primaryContainer
    }
    val decor = when (pet.species) {
        Species.RACCOON -> listOf("🍓", "🍂", "✨", "🍯")
        Species.FOX     -> listOf("🍁", "🍄", "✨", "🌿")
        Species.CAT     -> listOf("☁️", "☀️", "✨", "🌿")
        Species.HEDGIE  -> listOf("🍎", "🍄", "🌿", "✨")
        Species.PANDA   -> listOf("🎋", "🏮", "✨", "🌿")
        Species.BUNNY   -> listOf("🥕", "🌷", "✨", "🌿")
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(sceneColor, extras.surfaceSoft),
                    radius = size.value * 0.8f
                )
            )
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
    ) {
        decor.take(4).forEachIndexed { i, e ->
            val positions = listOf(
                Alignment.TopStart, Alignment.TopEnd, Alignment.BottomStart, Alignment.BottomEnd
            )
            Box(
                modifier = Modifier.fillMaxSize().padding(size * 0.10f),
                contentAlignment = positions[i],
            ) {
                Text(text = e, fontSize = (size.value * 0.10f).sp, color = Color.Black.copy(alpha = 0.85f))
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .height(size * 0.18f)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, sceneColor.copy(alpha = 0.55f)),
                    )
                )
        )

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = pet.species.emoji, fontSize = (size.value * 0.55f).sp)
        }

        val moodSize = size * 0.22f
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
                .size(moodSize)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = pet.mood.emoji, fontSize = (size.value * 0.14f).sp)
        }
    }
}
