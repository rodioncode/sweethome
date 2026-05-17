package com.jetbrains.kmpapp.screens.pet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetbrains.kmpapp.ui.LocalCozyExtraColors
import com.jetbrains.kmpapp.ui.components.CozyTopBar
import com.jetbrains.kmpapp.ui.components.pet.PetAvatar
import com.jetbrains.kmpapp.ui.models.Species

@Composable
fun PetChooseScreen(
    onBack: () -> Unit,
    onConfirm: (species: Species, name: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var step by remember { mutableIntStateOf(0) }
    var selectedSpecies by remember { mutableStateOf<Species?>(null) }
    var petName by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp),
    ) {
        CozyTopBar(title = "Завести питомца", onBack = onBack)

        Spacer(Modifier.height(24.dp))

        when (step) {
            0 -> {
                Text(
                    "Выбери питомца",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Он будет расти вместе с тобой по мере выполнения задач",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(24.dp))

                val chunked = Species.entries.chunked(3)
                chunked.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        row.forEach { species ->
                            val selected = species == selectedSpecies
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable { selectedSpecies = species }
                                    .padding(8.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .then(
                                            if (selected) Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                            else Modifier
                                        ),
                                ) {
                                    PetAvatar(species = species, size = 72.dp)
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = species.displayName,
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                Spacer(Modifier.weight(1f))

                if (selectedSpecies != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable { step = 1 }
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Далее", color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
            1 -> {
                Text(
                    "Как назовём?",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(24.dp))

                selectedSpecies?.let { PetAvatar(species = it, size = 100.dp, accent = true) }

                Spacer(Modifier.height(24.dp))

                androidx.compose.material3.OutlinedTextField(
                    value = petName,
                    onValueChange = { petName = it },
                    placeholder = { Text("Имя питомца") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                Spacer(Modifier.weight(1f))

                if (petName.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable {
                                selectedSpecies?.let { onConfirm(it, petName) }
                            }
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Готово!", color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
