package com.jetbrains.kmpapp.data.groups

import kotlinx.serialization.Serializable

@Serializable
data class GroupMember(
    val userId: String,
    val displayName: String,
    val role: String, // "owner" | "admin" | "member"
)

@Serializable
data class Group(
    val id: String,
    val name: String,
    val createdBy: String,
    val createdAt: String,
    val role: String,                        // "owner" | "member" | "admin" — роль текущего пользователя
    val type: String = "group",              // "group" | "family"
    val members: List<GroupMember>? = null,  // опционально — если сервер вернёт
)

@Serializable
data class GroupsWrapper(
    val groups: List<Group>,
)

@Serializable
data class CreateGroupRequest(
    val name: String,
    val type: String = "group",             // "group" | "family"
)

@Serializable
data class TransferOwnershipRequest(
    val userId: String,
)

@Serializable
data class Invite(
    val token: String,
    val expiresAt: String,
)

@Serializable
data class AcceptInviteResponse(
    val groupId: String,
)

class EmailRequiredException : Exception("email_required")
class OwnerCannotLeaveException : Exception("owner_cannot_leave")
class InvalidInviteException : Exception("invalid_invite")
