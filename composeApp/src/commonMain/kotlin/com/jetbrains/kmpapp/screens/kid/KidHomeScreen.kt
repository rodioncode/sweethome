package com.jetbrains.kmpapp.screens.kid

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetbrains.kmpapp.ui.KidColors
import com.jetbrains.kmpapp.ui.KidTheme
import com.jetbrains.kmpapp.ui.LocalKidColors
import com.jetbrains.kmpapp.ui.components.kid.KidSpeechBubble
import com.jetbrains.kmpapp.ui.components.kid.KidStarPill

@Composable
fun KidHomeScreen(
    state: KidHomeState,
    onIntent: (KidHomeIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    KidTheme {
        val c = LocalKidColors.current

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(c.cream),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 92.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                // 1. Greeting + star pill
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 8.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = state.dateLabel,
                            fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                            color = c.inkSec,
                        )
                        Text(
                            text = "Привет, ${state.kidName}! 👋",
                            fontSize = 26.sp, fontWeight = FontWeight.Bold,
                            color = c.ink, lineHeight = 28.sp,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                    KidStarPill(count = state.stars, big = true, onClick = { onIntent(KidHomeIntent.OpenShop) })
                }

                // 2. Pet speech bubble
                Box(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp)) {
                    KidSpeechBubble {
                        Text(
                            text = state.petGreeting,
                            fontSize = 15.sp, fontWeight = FontWeight.Medium, color = c.ink,
                        )
                    }
                }

                // 3. Mini pet under bubble
                Row(
                    modifier = Modifier.padding(start = 20.dp, top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(c.grass.copy(alpha = 0.3f))
                            .border(3.dp, c.line, CircleShape)
                            .clickable { onIntent(KidHomeIntent.OpenPet) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = state.petEmoji, fontSize = 36.sp)
                    }
                }

                Spacer(Modifier.height(20.dp))

                // 4. Adventure path header
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "🗺 Сегодняшний путь",
                        fontSize = 14.sp, fontWeight = FontWeight.Bold,
                        color = c.ink, modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "${state.stops.count { it.done }} из ${state.stops.size}",
                        fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                        color = c.inkSec,
                    )
                }

                Spacer(Modifier.height(8.dp))

                // 5. Stops
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Column {
                        state.stops.forEachIndexed { i, stop ->
                            StopRow(stop = stop, accent = c, onClick = {
                                if (!stop.done) onIntent(KidHomeIntent.OpenStop(stop.id))
                            })
                            Spacer(Modifier.height(if (i < state.stops.lastIndex) 6.dp else 12.dp))
                        }

                        // 6. Prize stop
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(c.candy)
                                    .border(3.dp, c.candyDeep, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) { Text(text = "🎁", fontSize = 32.sp) }
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("ПРИЗ", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = c.candyDeep)
                                Text(state.prizeName, fontSize = 15.sp, fontWeight = FontWeight.Bold,
                                    color = c.ink, modifier = Modifier.padding(top = 2.dp))
                                Text("осталось ${state.prizeRemaining} ⭐",
                                    fontSize = 12.sp, color = c.inkSec,
                                    modifier = Modifier.padding(top = 2.dp))
                            }
                        }
                    }
                }
            }

            // Bottom nav
            KidBottomNav(
                active = KidTab.HOME,
                onTabSelected = { onIntent(KidHomeIntent.NavTab(it)) },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@Composable
private fun StopRow(stop: KidStop, accent: KidColors, onClick: () -> Unit) {
    val dotColor = when {
        stop.done    -> accent.grass
        stop.current -> accent.sun
        else         -> accent.warm
    }
    val dotDeep = when {
        stop.done    -> accent.grassDeep
        stop.current -> accent.sunDeep
        else         -> accent.line
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !stop.done, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(dotColor)
                .border(3.dp, dotDeep, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (stop.done) {
                Text("✓", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
            } else {
                Text(stop.emoji, fontSize = 32.sp)
            }
        }
        Spacer(Modifier.width(16.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .background(accent.paper, RoundedCornerShape(20.dp))
                .border(
                    width = 2.dp,
                    color = if (stop.current) accent.sunDeep else accent.line,
                    shape = RoundedCornerShape(20.dp),
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Text(
                text = stop.title,
                fontSize = 17.sp, fontWeight = FontWeight.Bold,
                color = if (stop.done) accent.inkTer else accent.ink,
                textDecoration = if (stop.done) TextDecoration.LineThrough else TextDecoration.None,
            )
            Row(
                modifier = Modifier.padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("⭐", fontSize = 14.sp)
                Spacer(Modifier.width(4.dp))
                Text(
                    text = if (stop.done) "+${stop.reward} получил" else "+${stop.reward}",
                    fontSize = 13.sp, fontWeight = FontWeight.Bold,
                    color = if (stop.done) accent.grassDeep else accent.sunDeep,
                )
                if (stop.current) {
                    Spacer(Modifier.width(8.dp))
                    Text("· твой ход!", fontSize = 12.sp,
                        fontWeight = FontWeight.Bold, color = accent.sunDeep)
                }
            }
        }
    }
}

@Composable
internal fun KidBottomNav(
    active: KidTab,
    onTabSelected: (KidTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalKidColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(92.dp)
            .background(c.paper)
            .border(2.dp, c.line, RoundedCornerShape(0.dp))
            .padding(horizontal = 8.dp, vertical = 10.dp),
    ) {
        KidTab.entries.forEach { tab ->
            val sel = tab == active
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTabSelected(tab) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(if (sel) c.sun else Color.Transparent),
                    contentAlignment = Alignment.Center,
                ) { Text(tab.emoji, fontSize = 28.sp) }
                Text(tab.label, fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (sel) c.ink else c.inkTer)
            }
        }
    }
}
