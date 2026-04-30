package com.jetbrains.kmpapp.data.groups

import kotlinx.serialization.Serializable

@Serializable
data class GroupMember(
    val userId: String,
    val displayName: String? = null,
    val role: String, // "owner" | "admin" | "member"
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
    val role: String,                        // "owner" | "member" | "admin" — роль текущего пользователя
    val type: String = "group",              // "personal" | "group" | "family" | "mentoring"
)

@Serializable
data class WorkspacesWrapper(
    val workspaces: List<Group>,
)

@Serializable
data class CreateWorkspaceRequest(
    val title: String,
    val type: String = "group",             // "group" | "family" | "mentoring"
    val icon: String? = null,
)

@Serializable
data class PatchWorkspaceRequest(
    val title: String? = null,
    val icon: String? = null,
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
