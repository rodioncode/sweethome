package com.jetbrains.kmpapp.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetbrains.kmpapp.ui.LocalCozySpacing
import com.jetbrains.kmpapp.ui.components.EmptyHero

@Composable
fun OnboardingDoneScreen(
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
        Spacer(Modifier.weight(1f))

        EmptyHero(
            emoji = "🎉",
            decor = listOf("✨", "🌿", "🎈", "🍀"),
            size = 200.dp,
        )

        Spacer(Modifier.height(spacing.xxl + 4.dp))

        Text(
            text = "Готово!",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 32.sp,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(spacing.sm))

        val subtitle = buildSubtitle(state)
        Text(
            text = subtitle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.weight(1f))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(spacing.xs + 2.dp),
        ) {
            OnboardingPrimaryButton(
                label = "В приложение",
                onClick = { onIntent(OnboardingIntent.Finish) },
            )
        }

        Spacer(Modifier.height(spacing.xxl))
    }
}

private fun buildSubtitle(state: OnboardingState): String {
    val base = "Добро пожаловать в SweetHome"
    return when {
        state.path == OnboardingPath.FAMILY && state.familyName.isNotBlank() ->
            "$base.\nСемья «${state.familyName}» создана."
        state.path == OnboardingPath.GROUP -> "$base.\nГруппа готова — можешь пригласить участников."
        state.path == OnboardingPath.SOLO  -> "$base.\nТвой уютный уголок для дел готов."
        else -> base
    }
}
