package com.jetbrains.kmpapp.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.jetbrains.kmpapp.ui.LocalCozyShapes
import com.jetbrains.kmpapp.ui.LocalCozySpacing
import com.jetbrains.kmpapp.ui.components.CozyTopBar

private val FAMILY_EMOJIS = listOf("🏡", "🌿", "🌸", "☕", "🐾", "🍀", "✨", "🍯")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingFamilySetupScreen(
    state: OnboardingState,
    onIntent: (OnboardingIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalCozySpacing.current
    val shapes = LocalCozyShapes.current

    var selectedEmojiIndex by remember { mutableStateOf(0) }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.systemBars),
    ) {
        CozyTopBar(onBack = { onIntent(OnboardingIntent.Back) })

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState),
        ) {
            Column(modifier = Modifier.padding(horizontal = spacing.xxl)) {
                OnboardingStepProgress(currentStep = 3, totalSteps = 5)
            }

            Spacer(Modifier.height(spacing.xl))
            Column(modifier = Modifier.padding(horizontal = spacing.xxl)) {
                Text(
                    text = "Твоя семья",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 32.sp,
                )
                Spacer(Modifier.height(spacing.xs))
                Text(
                    text = "Дай ей имя и выбери эмодзи — это увидят все, кого пригласишь.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                )
            }

            Spacer(Modifier.height(spacing.xxl))

            // Family name input
            Column(modifier = Modifier.padding(horizontal = spacing.xxl)) {
                OutlinedTextField(
                    value = state.familyName,
                    onValueChange = { onIntent(OnboardingIntent.SetFamilyName(it)) },
                    label = { Text("Имя семьи") },
                    placeholder = { Text("Например, Сидоровы") },
                    singleLine = true,
                    shape = shapes.button,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )

                Spacer(Modifier.height(spacing.xl))

                Text(
                    text = "ЭМОДЗИ",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                )
                Spacer(Modifier.height(spacing.sm))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                    verticalArrangement = Arrangement.spacedBy(spacing.xs),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    FAMILY_EMOJIS.forEachIndexed { index, emoji ->
                        EmojiTile(
                            emoji = emoji,
                            selected = index == selectedEmojiIndex,
                            onClick = { selectedEmojiIndex = index },
                        )
                    }
                }

                Spacer(Modifier.height(spacing.xl))

                // Invite hint card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(shapes.button)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(spacing.sm),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(spacing.xs + 2.dp),
                ) {
                    Text(text = "👯", fontSize = 18.sp)
                    Text(
                        text = "На следующем шаге пригласишь родственников — по коду, ссылке или QR.",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                    )
                }
            }

            Spacer(Modifier.height(spacing.xxl))
        }

        Column(modifier = Modifier.padding(horizontal = spacing.xxl, vertical = spacing.xxl)) {
            OnboardingPrimaryButton(
                label = "Создать семью →",
                onClick = { onIntent(OnboardingIntent.Next) },
            )
        }
    }
}

@Composable
private fun EmojiTile(
    emoji: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val shape = LocalCozyShapes.current.chip
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(shape)
            .background(
                if (selected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface
            )
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outlineVariant,
                shape = shape,
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = emoji, fontSize = 22.sp)
    }
}
