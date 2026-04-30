package com.jetbrains.kmpapp.data.groups

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GroupsRepository(
    private val groupsApi: GroupsApi,
) {
    private val _groups = MutableStateFlow<List<Group>>(emptyList())
    val groups: StateFlow<List<Group>> = _groups.asStateFlow()

    private val _members = MutableStateFlow<List<GroupMember>>(emptyList())
    val members: StateFlow<List<GroupMember>> = _members.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    suspend fun loadGroups() {
        groupsApi.getWorkspaces()
            .onSuccess { _groups.value = it }
            .onFailure { _error.value = it.message }
    }

    suspend fun loadWorkspaceMembers(workspaceId: String) {
        groupsApi.getWorkspaceMembers(workspaceId)
            .onSuccess { _members.value = it }
            .onFailure { _error.value = it.message }
    }

    suspend fun createGroup(title: String, type: String = "group", icon: String? = null): Result<Group> {
        val result = groupsApi.createWorkspace(CreateWorkspaceRequest(title = title, type = type, icon = icon))
        result.onSuccess { _groups.value = listOf(it) + _groups.value }
        result.onFailure { _error.value = it.message }
        return result
    }

    suspend fun createInvite(workspaceId: String): Result<Invite> {
        val result = groupsApi.createInviteCode(workspaceId)
        result.onFailure { _error.value = it.message }
        return result
    }

    suspend fun joinByCode(token: String): Result<Group> {
        val result = groupsApi.joinByCode(token)
        result.onSuccess { workspace ->
            if (_groups.value.none { it.id == workspace.id }) {
                _groups.value = _groups.value + workspace
            }
        }
        result.onFailure {
            if (it !is EmailRequiredException && it !is InvalidInviteException && it !is InviteExpiredException) {
                _error.value = it.message
            }
        }
        return result
    }

    suspend fun deleteGroup(groupId: String): Result<Unit> {
        val result = groupsApi.deleteWorkspace(groupId)
        result.onSuccess { _groups.value = _groups.value.filter { it.id != groupId } }
        result.onFailure { _error.value = it.message }
        return result
    }

    suspend fun removeMember(groupId: String, userId: String, currentUserId: String?): Result<Unit> {
        val result = groupsApi.removeMember(groupId, userId)
        result.onSuccess {
            if (userId == currentUserId) {
                _groups.value = _groups.value.filter { it.id != groupId }
            } else {
                _members.value = _members.value.filter { it.userId != userId }
            }
        }
        result.onFailure {
            if (it !is OwnerCannotLeaveException) _error.value = it.message
        }
        return result
    }

    suspend fun transferOwnership(groupId: String, userId: String): Result<Unit> {
        val result = groupsApi.transferOwnership(groupId, TransferOwnershipRequest(toUserId = userId))
        result.onSuccess {
            _groups.value = _groups.value.map { group ->
                if (group.id == groupId) group.copy(role = "member") else group
            }
        }
        result.onFailure { _error.value = it.message }
        return result
    }

    fun clearAll() {
        _groups.value = emptyList()
        _members.value = emptyList()
        _error.value = null
    }

    fun clearError() {
        _error.value = null
    }
}
