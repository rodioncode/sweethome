package com.jetbrains.kmpapp.data.sync

interface SyncApi {
    suspend fun sync(since: String): Result<SyncWrapper>
}
