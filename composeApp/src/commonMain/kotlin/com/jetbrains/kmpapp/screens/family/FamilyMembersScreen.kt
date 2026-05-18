package com.jetbrains.kmpapp.screens.family

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetbrains.kmpapp.ui.LocalCozyExtraColors
import com.jetbrains.kmpapp.ui.LocalCozyShapes
import com.jetbrains.kmpapp.ui.LocalCozySpacing
import com.jetbrains.kmpapp.ui.components.CozyAvatar
import com.jetbrains.kmpapp.ui.components.CozyCard
import com.jetbrains.kmpapp.ui.components.CozyChip
import com.jetbrains.kmpapp.ui.components.CozyTopBar
import com.jetbrains.kmpapp.ui.models.Palette

private data class FamilyMember(
    val id: String,
    val name: String,
    val initials: String,
    val palette: Palette,
    val roleLabel: String,
    val ageLabel: String,
    val email: String?,
    val done: Int,
    val you: Boolean,
)

private val mockMembers = listOf(
    FamilyMember("u1", "Аня Сидорова", "А", Palette.CORAL, "Владелец", "мама", "anya@sweethome.app", 47, you = true),
    FamilyMember("u2", "Дима Сидоров", "Д", Palette.PRIMARY, "Взрослый", "папа", "dima@sweethome.app", 32, you = false),
    FamilyMember("u3", "Маша Сидорова", "М", Palette.LAVENDER, "Ребёнок", "8 лет", null, 18, you = false),
    FamilyMember("u4", "Петя Сидоров", "П", Palette.OCHRE, "Ребёнок", "5 лет", null, 6, you = false),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyMembersScreen(
    navigateBack: () -> Unit,
) {
    val spacing = LocalCozySpacing.current
    val shapes = LocalCozyShapes.current
    var showInviteSheet by remember { mutableStateOf(false) }
    var memberSheet by remember { mutableStateOf<FamilyMember?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val memberSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
        ) {
            CozyTopBar(
                title = "Семья Сидоровых",
                onBack = navigateBack,
                action = {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable { showInviteSheet = true },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "+",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                },
            )

            LazyColumn(
                contentPadding = PaddingValues(
                    start = spacing.lg, end = spacing.lg,
                    top = spacing.sm, bottom = spacing.xxxl,
                ),
                verticalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                items(mockMembers, key = { it.id }) { member ->
                    MemberRow(member = member, onClick = { memberSheet = member })
                }

                item {
                    Spacer(Modifier.height(spacing.xs))
                    InviteCodeCard(onShare = { showInviteSheet = true })
                }
            }
        }
    }

    if (showInviteSheet) {
        ModalBottomSheet(
            onDismissRequest = { showInviteSheet = false },
            sheetState = sheetState,
            shape = shapes.sheet,
        ) {
            InviteCodeSheet(onDismiss = { showInviteSheet = false })
        }
    }

    memberSheet?.let { m ->
        ModalBottomSheet(
            onDismissRequest = { memberSheet = null },
            sheetState = memberSheetState,
            shape = shapes.sheet,
        ) {
            MemberActionsSheet(member = m, onDismiss = { memberSheet = null })
        }
    }
}

@Composable
private fun MemberRow(member: FamilyMember, onClick: () -> Unit) {
    val spacing = LocalCozySpacing.current
    val extras = LocalCozyExtraColors.current
    CozyCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        bordered = true,
        contentPadding = spacing.lg,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            CozyAvatar(letter = member.initials, palette = member.palette, size = 52.dp)
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                    Text(
                        member.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    if (member.you) {
                        CozyChip(
                            label = "ВЫ",
                            selected = true,
                        )
                    }
                }
                Spacer(Modifier.height(spacing.xxs))
                val emailPart = member.email ?: "без email"
                Text(
                    "${member.roleLabel} · ${member.ageLabel} · $emailPart",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(spacing.xxs))
                Text(
                    "✓ ${member.done} задач выполнено",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                "⋯",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = extras.textTer,
                modifier = Modifier.padding(start = spacing.xs),
            )
        }
    }
}

@Composable
private fun InviteCodeCard(onShare: () -> Unit) {
    val spacing = LocalCozySpacing.current
    CozyCard(
        modifier = Modifier.fillMaxWidth(),
        background = MaterialTheme.colorScheme.primaryContainer,
        contentPadding = spacing.lg,
        onClick = onShare,
    ) {
        Column {
            Text(
                "КОД ПРИГЛАШЕНИЯ",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp,
            )
            Spacer(Modifier.height(spacing.sm))
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "HOME42",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = 4.sp,
                    modifier = Modifier.weight(1f),
                )
                Box(
                    modifier = Modifier
                        .clip(LocalCozyShapes.current.button)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable(onClick = onShare)
                        .padding(horizontal = spacing.md, vertical = spacing.xs),
                ) {
                    Text(
                        "Поделиться",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        }
    }
}

@Composable
private fun InviteCodeSheet(onDismiss: () -> Unit) {
    val spacing = LocalCozySpacing.current
    val shapes = LocalCozyShapes.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.xxl)
            .padding(bottom = spacing.huge),
    ) {
        Text(
            "Код приглашения",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = spacing.xl),
        )

        CozyCard(
            modifier = Modifier.fillMaxWidth(),
            background = MaterialTheme.colorScheme.surfaceVariant,
            radius = 22.dp,
            contentPadding = spacing.xl,
            bordered = true,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    "FAMILY42",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = 8.sp,
                )
                Spacer(Modifier.height(spacing.xs))
                Text(
                    "Действителен 7 дней",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(spacing.xl))

        Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(shapes.button)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(onClick = onDismiss)
                    .padding(vertical = spacing.md),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Скопировать",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(shapes.button)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable(onClick = onDismiss)
                    .padding(vertical = spacing.md),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Поделиться",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

@Composable
private fun MemberActionsSheet(member: FamilyMember, onDismiss: () -> Unit) {
    val spacing = LocalCozySpacing.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.xxl)
            .padding(bottom = spacing.huge),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.md),
            modifier = Modifier.padding(bottom = spacing.lg),
        ) {
            CozyAvatar(letter = member.initials, palette = member.palette, size = 48.dp)
            Column {
                Text(
                    member.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    "${member.roleLabel} · ${member.ageLabel}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        listOf(
            "✏️" to "Изменить роль",
            "🔔" to "Настройки уведомлений",
            "🚪" to "Удалить из семьи",
        ).forEachIndexed { i, (icon, label) ->
            val danger = i == 2
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onDismiss)
                    .padding(vertical = spacing.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.md),
            ) {
                Text(icon, fontSize = 18.sp, modifier = Modifier.width(24.dp))
                Text(
                    label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (danger) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
