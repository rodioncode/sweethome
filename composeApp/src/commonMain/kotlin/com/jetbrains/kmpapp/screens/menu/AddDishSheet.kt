package com.jetbrains.kmpapp.screens.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetbrains.kmpapp.ui.LocalCozyShapes
import com.jetbrains.kmpapp.ui.components.CozyChip

enum class AddDishKind(val label: String, val emoji: String) {
    RECIPE  ("Рецепт",   "📖"),
    FREE    ("Свободно", "✏️"),
    OUT     ("Вне дома", "🍽"),
    DELIVERY("Доставка", "🛵"),
}

@Composable
fun AddDishSheet(
    onAdd: (kind: AddDishKind, payload: Map<String, String>) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shapes = LocalCozyShapes.current
    var kind by remember { mutableStateOf(AddDishKind.RECIPE) }
    var dishName by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var service by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, shapes.sheet)
            .padding(horizontal = 24.dp)
            .padding(top = 12.dp, bottom = 24.dp),
    ) {
        // Drag handle
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(shapes.pill)
                    .background(MaterialTheme.colorScheme.outlineVariant),
            )
        }

        Text(
            text = "Добавить блюдо",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(Modifier.height(14.dp))

        // 4 tabs
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            AddDishKind.entries.forEach { k ->
                CozyChip(
                    label = "${k.emoji} ${k.label}",
                    selected = k == kind,
                    onClick = { kind = k },
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        when (kind) {
            AddDishKind.RECIPE -> {
                OutlinedTextField(
                    value = dishName,
                    onValueChange = { dishName = it },
                    label = { Text("Название рецепта") },
                    placeholder = { Text("Паста с томатами…") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = shapes.button,
                    colors = cozyTextFieldColors(),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Или выбери из коллекции →",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            AddDishKind.FREE -> {
                OutlinedTextField(
                    value = dishName,
                    onValueChange = { dishName = it },
                    label = { Text("Что готовите") },
                    placeholder = { Text("Овсянка / Бутерброды / …") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = shapes.button,
                    colors = cozyTextFieldColors(),
                )
            }
            AddDishKind.OUT -> {
                OutlinedTextField(
                    value = dishName,
                    onValueChange = { dishName = it },
                    label = { Text("Где / что") },
                    placeholder = { Text("В кафе у дома") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = shapes.button,
                    colors = cozyTextFieldColors(),
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Заметка (повод, время)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = shapes.button,
                    colors = cozyTextFieldColors(),
                )
            }
            AddDishKind.DELIVERY -> {
                OutlinedTextField(
                    value = dishName,
                    onValueChange = { dishName = it },
                    label = { Text("Что заказать") },
                    placeholder = { Text("Пицца Маргарита") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = shapes.button,
                    colors = cozyTextFieldColors(),
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = service,
                        onValueChange = { service = it },
                        label = { Text("Сервис") },
                        placeholder = { Text("Самокат") },
                        modifier = Modifier.weight(1f),
                        shape = shapes.button,
                        colors = cozyTextFieldColors(),
                    )
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it.filter { c -> c.isDigit() } },
                        label = { Text("Цена ₽") },
                        modifier = Modifier.weight(1f),
                        shape = shapes.button,
                        colors = cozyTextFieldColors(),
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shapes.button)
                .background(
                    if (dishName.isNotBlank()) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outlineVariant
                )
                .clickable(enabled = dishName.isNotBlank()) {
                    val payload = buildMap {
                        put("name", dishName)
                        if (note.isNotBlank()) put("note", note)
                        if (price.isNotBlank()) put("price", price)
                        if (service.isNotBlank()) put("service", service)
                    }
                    onAdd(kind, payload)
                }
                .heightIn(min = 52.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Добавить",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onDismiss() }
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Отмена",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun cozyTextFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
    unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant,
)
