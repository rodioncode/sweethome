package com.jetbrains.kmpapp.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jetbrains.kmpapp.ui.DividerColor
import com.jetbrains.kmpapp.ui.OnPrimaryWhite
import com.jetbrains.kmpapp.ui.PrimaryGreen
import com.jetbrains.kmpapp.ui.SurfaceVariantCream
import com.jetbrains.kmpapp.ui.SurfaceWhite
import com.jetbrains.kmpapp.ui.SweetHomeSpacing
import com.jetbrains.kmpapp.ui.TextPrimary
import com.jetbrains.kmpapp.ui.TextSecondary
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProfileContent(
    navigateToLinkEmail: () -> Unit,
    navigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val viewModel = koinViewModel<ProfileViewModel>()
    val isGuest by viewModel.isGuest.collectAsStateWithLifecycle()
    val userId by viewModel.userId.collectAsStateWithLifecycle()
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val listCount by viewModel.listCount.collectAsStateWithLifecycle()
    val groupCount by viewModel.groupCount.collectAsStateWithLifecycle()
    val groups by viewModel.groups.collectAsStateWithLifecycle()

    LazyColumn(modifier = modifier.fillMaxSize()) {

        // --- Green hero header ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryGreen),
            ) {
                // Decorative circle
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .align(Alignment.TopEnd)
                        .padding(top = 0.dp)
                        .background(Color.White.copy(alpha = 0.06f), CircleShape),
                )

                Column(modifier = Modifier.fillMaxWidth().padding(20.dp).padding(top = 10.dp)) {
                    // Top row: title + logout
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "Профиль",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnPrimaryWhite,
                        )
                        Surface(
                            onClick = { viewModel.logout() },
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.15f),
                        ) {
                            Text(
                                "Выйти",
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnPrimaryWhite,
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Avatar row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.25f))
                                .border(3.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            val displayName = profile?.displayName ?: userId ?: ""
                            val initial = displayName.firstOrNull()?.uppercase() ?: "?"
                            Text(
                                initial,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnPrimaryWhite,
                            )
                        }
                        Column {
                            Text(
                                if (isGuest) "Гостевой аккаунт" else (profile?.displayName ?: userId ?: "Пользователь"),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnPrimaryWhite,
                            )
                            if (!isGuest && profile?.email != null) {
                                Text(
                                    profile!!.email!!.take(30),
                                    fontSize = 13.sp,
                                    color = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(top = 3.dp),
                                )
                            }
                            if (isGuest) {
                                Surface(
                                    onClick = navigateToLinkEmail,
                                    modifier = Modifier.padding(top = 8.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White.copy(alpha = 0.15f),
                                ) {
                                    Text(
                                        "Привязать email",
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = OnPrimaryWhite,
                                    )
                                }
                            } else {
                                Surface(
                                    onClick = navigateToSettings,
                                    modifier = Modifier.padding(top = 8.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White.copy(alpha = 0.15f),
                                ) {
                                    Text(
                                        "Редактировать",
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = OnPrimaryWhite,
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }

        // --- Stats row ---
        item {
            Surface(color = SurfaceWhite) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .border(width = 1.dp, color = DividerColor),
                ) {
                    listOf(
                        "0" to "задач",
                        listCount.toString() to "списков",
                        groupCount.toString() to "пространств",
                    ).forEachIndexed { index, (value, label) ->
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 16.dp)
                                .then(
                                    if (index < 2) Modifier.border(
                                        width = 1.dp,
                                        color = DividerColor,
                                    ) else Modifier
                                ),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                            Text(label, fontSize = 12.sp, color = TextSecondary, modifier = Modifier.padding(top = 2.dp))
                        }
                    }
                }
            }
        }

        // --- Settings section ---
        item {
            Spacer(Modifier.height(SweetHomeSpacing.lg))
            SectionLabel("НАСТРОЙКИ")
        }
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SweetHomeSpacing.md),
                shape = RoundedCornerShape(16.dp),
                color = SurfaceWhite,
                border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor),
            ) {
                Column {
                    val settingsItems = listOf(
                        "🔔" to "Уведомления",
                        "📅" to "Интеграция с календарём",
                        "🤖" to "Telegram-бот",
                        "🌙" to "Тема",
                        "🌐" to "Язык",
                    )
                    settingsItems.forEachIndexed { index, (icon, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { navigateToSettings() }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(icon, fontSize = 18.sp)
                            Text(label, fontSize = 15.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                            Text("›", fontSize = 13.sp, color = TextSecondary)
                        }
                        if (index < settingsItems.lastIndex) {
                            HorizontalDivider(color = DividerColor, modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }
        }

        // --- My spaces ---
        item {
            Spacer(Modifier.height(SweetHomeSpacing.lg))
            SectionLabel("МОИ ПРОСТРАНСТВА")
        }
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SweetHomeSpacing.md),
                shape = RoundedCornerShape(16.dp),
                color = SurfaceWhite,
                border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor),
            ) {
                Column {
                    // Personal space
                    SpaceRow(icon = "👤", title = "Личное", subtitle = "Только вы", onClick = {})
                    HorizontalDivider(color = DividerColor, modifier = Modifier.padding(horizontal = 16.dp))

                    // Group spaces
                    groups.forEachIndexed { index, group ->
                        SpaceRow(
                            icon = when (group.type) {
                                "family" -> "👨‍👩‍👧‍👦"
                                "mentoring" -> "🎓"
                                else -> "👥"
                            },
                            title = group.title,
                            subtitle = when (group.role) {
                                "owner" -> "owner"
                                else -> "member"
                            },
                            onClick = {},
                        )
                        if (index < groups.lastIndex) {
                            HorizontalDivider(color = DividerColor, modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }
        }

        // --- Activity ---
        item {
            Spacer(Modifier.height(SweetHomeSpacing.lg))
            SectionLabel("АКТИВНОСТЬ")
        }
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SweetHomeSpacing.md),
                shape = RoundedCornerShape(16.dp),
                color = SurfaceWhite,
                border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(PrimaryGreen.copy(alpha = 0.5f))
                            .padding(top = 6.dp),
                    )
                    Text("Нет недавней активности", fontSize = 13.sp, color = TextSecondary)
                }
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = TextSecondary,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(horizontal = SweetHomeSpacing.md, vertical = 0.dp).padding(bottom = 10.dp),
    )
}

@Composable
private fun SpaceRow(
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(icon, fontSize = 18.sp)
        Text(title, fontSize = 15.sp, color = TextPrimary, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text("$subtitle ›", fontSize = 12.sp, color = TextSecondary)
    }
}
