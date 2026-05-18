package com.jetbrains.kmpapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetbrains.kmpapp.ui.LocalCozyExtraColors

enum class CozyTab(val id: String, val emoji: String, val label: String) {
    HOME    ("home",     "⌂", "Главная"),
    FAMILY  ("family",   "⌖", "Мой дом"),
    LISTS   ("lists",    "☰", "Списки"),
    CALENDAR("calendar", "◷", "Календарь"),
    GROUPS  ("groups",   "◇", "Группы");
}

enum class CozySpace { PERSONAL, WORK }

@Composable
fun CozyBottomNav(
    active: CozyTab,
    onTabSelected: (CozyTab) -> Unit,
    modifier: Modifier = Modifier,
    space: CozySpace = CozySpace.PERSONAL,
) {
    val accent = when (space) {
        CozySpace.PERSONAL -> MaterialTheme.colorScheme.primary
        CozySpace.WORK     -> LocalCozyExtraColors.current.lavender
    }
    val inactive = MaterialTheme.colorScheme.onSurfaceVariant
    val outline  = MaterialTheme.colorScheme.outlineVariant

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(outline))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(83.dp)
                .padding(start = 8.dp, end = 8.dp, top = 10.dp),
        ) {
            CozyTab.entries.forEach { tab ->
                val selected = tab == active
                val tint = if (selected) accent else inactive
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onTabSelected(tab) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(text = tab.emoji, fontSize = 22.sp, color = tint,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                    Text(text = tab.label, fontSize = 10.sp, color = tint,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
                }
            }
        }
    }
}
