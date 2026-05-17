package com.jetbrains.kmpapp.ui.components.pet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetbrains.kmpapp.ui.LocalCozyExtraColors
import com.jetbrains.kmpapp.ui.models.Pet
import com.jetbrains.kmpapp.ui.models.Species

@Composable
fun PetAvatar(
    pet: Pet,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    accent: Boolean = false,
) {
    PetAvatar(species = pet.species, modifier = modifier, size = size, accent = accent)
}

@Composable
fun PetAvatar(
    species: Species,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    accent: Boolean = false,
) {
    val extras = LocalCozyExtraColors.current
    val bg: Color = when (species) {
        Species.RACCOON, Species.BUNNY  -> extras.coralSoft
        Species.FOX                     -> extras.coralSoft
        Species.CAT                     -> extras.lavenderSoft
        Species.HEDGIE, Species.PANDA   -> extras.lavenderSoft
    }
    val border: Color = when (species) {
        Species.RACCOON, Species.BUNNY  -> extras.coral
        Species.FOX                     -> extras.coral
        Species.CAT                     -> extras.lavender
        Species.HEDGIE, Species.PANDA   -> extras.lavender
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(bg)
            .then(if (accent) Modifier.border(2.dp, border, CircleShape) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = species.emoji, fontSize = (size.value * 0.55f).sp)
    }
}
