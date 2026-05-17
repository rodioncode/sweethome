package com.jetbrains.kmpapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class CozyTab(val id: String, val emoji: String, val label: String) {
    HOME    ("home",     "⌂", "Главная"),
    LISTS   ("lists",    "☰", "Списки"),
    FAMILY  ("family",   "⌖", "Семья"),
    CALENDAR("calendar", "◷", "Календарь"),
    PROFILE ("profile",  "◉", "Профиль");
}

@Composable
fun CozyBottomNav(
    active: CozyTab,
    onTabSelected: (CozyTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(84.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 8.dp, vertical = 10.dp),
    ) {
        CozyTab.entries.forEach { tab ->
            val selected = tab == active
            val tint = if (selected) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurfaceVariant
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
