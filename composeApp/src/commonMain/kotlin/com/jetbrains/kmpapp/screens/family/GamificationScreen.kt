package com.jetbrains.kmpapp.screens.family

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetbrains.kmpapp.ui.DividerColor
import com.jetbrains.kmpapp.ui.PrimaryGreen
import com.jetbrains.kmpapp.ui.SurfaceVariantCream
import com.jetbrains.kmpapp.ui.SurfaceWhite
import com.jetbrains.kmpapp.ui.TextPrimary
import com.jetbrains.kmpapp.ui.TextSecondary

private data class LeaderboardEntry(
    val name: String,
    val initials: String,
    val color: Color,
    val points: Int,
    val done: Int,
    val streak: Int,
    val rank: Int,
)

private data class Achievement(
    val icon: String,
    val title: String,
    val desc: String,
    val unlocked: Boolean,
)

private val leaderboard = listOf(
    LeaderboardEntry("Аня Смирнова", "АС", Color(0xFF5B7C5A), 142, 28, 7, 1),
    LeaderboardEntry("Дима Смирнов", "ДС", Color(0xFF42A5F5), 98, 18, 3, 2),
    LeaderboardEntry("Соня Смирнова", "СС", Color(0xFFFF7043), 74, 14, 2, 3),
    LeaderboardEntry("Бабушка", "БА", Color(0xFFAB47BC), 51, 10, 1, 4),
)

private val achievements = listOf(
    Achievement("🔥", "Стрик 7 дней", "Выполняла задачи 7 дней подряд", true),
    Achievement("⚡", "Суперскорость", "5 задач за день", true),
    Achievement("🌟", "Первые 100 очков", "Набери 100 баллов", true),
    Achievement("👑", "Месяц в лидерах", "30 дней на первом месте", false),
    Achievement("🤝", "Командный игрок", "Помоги 10 задачам других", false),
    Achievement("🏠", "Хозяйка дома", "50 домашних дел выполнено", false),
)

private val rankEmoji = listOf("🥇", "🥈", "🥉", "4️⃣")

@Composable
fun GamificationScreen(
    navigateBack: () -> Unit,
    navigateToShop: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceVariantCream),
    ) {
        // TopBar
        Surface(
            color = SurfaceWhite,
            shadowElevation = 2.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Surface(
                    onClick = navigateBack,
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    color = SurfaceVariantCream,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("‹", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                    }
                }
                Text(
                    "🏆 Семейный рейтинг",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(bottom = 32.dp),
        ) {
            // Podium
            item {
                Podium(modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp))
            }

            // Full leaderboard
            item {
                Text(
                    "Таблица лидеров",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
                Spacer(Modifier.height(8.dp))
            }

            items(leaderboard.size) { i ->
                val entry = leaderboard[i]
                val isFirst = i == 0
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = if (isFirst) Color(0xFFFFFDE7) else SurfaceWhite,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isFirst) Color(0xFFFFD700) else DividerColor,
                    ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp, 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(rankEmoji[i], fontSize = 20.sp, modifier = Modifier.width(28.dp))
                        MemberCircle(displayName = entry.initials, color = entry.color, size = 40, fontSize = 14)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                entry.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                            )
                            Text(
                                "🔥 стрик ${entry.streak} дн · ${entry.done} задач",
                                fontSize = 11.sp,
                                color = TextSecondary,
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "${entry.points}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGreen,
                            )
                            Text("очков", fontSize = 10.sp, color = TextSecondary)
                        }
                    }
                }
            }

            // Achievements
            item {
                Spacer(Modifier.height(20.dp))
                Text(
                    "Достижения",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
                Spacer(Modifier.height(8.dp))
                AchievementsGrid(
                    achievements = achievements,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }

            // Shop button
            item {
                Spacer(Modifier.height(20.dp))
                Surface(
                    onClick = navigateToShop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = Color.Transparent,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(listOf(Color(0xFFE8A87C), Color(0xFFD4956B))),
                                RoundedCornerShape(14.dp),
                            )
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "🛍 Потратить баллы в магазине",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Podium(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // 2nd place
        PodiumSlot(
            entry = leaderboard[1],
            podiumHeight = 60.dp.value.toInt(),
            podiumColor = Color(0xFFE8E8E8),
            avatarSize = 56,
            avatarBorderColor = Color(0xFFC0C0C0),
            modifier = Modifier.weight(1f),
        )
        // 1st place
        PodiumSlot(
            entry = leaderboard[0],
            podiumHeight = 80.dp.value.toInt(),
            podiumColor = null, // gold gradient
            avatarSize = 68,
            avatarBorderColor = Color(0xFFFFD700),
            glowColor = Color(0xFFFFD700),
            modifier = Modifier.weight(1f),
        )
        // 3rd place
        PodiumSlot(
            entry = leaderboard[2],
            podiumHeight = 44.dp.value.toInt(),
            podiumColor = Color(0xFFCD7F32),
            avatarSize = 56,
            avatarBorderColor = Color(0xFFCD7F32),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun PodiumSlot(
    entry: LeaderboardEntry,
    podiumHeight: Int,
    podiumColor: Color?,
    avatarSize: Int,
    avatarBorderColor: Color,
    glowColor: Color? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        MemberCircle(
            displayName = entry.initials,
            color = entry.color,
            size = avatarSize,
            fontSize = if (avatarSize >= 68) 22 else 18,
            borderColor = avatarBorderColor,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            entry.name.split(" ").firstOrNull() ?: entry.name,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
        )
        Text(
            "${entry.points} ⭐",
            fontSize = 11.sp,
            color = TextSecondary,
        )
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(podiumHeight.dp)
                .then(
                    if (podiumColor != null) {
                        Modifier.background(podiumColor, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    } else {
                        Modifier.background(
                            Brush.verticalGradient(listOf(Color(0xFFFFD700), Color(0xFFFFA000))),
                            RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                        )
                    }
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(rankEmoji[entry.rank - 1], fontSize = if (entry.rank == 1) 24.sp else 20.sp)
        }
    }
}

@Composable
private fun AchievementsGrid(
    achievements: List<Achievement>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        achievements.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { achievement ->
                    AchievementCard(
                        achievement = achievement,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun AchievementCard(
    achievement: Achievement,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = if (achievement.unlocked) SurfaceWhite else SurfaceVariantCream,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (achievement.unlocked) PrimaryGreen else DividerColor,
        ),
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .then(if (!achievement.unlocked) Modifier else Modifier),
        ) {
            Text(
                if (achievement.unlocked) achievement.icon else "🔒",
                fontSize = 28.sp,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                achievement.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                achievement.desc,
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 15.sp,
            )
            if (achievement.unlocked) {
                Spacer(Modifier.height(6.dp))
                Text(
                    "✓ Получено",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen,
                )
            }
        }
    }
}

@Composable
internal fun MemberCircle(
    displayName: String,
    color: Color,
    size: Int,
    fontSize: Int,
    borderColor: Color? = null,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(color)
            .then(
                if (borderColor != null) Modifier.border(3.dp, borderColor, CircleShape)
                else Modifier
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            displayName,
            fontSize = fontSize.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
    }
}
