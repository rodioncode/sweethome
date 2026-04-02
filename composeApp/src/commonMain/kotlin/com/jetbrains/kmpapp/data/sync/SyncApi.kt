package com.jetbrains.kmpapp.data.sync

interface SyncApi {
    /**
     * GET /sync?since={timestamp}
     * Возвращает все изменения после указанного момента (включая удалённые).
     * @param since RFC3339 timestamp, например "1970-01-01T00:00:00Z" для первой загрузки
     */
    suspend fun sync(since: String): Result<SyncResponse>
}
