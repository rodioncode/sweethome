package com.jetbrains.kmpapp.screens.sharing

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetbrains.kmpapp.ui.LocalCozyShapes
import com.jetbrains.kmpapp.ui.LocalCozySpacing
import com.jetbrains.kmpapp.ui.components.CozyCard
import com.jetbrains.kmpapp.ui.components.CozyTopBar
import com.jetbrains.kmpapp.ui.components.EmptyHero
import com.jetbrains.kmpapp.ui.components.MetaRow

@Composable
fun InviteFamilyScreen(
    inviteCode: String,
    inviteLink: String,
    onCopy: (text: String) -> Unit,
    onShare: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalCozySpacing.current
    val shapes = LocalCozyShapes.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        CozyTopBar(title = "Пригласить в семью", onBack = onBack)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.xxl)
                .padding(top = spacing.md, bottom = spacing.xxxl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            EmptyHero(
                emoji = "👨‍👩‍👧",
                decor = listOf("💌", "🏡", "✨", "🌸"),
                size = 140.dp,
                bgColor = MaterialTheme.colorScheme.primaryContainer,
            )
            Spacer(Modifier.height(spacing.xl))

            Text(
                text = "Код приглашения",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(spacing.sm))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.xs, Alignment.CenterHorizontally),
            ) {
                val padded = inviteCode.padEnd(6, ' ').take(6)
                padded.forEach { ch ->
                    Box(
                        modifier = Modifier
                            .size(width = 40.dp, height = 52.dp)
                            .clip(shapes.chip)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = ch.toString().ifBlank { "·" },
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
            Spacer(Modifier.height(spacing.sm))
            Text(
                text = "Покажи код или поделись ссылкой",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
            )

            Spacer(Modifier.height(spacing.xxl))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Поделиться по:",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(Modifier.height(spacing.sm))

            CozyCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = 0.dp,
            ) {
                Column {
                    MetaRow(
                        icon = "🔗",
                        title = "Скопировать ссылку",
                        value = inviteLink,
                        modifier = Modifier.clickable {
                            onCopy(inviteLink)
                            onShare("link")
                        },
                        onClick = {
                            onCopy(inviteLink)
                            onShare("link")
                        },
                    )
                    MetaRow(
                        icon = "🔳",
                        title = "Показать QR-код",
                        value = null,
                        modifier = Modifier.clickable { onShare("qr") },
                        onClick = { onShare("qr") },
                    )
                    MetaRow(
                        icon = "✈️",
                        title = "Отправить в Telegram",
                        value = null,
                        modifier = Modifier.clickable { onShare("telegram") },
                        onClick = { onShare("telegram") },
                    )
                }
            }
        }
    }
}
