package com.jetbrains.kmpapp.screens.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetbrains.kmpapp.ui.LocalCozyShapes
import com.jetbrains.kmpapp.ui.LocalCozySpacing
import com.jetbrains.kmpapp.ui.components.CozyChip
import com.jetbrains.kmpapp.ui.components.resolve
import com.jetbrains.kmpapp.ui.components.resolveContainer
import com.jetbrains.kmpapp.ui.models.ListType

@Composable
fun AddTaskQuickScreen(
    onCreate: (title: String, type: ListType) -> Unit,
    onFullEdit: (title: String, type: ListType) -> Unit,
    onDismiss: () -> Unit,
    suggestedType: ListType = ListType.GENERAL_TODOS,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalCozySpacing.current
    val shapes = LocalCozyShapes.current

    var selectedType by remember { mutableStateOf(suggestedType) }
    var title by remember { mutableStateOf("") }

    val accent = selectedType.palette.resolve()
    val accentContainer = selectedType.palette.resolveContainer()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, shapes.sheet)
            .padding(spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Drag handle: 40dp wide, 4dp tall pill
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .clip(shapes.pill)
                .background(MaterialTheme.colorScheme.outlineVariant),
        )

        Spacer(Modifier.height(spacing.lg))

        // Type pill row — horizontal scroll
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(spacing.xs),
        ) {
            ListType.entries.forEach { lt ->
                val isSelected = lt == selectedType
                CozyChip(
                    label = "${lt.emoji} ${lt.displayName}",
                    selected = isSelected,
                    accent = lt.palette.resolve(),
                    accentContainer = lt.palette.resolveContainer(),
                    onClick = { selectedType = lt },
                )
            }
        }

        Spacer(Modifier.height(spacing.lg))

        // Big TextField "Что нужно сделать?"
        BasicTextField(
            value = title,
            onValueChange = { title = it },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .background(MaterialTheme.colorScheme.surface, shapes.button)
                .border(1.5.dp, accent, shapes.button)
                .padding(horizontal = spacing.lg, vertical = spacing.md),
            textStyle = TextStyle(
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Medium,
            ),
            cursorBrush = SolidColor(accent),
            singleLine = false,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                if (title.isNotBlank()) onCreate(title.trim(), selectedType)
            }),
            decorationBox = { inner ->
                if (title.isEmpty()) {
                    Text(
                        text = "Что нужно сделать?",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                    )
                }
                inner()
            },
        )

        Spacer(Modifier.height(spacing.md))

        // Hint: which list
        Row(
            modifier = Modifier
                .background(accentContainer, shapes.chip)
                .padding(horizontal = spacing.sm, vertical = spacing.xxs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.xxs),
        ) {
            Text(text = selectedType.emoji, fontSize = 12.sp)
            Text(
                text = "Добавить в ${selectedType.displayName}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = accent,
            )
        }

        Spacer(Modifier.height(spacing.xl))

        // Action buttons row: Создать + Подробнее + Отмена
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Primary CTA: Создать
            val canSubmit = title.isNotBlank()
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 52.dp)
                    .clip(shapes.button)
                    .background(
                        if (canSubmit) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant,
                    )
                    .clickable(enabled = canSubmit) {
                        onCreate(title.trim(), selectedType)
                    }
                    .padding(horizontal = spacing.lg, vertical = spacing.sm),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Создать",
                    color = if (canSubmit)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            // Outlined: Подробнее
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 52.dp)
                    .clip(shapes.button)
                    .border(1.5.dp, MaterialTheme.colorScheme.outline, shapes.button)
                    .clickable {
                        onFullEdit(title.trim(), selectedType)
                    }
                    .padding(horizontal = spacing.lg, vertical = spacing.sm),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Подробнее",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        Spacer(Modifier.height(spacing.sm))

        // Tertiary: Отмена
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 40.dp)
                .clip(shapes.button)
                .clickable(onClick = onDismiss)
                .padding(vertical = spacing.xs),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Отмена",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
