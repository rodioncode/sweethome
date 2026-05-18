package com.jetbrains.kmpapp.data.groups

import com.jetbrains.kmpapp.data.preferences.LocalPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock

class GroupsRepository(
    private val groupsApi: GroupsApi,
    private val localPreferences: LocalPreferences,
) {
    // Raw server snapshot. Use [groups] to read overlay-applied list.
    private val _rawGroups = MutableStateFlow<List<Group>>(emptyList())

    private val _pinnedIds = MutableStateFlow(localPreferences.getPinnedGroupIds())
    private val _mutedIds = MutableStateFlow(localPreferences.getMutedGroupIds())
    private val _localArchivedIds = MutableStateFlow(localPreferences.getArchivedGroupIds())

    private val _groups = MutableStateFlow<List<Group>>(emptyList())
    val groups: StateFlow<List<Group>> = _groups.asStateFlow()

    private val _members = MutableStateFlow<List<GroupMember>>(emptyList())
    val members: StateFlow<List<GroupMember>> = _members.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private fun nowIso(): String = Clock.System.now().toString()

    private fun applyOverlay(raw: List<Group>): List<Group> {
        val pinned = _pinnedIds.value
        val muted = _mutedIds.value
        val archived = _localArchivedIds.value
        return raw.map { g ->
            g.copy(
                pinnedAt = if (g.id in pinned) (g.pinnedAt ?: nowIso()) else null,
                mutedAt = if (g.id in muted) (g.mutedAt ?: nowIso()) else null,
                archivedAt = g.archivedAt ?: if (g.id in archived) nowIso() else null,
            )
        }
    }

    private fun publish() {
        _groups.value = applyOverlay(_rawGroups.value)
    }

    suspend fun loadGroups() {
        groupsApi.getWorkspaces()
            .onSuccess {
                _rawGroups.value = it
                publish()
            }
            .onFailure { _error.value = it.message }
    }

    suspend fun loadWorkspaceMembers(workspaceId: String) {
        groupsApi.getWorkspaceMembers(workspaceId)
            .onSuccess { _members.value = it }
            .onFailure { _error.value = it.message }
    }

    suspend fun createGroup(title: String, type: String = "group", icon: String? = null): Result<Group> {
        val result = groupsApi.createWorkspace(CreateWorkspaceRequest(title = title, type = type, icon = icon))
        result.onSuccess {
            _rawGroups.value = listOf(it) + _rawGroups.value
            publish()
        }
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
            if (_rawGroups.value.none { it.id == workspace.id }) {
                _rawGroups.value = _rawGroups.value + workspace
                publish()
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
        result.onSuccess {
            _rawGroups.value = _rawGroups.value.filter { it.id != groupId }
            // Drop local overlays for the removed group too.
            removeFromLocalSets(groupId)
            publish()
        }
        result.onFailure { _error.value = it.message }
        return result
    }

    suspend fun removeMember(groupId: String, userId: String, currentUserId: String?): Result<Unit> {
        val result = groupsApi.removeMember(groupId, userId)
        result.onSuccess {
            if (userId == currentUserId) {
                _rawGroups.value = _rawGroups.value.filter { it.id != groupId }
                removeFromLocalSets(groupId)
                publish()
            } else {
                _members.value = _members.value.filter { it.userId != userId }
            }
        }
        result.onFailure {
            if (it !is OwnerCannotLeaveException) _error.value = it.message
        }
        return result
    }

    suspend fun updateWorkspace(workspaceId: String, request: PatchWorkspaceRequest): Result<Group> {
        val result = groupsApi.patchWorkspace(workspaceId, request)
        result.onSuccess { updated ->
            _rawGroups.value = _rawGroups.value.map { if (it.id == workspaceId) updated else it }
            publish()
        }
        result.onFailure { _error.value = it.message }
        return result
    }

    suspend fun transferOwnership(groupId: String, userId: String): Result<Unit> {
        val result = groupsApi.transferOwnership(groupId, TransferOwnershipRequest(toUserId = userId))
        result.onSuccess {
            _rawGroups.value = _rawGroups.value.map { group ->
                if (group.id == groupId) group.copy(role = "member") else group
            }
            publish()
        }
        result.onFailure { _error.value = it.message }
        return result
    }

    fun togglePinned(groupId: String) {
        val next = _pinnedIds.value.toggle(groupId)
        _pinnedIds.value = next
        localPreferences.setPinnedGroupIds(next)
        publish()
    }

    fun toggleMuted(groupId: String) {
        val next = _mutedIds.value.toggle(groupId)
        _mutedIds.value = next
        localPreferences.setMutedGroupIds(next)
        publish()
    }

    fun setArchived(groupId: String, archived: Boolean) {
        val current = _localArchivedIds.value
        val next = if (archived) current + groupId else current - groupId
        if (next == current) return
        _localArchivedIds.value = next
        localPreferences.setArchivedGroupIds(next)
        publish()
    }

    private fun removeFromLocalSets(groupId: String) {
        if (groupId in _pinnedIds.value) {
            val next = _pinnedIds.value - groupId
            _pinnedIds.value = next
            localPreferences.setPinnedGroupIds(next)
        }
        if (groupId in _mutedIds.value) {
            val next = _mutedIds.value - groupId
            _mutedIds.value = next
            localPreferences.setMutedGroupIds(next)
        }
        if (groupId in _localArchivedIds.value) {
            val next = _localArchivedIds.value - groupId
            _localArchivedIds.value = next
            localPreferences.setArchivedGroupIds(next)
        }
    }

    fun clearAll() {
        _rawGroups.value = emptyList()
        _groups.value = emptyList()
        _members.value = emptyList()
        _error.value = null
    }

    fun clearMembers() {
        _members.value = emptyList()
    }

    fun clearError() {
        _error.value = null
    }
}

private fun Set<String>.toggle(id: String): Set<String> =
    if (id in this) this - id else this + id
