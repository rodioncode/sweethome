package com.jetbrains.kmpapp.data.groups

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GroupsRepository(
    private val groupsApi: GroupsApi,
) {
    private val _groups = MutableStateFlow<List<GroupDTO>>(emptyList())
    val groups: StateFlow<List<GroupDTO>> = _groups.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    suspend fun loadGroups() {
        groupsApi.getGroups()
            .onSuccess { _groups.value = it }
            .onFailure { _error.value = it.message }
    }

    suspend fun createGroup(name: String): Result<GroupDTO> {
        val result = groupsApi.createGroup(CreateGroupRequest(name))
        result.onSuccess { _groups.value = _groups.value + it }
        result.onFailure { _error.value = it.message }
        return result
    }

    suspend fun createInvite(groupId: String): Result<InviteDTO> {
        val result = groupsApi.createInvite(groupId)
        result.onFailure { _error.value = it.message }
        return result
    }

    suspend fun acceptInvite(token: String): Result<AcceptInviteResponse> {
        val result = groupsApi.acceptInvite(token)
        result.onSuccess { loadGroups() }
        result.onFailure { _error.value = it.message }
        return result
    }

    suspend fun transferOwnership(groupId: String, newOwnerId: String): Result<Unit> {
        val result = groupsApi.transferOwnership(groupId, TransferOwnershipRequest(newOwnerId))
        result.onSuccess { loadGroups() }
        result.onFailure { _error.value = it.message }
        return result
    }

    suspend fun deleteGroup(groupId: String): Result<Unit> {
        val result = groupsApi.deleteGroup(groupId)
        result.onSuccess { _groups.value = _groups.value.filter { it.id != groupId } }
        result.onFailure { _error.value = it.message }
        return result
    }

    suspend fun removeMember(groupId: String, userId: String): Result<Unit> {
        val result = groupsApi.removeMember(groupId, userId)
        // После выхода из группы — перезагружаем список (группа могла исчезнуть)
        result.onSuccess { loadGroups() }
        result.onFailure { _error.value = it.message }
        return result
    }

    fun clearError() {
        _error.value = null
    }
}
