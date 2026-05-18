package com.jetbrains.kmpapp.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetbrains.kmpapp.ui.LocalCozyExtraColors
import com.jetbrains.kmpapp.ui.LocalCozyShapes
import com.jetbrains.kmpapp.ui.LocalCozySpacing
import com.jetbrains.kmpapp.ui.components.CozyTopBar

@Composable
fun OnboardingPathScreen(
    state: OnboardingState,
    onIntent: (OnboardingIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalCozySpacing.current
    val extras = LocalCozyExtraColors.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.systemBars),
    ) {
        CozyTopBar(onBack = { onIntent(OnboardingIntent.Back) })

        Column(modifier = Modifier.padding(horizontal = spacing.xxl)) {
            OnboardingStepProgress(currentStep = 2, totalSteps = 5)
        }

        Spacer(Modifier.height(spacing.xl))
        Column(modifier = Modifier.padding(horizontal = spacing.xxl)) {
            Text(
                text = "Как будешь\nпользоваться?",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 32.sp,
            )
            Spacer(Modifier.height(spacing.xs))
            Text(
                text = "Это можно поменять потом, в настройках.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
            )
        }

        Spacer(Modifier.height(spacing.xxl))

        Column(
            modifier = Modifier.padding(horizontal = spacing.xxl),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            PathOption(
                emoji = "🌱",
                title = "Соло",
                description = "Личные задачи, личный питомец. Семью можно завести позже.",
                accent = MaterialTheme.colorScheme.primary,
                accentSoft = MaterialTheme.colorScheme.primaryContainer,
                selected = state.path == OnboardingPath.SOLO,
                onClick = { onIntent(OnboardingIntent.SelectPath(OnboardingPath.SOLO)) },
            )
            PathOption(
                emoji = "👨‍👩‍👧",
                title = "Семья",
                description = "Общие списки, чат, рейтинг и питомец на всех.",
                accent = extras.coral,
                accentSoft = extras.coralSoft,
                selected = state.path == OnboardingPath.FAMILY,
                onClick = { onIntent(OnboardingIntent.SelectPath(OnboardingPath.FAMILY)) },
            )
            PathOption(
                emoji = "🤝",
                title = "Группа",
                description = "Соседи, коворкинг, друзья — общие дела без семейного контекста.",
                accent = extras.lavender,
                accentSoft = extras.lavenderSoft,
                selected = state.path == OnboardingPath.GROUP,
                onClick = { onIntent(OnboardingIntent.SelectPath(OnboardingPath.GROUP)) },
            )
        }

        Spacer(Modifier.weight(1f))

        Column(
            modifier = Modifier.padding(horizontal = spacing.xxl, vertical = spacing.xxl),
        ) {
            OnboardingPrimaryButton(
                label = "Дальше →",
                onClick = { onIntent(OnboardingIntent.Next) },
            )
        }
    }
}

@Composable
private fun PathOption(
    emoji: String,
    title: String,
    description: String,
    accent: Color,
    accentSoft: Color,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val shape = LocalCozyShapes.current.card
    val spacing = LocalCozySpacing.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (selected) accentSoft else MaterialTheme.colorScheme.surface)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) accent else MaterialTheme.colorScheme.outlineVariant,
                shape = shape,
            )
            .clickable(onClick = onClick)
            .padding(spacing.lg + 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(LocalCozyShapes.current.chip)
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = emoji, fontSize = 30.sp)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = description,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                lineHeight = 17.sp,
            )
        }
        SelectionDot(selected = selected, accent = accent)
    }
}

@Composable
private fun SelectionDot(selected: Boolean, accent: Color) {
    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(if (selected) accent else Color.Transparent)
            .border(
                width = if (selected) 0.dp else 1.5.dp,
                color = if (selected) Color.Transparent else MaterialTheme.colorScheme.outline,
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Text(
                text = "✓",
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

