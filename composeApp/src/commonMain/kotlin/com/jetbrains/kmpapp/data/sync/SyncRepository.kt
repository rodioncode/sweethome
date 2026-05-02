package com.jetbrains.kmpapp.data.sync

import com.jetbrains.kmpapp.auth.TokenStorage
import com.jetbrains.kmpapp.data.lists.ListsRepository
import com.jetbrains.kmpapp.data.lists.ListsStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val EPOCH = "1970-01-01T00:00:00Z"

class SyncRepository(
    private val syncApi: SyncApi,
    private val listsStorage: ListsStorage,
    private val listsRepository: ListsRepository,
    private val tokenStorage: TokenStorage,
) {
    private val _memberBalances = MutableStateFlow<List<MemberBalance>>(emptyList())
    val memberBalances: StateFlow<List<MemberBalance>> = _memberBalances.asStateFlow()

    fun balanceFor(workspaceId: String): MemberBalance? =
        _memberBalances.value.firstOrNull { it.workspaceId == workspaceId }

    suspend fun sync(): Result<Unit> = runCatching {
        val since = tokenStorage.getSyncTimestamp() ?: EPOCH
        val response = syncApi.sync(since).getOrThrow()

        val updated = response.items.filter { !it.deleted }.map { it.toTodoItem() }
        val deletedIds = response.items.filter { it.deleted }.map { it.id }

        listsStorage.applySync(updated, deletedIds)
        tokenStorage.saveSyncTimestamp(response.timestamp)
        _memberBalances.value = response.memberBalances

        listsRepository.reloadListsFromStorage()
    }

    fun clear() {
        _memberBalances.value = emptyList()
    }
}
