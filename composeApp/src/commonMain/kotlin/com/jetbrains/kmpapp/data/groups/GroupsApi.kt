package com.jetbrains.kmpapp.data.groups

interface GroupsApi {
    suspend fun getGroups(): Result<List<Group>>
    suspend fun createGroup(request: CreateGroupRequest): Result<Group>
    suspend fun createInvite(groupId: String): Result<Invite>
    suspend fun acceptInvite(token: String): Result<AcceptInviteResponse>
    suspend fun deleteGroup(groupId: String): Result<Unit>
    suspend fun removeMember(groupId: String, userId: String): Result<Unit>
    suspend fun transferOwnership(groupId: String, request: TransferOwnershipRequest): Result<Unit>
}
