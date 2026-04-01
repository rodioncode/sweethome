package com.jetbrains.kmpapp.data.groups

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GroupsRepository(
    private val groupsApi: GroupsApi,
) {
    private val _groups = MutableStateFlow<List<Group>>(emptyList())
    val groups: StateFlow<List<Group>> = _groups.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    suspend fun loadGroups() {
        groupsApi.getGroups()
            .onSuccess { _groups.value = it }
            .onFailure { _error.value = it.message }
    }

    suspend fun createGroup(name: String): Result<Group> {
        val result = groupsApi.createGroup(CreateGroupRequest(name = name))
        result.onSuccess { _groups.value = listOf(it) + _groups.value }
        result.onFailure { _error.value = it.message }
        return result
    }

    suspend fun createInvite(groupId: String): Result<Invite> {
        val result = groupsApi.createInvite(groupId)
        result.onFailure { _error.value = it.message }
        return result
    }

    suspend fun acceptInvite(token: String): Result<AcceptInviteResponse> {
        val result = groupsApi.acceptInvite(token)
        result.onSuccess { loadGroups() }
        result.onFailure { if (it !is EmailRequiredException && it !is InvalidInviteException) _error.value = it.message }
        return result
    }

    suspend fun deleteGroup(groupId: String): Result<Unit> {
        val result = groupsApi.deleteGroup(groupId)
        result.onSuccess { _groups.value = _groups.value.filter { it.id != groupId } }
        result.onFailure { _error.value = it.message }
        return result
    }

    suspend fun removeMember(groupId: String, userId: String, currentUserId: String?): Result<Unit> {
        val result = groupsApi.removeMember(groupId, userId)
        result.onSuccess {
            if (userId == currentUserId) {
                // Self-leave: remove the group entirely
                _groups.value = _groups.value.filter { it.id != groupId }
            } else {
                // Remove member from in-memory list
                _groups.value = _groups.value.map { group ->
                    if (group.id == groupId) {
                        group.copy(members = group.members?.filter { it.userId != userId })
                    } else group
                }
            }
        }
        result.onFailure {
            if (it !is OwnerCannotLeaveException) _error.value = it.message
        }
        return result
    }

    suspend fun transferOwnership(groupId: String, userId: String): Result<Unit> {
        val result = groupsApi.transferOwnership(groupId, TransferOwnershipRequest(userId = userId))
        result.onSuccess {
            _groups.value = _groups.value.map { group ->
                if (group.id == groupId) group.copy(role = "member") else group
            }
        }
        result.onFailure { _error.value = it.message }
        return result
    }

    fun clearError() {
        _error.value = null
    }
}
