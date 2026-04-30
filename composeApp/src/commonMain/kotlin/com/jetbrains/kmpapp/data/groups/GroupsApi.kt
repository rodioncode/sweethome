package com.jetbrains.kmpapp.data.groups

interface GroupsApi {
    suspend fun getWorkspaces(): Result<List<Group>>
    suspend fun createWorkspace(request: CreateWorkspaceRequest): Result<Group>
    suspend fun getWorkspace(workspaceId: String): Result<Group>
    suspend fun patchWorkspace(workspaceId: String, request: PatchWorkspaceRequest): Result<Group>
    suspend fun deleteWorkspace(workspaceId: String): Result<Unit>
    suspend fun getWorkspaceMembers(workspaceId: String): Result<List<GroupMember>>
    suspend fun removeMember(workspaceId: String, userId: String): Result<Unit>
    suspend fun transferOwnership(workspaceId: String, request: TransferOwnershipRequest): Result<Unit>
    suspend fun createInviteCode(workspaceId: String): Result<Invite>
    suspend fun joinByCode(token: String): Result<Group>
}
