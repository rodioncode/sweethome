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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetbrains.kmpapp.ui.LocalCozySpacing
import com.jetbrains.kmpapp.ui.components.CozyCard
import com.jetbrains.kmpapp.ui.components.CozyTopBar
import com.jetbrains.kmpapp.ui.components.MetaRow

@Composable
fun OnboardingPermissionsScreen(
    state: OnboardingState,
    onIntent: (OnboardingIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalCozySpacing.current
    val scrollState = rememberScrollState()
    val p = state.permissions

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
                OnboardingStepProgress(currentStep = 4, totalSteps = 5)
            }

            Spacer(Modifier.height(spacing.xl))
            Column(modifier = Modifier.padding(horizontal = spacing.xxl)) {
                Text(
                    text = "Что разрешить?",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 32.sp,
                )
                Spacer(Modifier.height(spacing.xs))
                Text(
                    text = "Можно изменить позже. Мы спросим у системы только то, что включил.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                )
            }

            Spacer(Modifier.height(spacing.xl))

            Column(
                modifier = Modifier.padding(horizontal = spacing.xxl),
                verticalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                PermissionRow(
                    icon = "🔔",
                    title = "Push-уведомления",
                    granted = p.push,
                    onToggle = { onIntent(OnboardingIntent.TogglePermission("push", it)) },
                )
                PermissionRow(
                    icon = "📅",
                    title = "Системный календарь",
                    granted = p.calendar,
                    onToggle = { onIntent(OnboardingIntent.TogglePermission("calendar", it)) },
                )
                PermissionRow(
                    icon = "📍",
                    title = "Геолокация",
                    granted = p.location,
                    onToggle = { onIntent(OnboardingIntent.TogglePermission("location", it)) },
                )
                PermissionRow(
                    icon = "👥",
                    title = "Контакты",
                    granted = p.contacts,
                    onToggle = { onIntent(OnboardingIntent.TogglePermission("contacts", it)) },
                )
            }

            Spacer(Modifier.height(spacing.xxl))
        }

        Column(modifier = Modifier.padding(horizontal = spacing.xxl, vertical = spacing.xxl)) {
            OnboardingPrimaryButton(
                label = "Готово →",
                onClick = { onIntent(OnboardingIntent.Next) },
            )
        }
    }
}

@Composable
private fun PermissionRow(
    icon: String,
    title: String,
    granted: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    CozyCard(
        bordered = true,
        contentPadding = 0.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        MetaRow(
            icon = icon,
            title = title,
            valueAdornment = {
                Switch(
                    checked = granted,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.surface,
                        uncheckedTrackColor = MaterialTheme.colorScheme.outline,
                        uncheckedBorderColor = MaterialTheme.colorScheme.outline,
                    ),
                )
            },
        )
    }
}

