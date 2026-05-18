package com.jetbrains.kmpapp.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

// ─────────────────────────────────────────────────────────────────────
// Onboarding state / intents — drives the 5-step flow.
// ─────────────────────────────────────────────────────────────────────

data class OnboardingState(
    val step: Int = 1,
    val path: OnboardingPath? = null,
    val familyName: String = "",
    val permissions: OnboardingPermissions = OnboardingPermissions(),
)

enum class OnboardingPath { SOLO, FAMILY, GROUP }

data class OnboardingPermissions(
    val push: Boolean = false,
    val calendar: Boolean = false,
    val location: Boolean = false,
    val contacts: Boolean = false,
)

sealed interface OnboardingIntent {
    data object Next : OnboardingIntent
    data object Back : OnboardingIntent
    data class SelectPath(val path: OnboardingPath) : OnboardingIntent
    data class SetFamilyName(val name: String) : OnboardingIntent
    data class TogglePermission(val key: String, val granted: Boolean) : OnboardingIntent
    data object Finish : OnboardingIntent
}

private const val LAST_STEP = 5

/**
 * Five-step onboarding flow: Welcome → Path → Family → Permissions → Done.
 *
 * Keeps in-memory state via [remember]; `onFinish` fires from the Done step's primary CTA.
 */
@Composable
fun OnboardingFlow(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var state by remember { mutableStateOf(OnboardingState()) }

    val onIntent: (OnboardingIntent) -> Unit = { intent ->
        when (intent) {
            OnboardingIntent.Next -> {
                if (state.step < LAST_STEP) state = state.copy(step = state.step + 1)
            }
            OnboardingIntent.Back -> {
                if (state.step > 1) state = state.copy(step = state.step - 1)
            }
            is OnboardingIntent.SelectPath -> {
                state = state.copy(path = intent.path)
            }
            is OnboardingIntent.SetFamilyName -> {
                state = state.copy(familyName = intent.name)
            }
            is OnboardingIntent.TogglePermission -> {
                val p = state.permissions
                state = state.copy(
                    permissions = when (intent.key) {
                        "push"     -> p.copy(push = intent.granted)
                        "calendar" -> p.copy(calendar = intent.granted)
                        "location" -> p.copy(location = intent.granted)
                        "contacts" -> p.copy(contacts = intent.granted)
                        else       -> p
                    }
                )
            }
            OnboardingIntent.Finish -> onFinish()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        when (state.step) {
            1 -> OnboardingWelcomeScreen(state = state, onIntent = onIntent)
            2 -> OnboardingPathScreen(state = state, onIntent = onIntent)
            3 -> OnboardingFamilySetupScreen(state = state, onIntent = onIntent)
            4 -> OnboardingPermissionsScreen(state = state, onIntent = onIntent)
            else -> OnboardingDoneScreen(state = state, onIntent = onIntent)
        }
    }
}
