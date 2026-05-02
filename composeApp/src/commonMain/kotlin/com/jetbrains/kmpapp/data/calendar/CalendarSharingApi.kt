package com.jetbrains.kmpapp.data.calendar

interface CalendarSharingApi {
    suspend fun getSharing(workspaceId: String): Result<CalendarSharing>
    suspend fun putSharing(workspaceId: String, request: PutSharingRequest): Result<CalendarSharing>
    suspend fun rotateToken(workspaceId: String): Result<RotateTokenResponse>
}
