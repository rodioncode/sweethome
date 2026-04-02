package com.jetbrains.kmpapp.data.groups

import kotlinx.serialization.Serializable

@Serializable
data class GroupDTO(
    val id: String,
    val name: String,
    val createdBy: String,
    val createdAt: String,
    val role: String, // "owner" | "member"
)

@Serializable
data class InviteDTO(
    val token: String,
    val expiresAt: String,
)

@Serializable
data class AcceptInviteResponse(
    val groupId: String,
)

@Serializable
data class GroupsWrapper(
    val groups: List<GroupDTO>,
)

@Serializable
data class CreateGroupRequest(
    val name: String,
)

@Serializable
data class TransferOwnershipRequest(
    val userId: String,
)
