package com.jetbrains.kmpapp.screens.family

import androidx.compose.foundation.background
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetbrains.kmpapp.ui.DividerColor
import com.jetbrains.kmpapp.ui.PrimaryGreen
import com.jetbrains.kmpapp.ui.SurfaceVariantCream
import com.jetbrains.kmpapp.ui.SurfaceWhite

private data class FamilyMember(
    val id: String,
    val name: String,
    val initials: String,
    val color: Color,
    val roleLabel: String,
    val roleBg: Color,
    val roleColor: Color,
    val tasks: Int,
    val online: Boolean,
)

private val familyMembers = listOf(
    FamilyMember("u1", "Аня Новикова", "АН", Color(0xFF5B7C5A), "Владелец", Color(0xFFE8F5E8), Color(0xFF3D5C3C), 8, true),
    FamilyMember("u2", "Дима Новиков", "ДН", Color(0xFFE8A87C), "Участник", Color(0xFFF5F5F5), Color(0xFF888888), 5, true),
    FamilyMember("u3", "Соня Новикова", "СН", Color(0xFFAB47BC), "Участник", Color(0xFFF5F5F5), Color(0xFF888888), 3, false),
    FamilyMember("u4", "Гриша Новиков", "ГН", Color(0xFF42A5F5), "Ребёнок", Color(0xFFFFF3E0), Color(0xFFE8A87C), 2, false),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyMembersScreen(
    navigateBack: () -> Unit,
) {
    var showInviteSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(containerColor = SurfaceVariantCream) { paddingValues ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
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
                    "Участники",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = { showInviteSheet = true }) {
                    Text(
                        "+ Добавить",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen,
                    )
                }
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Invite code banner
            item {
                Surface(
                    onClick = { showInviteSheet = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFE8F5E8),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryGreen),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "🔗 Пригласить: FAMILY42",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3D5C3C),
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            "Поделиться",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryGreen,
                        )
                    }
                }
            }

            // Member list
            items(familyMembers.size) { i ->
                val member = familyMembers[i]
                MemberCard(member = member)
            }

            // Invite button
            item {
                Spacer(Modifier.height(2.dp))
                Surface(
                    onClick = { showInviteSheet = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = PrimaryGreen,
                    shadowElevation = 3.dp,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "+ Пригласить участника",
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

    if (showInviteSheet) {
        ModalBottomSheet(
            onDismissRequest = { showInviteSheet = false },
            sheetState = sheetState,
        ) {
            InviteCodeSheet(onDismiss = { showInviteSheet = false })
        }
    }
}

@Composable
private fun MemberCard(member: FamilyMember) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = SurfaceWhite,
        border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor),
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Avatar with online indicator
            Box(modifier = Modifier.size(48.dp)) {
                MemberCircle(
                    displayName = member.initials,
                    color = member.color,
                    size = 48,
                    fontSize = 15,
                )
                if (member.online) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(PrimaryGreen, CircleShape)
                            .background(Color.White, shape = CircleShape)
                            .align(Alignment.BottomEnd),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(PrimaryGreen, CircleShape)
                                .align(Alignment.Center),
                        )
                    }
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    member.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    "${member.tasks} задач",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = member.roleBg,
            ) {
                Text(
                    member.roleLabel,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = member.roleColor,
                )
            }

            Text("›", fontSize = 20.sp, color = DividerColor)
        }
    }
}

@Composable
private fun InviteCodeSheet(onDismiss: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 40.dp),
    ) {
        // Drag handle
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(4.dp)
                .background(DividerColor, RoundedCornerShape(2.dp))
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 20.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Код приглашения",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 20.dp),
        )

        // Big code display
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = SurfaceVariantCream,
            border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    "FAMILY42",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = 8.sp,
                )
                Text(
                    "Действителен 7 дней",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Surface(
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                color = SurfaceVariantCream,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Скопировать", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }
            Surface(
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                color = PrimaryGreen,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Поделиться", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
