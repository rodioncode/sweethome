package com.jetbrains.kmpapp.screens.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.jetbrains.kmpapp.ui.components.CozyCard
import com.jetbrains.kmpapp.ui.components.CozyChip

@Composable
fun AddTaskFieldBlock(
    block: FieldBlock,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    val extras = LocalCozyExtraColors.current
    val shapes = LocalCozyShapes.current

    Column(modifier = modifier.fillMaxWidth()) {
        if (block !is FieldBlock.AddField) {
            Text(
                text = when (block) {
                    is FieldBlock.Pair -> block.label
                    is FieldBlock.Chips -> block.label
                    is FieldBlock.Segmented -> block.label
                    is FieldBlock.TextArea -> block.label
                    is FieldBlock.Rating -> block.label
                    is FieldBlock.CustomFields -> block.label
                    FieldBlock.AddField -> ""
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 10.dp),
            )
        }

        when (block) {
            is FieldBlock.Pair -> {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    HalfFieldCard(
                        label = block.left.label,
                        value = block.left.value,
                        modifier = Modifier.weight(1f),
                    )
                    HalfFieldCard(
                        label = block.right.label,
                        value = block.right.value,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            is FieldBlock.Chips -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    block.options.forEach { (label, selected) ->
                        CozyChip(
                            label = label,
                            selected = selected,
                            accent = accent,
                            accentContainer = accent.copy(alpha = 0.18f),
                        )
                    }
                }
            }

            is FieldBlock.Segmented -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, shapes.button)
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    block.options.forEach { (label, selected) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(MaterialTheme.shapes.small)
                                .background(if (selected) MaterialTheme.colorScheme.surface else Color.Transparent)
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (selected) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            is FieldBlock.TextArea -> {
                CozyCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text(
                            text = block.placeholder,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (block.value.isNotEmpty()) {
                            Text(
                                text = block.value,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(top = 6.dp),
                            )
                        }
                    }
                }
            }

            is FieldBlock.Rating -> {
                CozyCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        (1..5).forEach { n ->
                            Text(
                                text = "★",
                                fontSize = 24.sp,
                                color = if (n <= block.value) extras.ochre
                                else MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier.padding(end = 4.dp),
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = "${block.value} / 5",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            is FieldBlock.CustomFields -> {
                CozyCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        block.fields.forEachIndexed { i, (name, value) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(LocalCozySpacing.current.md),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = name.uppercase(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text = value,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        modifier = Modifier.padding(top = 2.dp),
                                    )
                                }
                                Text("⋯", color = extras.textTer, fontSize = 16.sp)
                            }
                            if (i < block.fields.lastIndex) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(MaterialTheme.colorScheme.outlineVariant),
                                )
                            }
                        }
                    }
                }
            }

            FieldBlock.AddField -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, shapes.button)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "+ Добавить своё поле",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun HalfFieldCard(label: String, value: String, modifier: Modifier = Modifier) {
    CozyCard(modifier = modifier) {
        Column {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value.ifEmpty { "—" },
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}
