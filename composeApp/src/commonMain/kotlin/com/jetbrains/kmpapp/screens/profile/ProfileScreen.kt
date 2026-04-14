package com.jetbrains.kmpapp.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.jetbrains.kmpapp.ui.PrimaryGreenDark
import com.jetbrains.kmpapp.ui.PrimaryGreenLight
import com.jetbrains.kmpapp.ui.SecondaryPeach
import com.jetbrains.kmpapp.ui.SweetHomeShapes
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
    val listCount by viewModel.listCount.collectAsStateWithLifecycle()
    val groupCount by viewModel.groupCount.collectAsStateWithLifecycle()
    val groups by viewModel.groups.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
    ) {
        // --- Green header: top bar + avatar ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryGreen),
            ) {
                // Top bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = SweetHomeSpacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Профиль",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OnPrimaryWhite,
                        modifier = Modifier.weight(1f),
                    )
                    Surface(
                        onClick = navigateToSettings,
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape,
                        color = Color(0xFF4A6B49),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("⚙", fontSize = 18.sp, color = OnPrimaryWhite)
                        }
                    }
                }

                // Avatar section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = SweetHomeSpacing.md),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Avatar with edit badge
                    Box {
                        Surface(
                            modifier = Modifier.size(80.dp),
                            shape = CircleShape,
                            color = PrimaryGreenLight,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                val initial = userId?.firstOrNull()?.uppercase() ?: "?"
                                Text(
                                    text = initial,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OnPrimaryWhite,
                                )
                            }
                        }
                        // Edit badge
                        Surface(
                            modifier = Modifier
                                .size(28.dp)
                                .align(Alignment.BottomEnd),
                            shape = CircleShape,
                            color = SecondaryPeach,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("✎", fontSize = 14.sp, color = OnPrimaryWhite)
                            }
                        }
                    }

                    Spacer(Modifier.height(SweetHomeSpacing.xs))

                    Text(
                        text = if (isGuest) "Гостевой аккаунт" else (userId ?: "Пользователь"),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnPrimaryWhite,
                    )
                    if (!isGuest && userId != null) {
                        Text(
                            text = userId!!.take(30),
                            fontSize = 13.sp,
                            color = DividerColor,
                        )
                    }
                }
            }
        }

        // --- Stats row ---
        item {
            Spacer(Modifier.height(SweetHomeSpacing.sm))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SweetHomeSpacing.md),
                horizontalArrangement = Arrangement.spacedBy(SweetHomeSpacing.xs),
            ) {
                StatCard(value = "0", label = "Выполнено", modifier = Modifier.weight(1f))
                StatCard(value = listCount.toString(), label = "Списков", modifier = Modifier.weight(1f))
                StatCard(value = groupCount.toString(), label = "Участий", modifier = Modifier.weight(1f))
            }
        }

        // --- Мои пространства ---
        item {
            Spacer(Modifier.height(SweetHomeSpacing.lg))
            Text(
                text = "Мои пространства",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = SweetHomeSpacing.md),
            )
            Spacer(Modifier.height(SweetHomeSpacing.xs))
        }

        // Personal space (always shown)
        item {
            SpaceCard(
                icon = "🏠",
                title = "Личное пространство",
                subtitle = "Только вы",
                onClick = {},
            )
        }

        // Group spaces
        items(groups, key = { it.id }) { group ->
            SpaceCard(
                icon = if (group.type == "family") "👨\u200D👩\u200D👧\u200D👦" else "👥",
                title = group.name,
                subtitle = buildString {
                    val memberCount = group.members?.size
                    if (memberCount != null) append("$memberCount участника · ")
                    append(
                        when (group.role) {
                            "owner" -> "Владелец"
                            "admin" -> "Администратор"
                            else -> "Участник"
                        }
                    )
                },
                onClick = {},
            )
        }

        // --- Последняя активность ---
        item {
            Spacer(Modifier.height(SweetHomeSpacing.lg))
            Text(
                text = "Последняя активность",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = SweetHomeSpacing.md),
            )
            Spacer(Modifier.height(SweetHomeSpacing.xs))
        }
        item {
            ActivityItem(icon = "✅", text = "Нет недавней активности", time = "")
        }

        item { Spacer(Modifier.height(SweetHomeSpacing.lg)) }
    }
}

@Composable
private fun StatCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = SweetHomeShapes.Card,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = SweetHomeSpacing.xs, horizontal = SweetHomeSpacing.xs),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryGreen,
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun SpaceCard(
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = SweetHomeSpacing.md, vertical = SweetHomeSpacing.xxs)
            .clickable(onClick = onClick),
        shape = SweetHomeShapes.Card,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = SweetHomeSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = PrimaryGreenLight.copy(alpha = 0.3f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(icon, fontSize = 20.sp)
                }
            }
            Spacer(Modifier.width(SweetHomeSpacing.sm))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = "›",
                fontSize = 20.sp,
                color = Color(0xFFBDBDBD),
            )
        }
    }
}

@Composable
private fun ActivityItem(
    icon: String,
    text: String,
    time: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = SweetHomeSpacing.md, vertical = SweetHomeSpacing.xxs),
        shape = SweetHomeShapes.Card,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = SweetHomeSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(icon, fontSize = 18.sp)
            Spacer(Modifier.width(SweetHomeSpacing.sm))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = text,
                    fontSize = 12.sp,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (time.isNotBlank()) {
                    Text(
                        text = time,
                        fontSize = 11.sp,
                        color = TextSecondary,
                    )
                }
            }
        }
    }
}
