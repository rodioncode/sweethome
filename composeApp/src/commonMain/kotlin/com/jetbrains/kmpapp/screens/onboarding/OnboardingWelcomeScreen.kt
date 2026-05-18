package com.jetbrains.kmpapp.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.jetbrains.kmpapp.ui.LocalCozyShapes
import com.jetbrains.kmpapp.ui.LocalCozySpacing
import com.jetbrains.kmpapp.ui.components.EmptyHero

@Composable
fun OnboardingWelcomeScreen(
    state: OnboardingState,
    onIntent: (OnboardingIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalCozySpacing.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(horizontal = spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Brand + skip row
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = spacing.xxl),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "SWEETHOME",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "Войти",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { onIntent(OnboardingIntent.Finish) },
            )
        }

        Spacer(Modifier.height(48.dp))

        EmptyHero(emoji = "🏡", decor = listOf("🌿", "✨", "☁️", "🍃"), size = 220.dp)

        Spacer(Modifier.weight(1f))

        Text(
            text = "Уютный дом\nдля общих дел",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 34.sp,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(spacing.sm))
        Text(
            text = "Дом, который растёт вместе с тобой. Списки, задачи, рецепты и подарки — для всей семьи в одном месте.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.weight(1f))

        OnboardingProgressDots(activeIndex = 0, totalDots = 4)
        Spacer(Modifier.height(spacing.lg))

        OnboardingPrimaryButton(
            label = "Начать →",
            onClick = { onIntent(OnboardingIntent.Next) },
        )

        Spacer(Modifier.height(spacing.xxl))
    }
}

// ─── Shared internals used by every onboarding step ───────────────────────

@Composable
internal fun OnboardingPrimaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clip(LocalCozyShapes.current.button)
            .background(MaterialTheme.colorScheme.primary)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
internal fun OnboardingSecondaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp)
            .clip(LocalCozyShapes.current.button)
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
internal fun OnboardingProgressDots(activeIndex: Int, totalDots: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(totalDots) { i ->
            val isActive = i == activeIndex
            Box(
                modifier = Modifier
                    .width(if (isActive) 22.dp else 6.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        if (isActive) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant
                    ),
            )
        }
    }
}

@Composable
internal fun OnboardingStepProgress(currentStep: Int, totalSteps: Int) {
    val spacing = LocalCozySpacing.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            repeat(totalSteps) { i ->
                val filled = i < currentStep
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            if (filled) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant
                        ),
                )
            }
        }
        Spacer(Modifier.height(spacing.xxs + 2.dp))
        Text(
            text = "ШАГ $currentStep ИЗ $totalSteps",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp,
        )
    }
}

