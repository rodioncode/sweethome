package com.jetbrains.kmpapp.screens.pet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.jetbrains.kmpapp.ui.components.CozyCard
import com.jetbrains.kmpapp.ui.components.CozyTopBar
import com.jetbrains.kmpapp.ui.components.pet.PetSceneTile
import com.jetbrains.kmpapp.ui.models.Pet
import com.jetbrains.kmpapp.ui.models.Species
import com.jetbrains.kmpapp.ui.models.Stage

@Composable
fun PetCardScreen(
    pet: Pet?,
    onBack: () -> Unit,
    onWardrobe: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val extras = LocalCozyExtraColors.current
    val shapes = LocalCozyShapes.current
    val accent = pet?.let { speciesAccent(it.species, extras) } ?: extras.coral
    val accentSoft = pet?.let { speciesAccentSoft(it.species, extras) } ?: extras.coralSoft

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        CozyTopBar(onBack = onBack, action = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(shapes.pill)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) { Text("⋯", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface) }
        })

        if (pet == null) return@Column

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 28.dp),
        ) {

            // Hero ────────────────────────────────────────────────────
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                PetSceneTile(pet = pet, size = 220.dp)
                Spacer(Modifier.height(14.dp))
                Text(pet.name, fontSize = 24.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground)
                Text(
                    text = "${pet.species.displayName} · Любопытная · ${pet.species.habitat}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }

            // Quick actions ───────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf("🫳" to "Погладить", "🍪" to "Покормить", "👗" to "Нарядить", "📸" to "Фото")
                    .forEach { (e, l) ->
                        QuickAction(emoji = e, label = l, modifier = Modifier.weight(1f))
                    }
            }

            // Stage progress ──────────────────────────────────────────
            SectionLabel("СТАДИЯ")
            CozyCard(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                contentPadding = 14.dp,
            ) {
                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${pet.stage.emoji} ${pet.stage.displayName}",
                                fontSize = 16.sp, fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            val nextStage = Stage.entries.firstOrNull { it.level > pet.level }
                            val nextLabel = nextStage?.let {
                                val stars = (it.level - pet.level) * 35
                                "До «${it.displayName}» — $stars ⭐"
                            } ?: "Максимальная стадия"
                            Text(nextLabel, fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 2.dp))
                        }
                        Text("${pet.level} / 12", fontSize = 13.sp,
                            fontWeight = FontWeight.Bold, color = accent)
                    }
                    Spacer(Modifier.height(14.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Stage.entries.forEach { stage ->
                            val unlocked = pet.level >= stage.level
                            val current = stage == pet.stage
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(shapes.pill)
                                        .background(
                                            when {
                                                current -> accent
                                                unlocked -> accentSoft
                                                else -> MaterialTheme.colorScheme.surfaceVariant
                                            }
                                        )
                                        .then(
                                            if (current) Modifier.border(2.dp, accent, shapes.pill)
                                            else Modifier
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = if (unlocked) stage.emoji else "🔒",
                                        fontSize = 14.sp,
                                        color = if (current) MaterialTheme.colorScheme.onPrimary
                                                else MaterialTheme.colorScheme.onBackground,
                                    )
                                }
                                Text(
                                    text = stage.level.toString(),
                                    fontSize = 9.sp, fontWeight = FontWeight.Bold,
                                    color = if (current) accent else extras.textTer,
                                    modifier = Modifier.padding(top = 4.dp),
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(shapes.pill)
                            .background(MaterialTheme.colorScheme.outlineVariant),
                    ) {
                        val nextStage = Stage.entries.firstOrNull { it.level > pet.level }
                        val progress = if (nextStage != null) {
                            val prev = Stage.entries.lastOrNull { it.level <= pet.level }?.level ?: 1
                            ((pet.level - prev + 0.5f) / (nextStage.level - prev).coerceAtLeast(1)).coerceIn(0f, 1f)
                        } else 1f
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progress)
                                .clip(shapes.pill)
                                .background(accent),
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Mood ────────────────────────────────────────────────────
            SectionLabel("НАСТРОЕНИЕ НА ЭТОЙ НЕДЕЛЕ")
            CozyCard(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                contentPadding = 14.dp,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(shapes.cardLarge)
                            .background(extras.lavenderSoft),
                        contentAlignment = Alignment.Center,
                    ) { Text(pet.mood.emoji, fontSize = 30.sp) }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = moodHeadlineFor(pet.mood),
                            fontSize = 15.sp, fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            text = "Ты закрыл 12 задач из 15. ${pet.name} довольна, но устала — отдохни и она тоже отдохнёт.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 15.sp,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                MoodReasonChip("12 ✓ дел", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
                MoodReasonChip("1 просрочка", extras.coral, extras.coralSoft)
                MoodReasonChip("3 дня подряд", extras.ochre, extras.ochreSoft)
            }

            Spacer(Modifier.height(20.dp))

            // Accessories ─────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 0.dp)
                    .padding(bottom = 10.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = "АКСЕССУАРЫ · ${pet.accessories.size} / ${SAMPLE_ACCESSORIES.size}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "Все →",
                    fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(shapes.button)
                        .clickable { onWardrobe() }
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                )
            }
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(start = 24.dp, end = 24.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SAMPLE_ACCESSORIES.forEach { acc ->
                    val equipped = pet.accessories.contains(acc.id)
                    AccessoryTile(acc, equipped = equipped, accent = accent, accentSoft = accentSoft)
                }
            }

            Spacer(Modifier.height(20.dp))

            // Diary ───────────────────────────────────────────────────
            SectionLabel("ДНЕВНИК ${pet.name.uppercase()} · 3")
            CozyCard(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                contentPadding = 0.dp,
                radius = 18.dp,
            ) {
                Column {
                    sampleDiary(pet.name).forEachIndexed { i, entry ->
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(entry.emoji, fontSize = 20.sp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = entry.date.uppercase(),
                                    fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                    color = extras.textTer,
                                )
                                Text(
                                    text = "«${entry.text}»",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    lineHeight = 18.sp,
                                    modifier = Modifier.padding(top = 4.dp),
                                )
                            }
                        }
                        if (i < 2) {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(1.dp)
                                    .background(MaterialTheme.colorScheme.outlineVariant),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Friendship hint ─────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
                    .clip(shapes.chip)
                    .background(accentSoft)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text("👯", fontSize = 18.sp)
                Text(
                    text = "${pet.name} подружилась с Бубликом (Аня) и Колючкой (Дима). Можно сделать совместное фото.",
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 10.dp),
    )
}

@Composable
private fun QuickAction(emoji: String, label: String, modifier: Modifier = Modifier) {
    CozyCard(modifier = modifier, bordered = true, contentPadding = 10.dp, radius = 14.dp) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(emoji, fontSize = 20.sp)
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun MoodReasonChip(label: String, accent: Color, bg: Color) {
    Box(
        modifier = Modifier
            .clip(LocalCozyShapes.current.chip)
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = accent)
    }
}

private data class AccessorySpec(
    val id: String,
    val emoji: String,
    val name: String,
    val unlocked: Boolean,
)

private val SAMPLE_ACCESSORIES = listOf(
    AccessorySpec("bow", "🎀", "Бантик", true),
    AccessorySpec("scarf", "🧣", "Шарф", true),
    AccessorySpec("hat", "🎩", "Шляпа", true),
    AccessorySpec("glasses", "👓", "Очки", true),
    AccessorySpec("cape", "🦸", "Плащ", false),
    AccessorySpec("crown", "👑", "Корона", false),
)

@Composable
private fun AccessoryTile(
    acc: AccessorySpec,
    equipped: Boolean,
    accent: Color,
    accentSoft: Color,
) {
    val shapes = LocalCozyShapes.current
    Column(
        modifier = Modifier
            .width(72.dp)
            .clip(shapes.chip)
            .background(if (equipped) accentSoft else MaterialTheme.colorScheme.surface)
            .border(
                width = if (equipped) 1.5.dp else 1.dp,
                color = if (equipped) accent else MaterialTheme.colorScheme.outlineVariant,
                shape = shapes.chip,
            )
            .padding(horizontal = 6.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = if (acc.unlocked) acc.emoji else "🔒",
            fontSize = 26.sp,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = acc.name,
            fontSize = 10.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        if (equipped) {
            Text(
                text = "НАДЕТО",
                fontSize = 8.sp, fontWeight = FontWeight.Bold,
                color = accent,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

private data class DiaryEntry(val date: String, val text: String, val emoji: String)

private fun sampleDiary(name: String): List<DiaryEntry> = listOf(
    DiaryEntry("Вчера", "Ты сделал 5 дел подряд — я довольна. Особенно понравилось «полить цветы».", "🌿"),
    DiaryEntry("3 дня назад", "Ты забыл про учёбу. Не страшно, но я немного скучала.", "🌧"),
    DiaryEntry("Неделю назад", "Получила бантик 🎀 за 30 закрытых задач. Маша, ты молодец.", "🎀"),
)

private fun moodHeadlineFor(mood: com.jetbrains.kmpapp.ui.models.Mood): String = when (mood) {
    com.jetbrains.kmpapp.ui.models.Mood.HAPPY   -> "Сияет от радости"
    com.jetbrains.kmpapp.ui.models.Mood.SLEEPY  -> "Сонный, но довольный"
    com.jetbrains.kmpapp.ui.models.Mood.EXCITED -> "Полон энергии"
    com.jetbrains.kmpapp.ui.models.Mood.SAD     -> "Скучает по тебе"
}

private fun speciesAccent(s: Species, ex: com.jetbrains.kmpapp.ui.CozyExtraColors): Color = when (s) {
    Species.RACCOON, Species.BUNNY  -> ex.ochre
    Species.FOX                      -> ex.coral
    Species.CAT, Species.HEDGIE, Species.PANDA -> ex.lavender
}

private fun speciesAccentSoft(s: Species, ex: com.jetbrains.kmpapp.ui.CozyExtraColors): Color = when (s) {
    Species.RACCOON, Species.BUNNY  -> ex.ochreSoft
    Species.FOX                      -> ex.coralSoft
    Species.CAT, Species.HEDGIE, Species.PANDA -> ex.lavenderSoft
}

