package com.jetbrains.kmpapp.data.groups

interface GroupsApi {
    suspend fun getGroups(): Result<List<GroupDTO>>
    suspend fun createGroup(request: CreateGroupRequest): Result<GroupDTO>
    suspend fun createInvite(groupId: String): Result<InviteDTO>
    suspend fun acceptInvite(token: String): Result<AcceptInviteResponse>
    suspend fun transferOwnership(groupId: String, request: TransferOwnershipRequest): Result<Unit>
    suspend fun deleteGroup(groupId: String): Result<Unit>
    suspend fun removeMember(groupId: String, userId: String): Result<Unit>
}
