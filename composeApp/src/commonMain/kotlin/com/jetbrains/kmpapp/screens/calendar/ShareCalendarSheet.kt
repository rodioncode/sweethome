package com.jetbrains.kmpapp.screens.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetbrains.kmpapp.ui.SweetHomeShapes
import com.jetbrains.kmpapp.ui.SweetHomeSpacing

enum class CalendarVisibility(val label: String) {
    ALL("Все события"),
    MY_TASKS("Только мои дела"),
    BUSY_FREE("Свободно / занято"),
}

data class CalendarShareConfig(
    val visibility: CalendarVisibility = CalendarVisibility.ALL,
    val notifyChanges: Boolean = true,
    val notifyEvents: Boolean = true,
)

/**
 * G-12 ShareCalendarSheet — sharing settings + visibility + notifications.
 * Backed by an in-memory state until CalendarShareRepository is wired.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareCalendarSheet(
    initial: CalendarShareConfig = CalendarShareConfig(),
    onSave: (CalendarShareConfig) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var visibility by remember { mutableStateOf(initial.visibility) }
    var notifyChanges by remember { mutableStateOf(initial.notifyChanges) }
    var notifyEvents by remember { mutableStateOf(initial.notifyEvents) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = SweetHomeShapes.BottomSheet,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = SweetHomeSpacing.bottomSheetPaddingH,
                vertical = SweetHomeSpacing.xl,
            ),
            verticalArrangement = Arrangement.spacedBy(SweetHomeSpacing.xl),
        ) {
            Text(
                "Поделиться календарём",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                "Видимость определяет, что увидят другие.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // What to show
            Text(
                "ЧТО ПОКАЗАТЬ",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            CalendarVisibility.entries.forEach { v ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = visibility == v,
                        onClick = { visibility = v },
                        colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary),
                    )
                    Text(v.label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Notifications
            Text(
                "УВЕДОМЛЕНИЯ",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Switch(
                    checked = notifyChanges,
                    onCheckedChange = { notifyChanges = it },
                    colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary),
                )
                Spacer(Modifier.padding(SweetHomeSpacing.xxs))
                Text("Уведомлять при изменениях", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Switch(
                    checked = notifyEvents,
                    onCheckedChange = { notifyEvents = it },
                    colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary),
                )
                Spacer(Modifier.padding(SweetHomeSpacing.xxs))
                Text("Напоминать о событиях семьи", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(SweetHomeSpacing.sm)) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = SweetHomeShapes.Button,
                ) { Text("Отмена") }
                Button(
                    onClick = {
                        onSave(CalendarShareConfig(visibility, notifyChanges, notifyEvents))
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = SweetHomeShapes.Button,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) { Text("Сохранить", fontWeight = FontWeight.Bold) }
            }

            Spacer(Modifier.height(SweetHomeSpacing.xl))
        }
    }
}
