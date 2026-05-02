package com.jetbrains.kmpapp.data.groups

import kotlinx.serialization.Serializable

object WorkspaceType {
    const val PERSONAL = "personal"
    const val FAMILY = "family"
    const val GROUP = "group"
    const val WORK = "work"
    const val MENTORING = "mentoring"
}

object WorkspaceRole {
    const val OWNER = "owner"
    const val ADMIN = "admin"
    const val MEMBER = "member"
    const val MENTOR = "mentor"
}

@Serializable
data class GroupMember(
    val userId: String,
    val displayName: String? = null,
    val role: String, // см. WorkspaceRole
    val joinedAt: String? = null,
    val invitedBy: String? = null,
)

@Serializable
data class Group(
    val id: String,
    val title: String,
    val icon: String? = null,
    val ownerId: String? = null,
    val createdAt: String,
    val updatedAt: String? = null,
    val archivedAt: String? = null,
    val role: String,                        // см. WorkspaceRole — роль текущего пользователя
    val type: String = WorkspaceType.GROUP,  // см. WorkspaceType
    val workHoursStart: String? = null,      // "HH:MM", актуально для type=work
    val workHoursEnd: String? = null,
    val workDays: List<String>? = null,      // подмножество mon..sun
)

@Serializable
data class WorkspacesWrapper(
    val workspaces: List<Group>,
)

@Serializable
data class CreateWorkspaceRequest(
    val title: String,
    val type: String = WorkspaceType.GROUP,  // personal создаётся бэком автоматически
    val icon: String? = null,
)

@Serializable
data class PatchWorkspaceRequest(
    val title: String? = null,
    val icon: String? = null,
    val workHoursStart: String? = null,
    val workHoursEnd: String? = null,
    val workDays: List<String>? = null,
)

@Serializable
data class TransferOwnershipRequest(
    val toUserId: String,
)

@Serializable
data class JoinByCodeRequest(
    val token: String,
)

@Serializable
data class Invite(
    val token: String,
    val expiresAt: String,
)

@Serializable
data class WorkspaceMembersWrapper(
    val members: List<GroupMember>,
)

class EmailRequiredException : Exception("email_required")
class OwnerCannotLeaveException : Exception("owner_cannot_leave")
class InvalidInviteException : Exception("invalid_invite")
class InviteExpiredException : Exception("invite_expired")
