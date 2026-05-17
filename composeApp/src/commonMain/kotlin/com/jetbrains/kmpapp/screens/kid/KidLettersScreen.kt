package com.jetbrains.kmpapp.screens.kid

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetbrains.kmpapp.ui.KidTheme
import com.jetbrains.kmpapp.ui.LocalKidColors
import com.jetbrains.kmpapp.ui.LocalKidTypography
import com.jetbrains.kmpapp.ui.components.kid.KidCard

data class KidLetter(
    val id: String,
    val senderName: String,
    val senderEmoji: String = "💌",
    val messagePreview: String,
    val fullMessage: String = "",
    val date: String = "",
    val isExpanded: Boolean = false,
)

data class KidLettersState(
    val letters: List<KidLetter> = emptyList(),
)

@Composable
fun KidLettersScreen(
    state: KidLettersState,
    onLetterTap: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    KidTheme {
        val colors = LocalKidColors.current
        val type = LocalKidTypography.current

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(colors.cream)
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            Text(
                text = "Письма",
                style = type.heading,
                color = colors.ink,
            )

            Spacer(Modifier.height(20.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.letters, key = { it.id }) { letter ->
                    KidCard(tilt = 0.5f) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onLetterTap(letter.id) }
                                .padding(4.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                // Sender avatar circle
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(colors.candy.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = letter.senderEmoji,
                                        fontSize = 20.sp,
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = letter.senderName,
                                        style = type.title,
                                        color = colors.ink,
                                    )
                                    if (letter.date.isNotEmpty()) {
                                        Text(
                                            text = letter.date,
                                            style = type.caption,
                                            color = colors.inkSec,
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(8.dp))

                            if (letter.isExpanded && letter.fullMessage.isNotEmpty()) {
                                Text(
                                    text = letter.fullMessage,
                                    style = type.body,
                                    color = colors.ink,
                                )
                            } else {
                                Text(
                                    text = letter.messagePreview,
                                    style = type.body,
                                    color = colors.inkSec,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
