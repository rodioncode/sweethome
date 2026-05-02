package com.jetbrains.kmpapp.data.profile

interface ProfileApi {
    suspend fun getProfile(): Result<UserProfile>
    suspend fun updateProfile(request: UpdateProfileRequest): Result<UserProfile>
    suspend fun updateAvatar(request: UpdateAvatarRequest): Result<UserProfile>
    suspend fun getActivity(): Result<List<ProfileActivityEvent>>
    suspend fun getNotificationPreferences(): Result<List<NotificationPreference>>
    suspend fun updateNotificationPreference(request: UpdateNotificationPreferenceRequest): Result<Unit>
    suspend fun deleteAccount(): Result<Unit>
}

class TooManyRequestsException : Exception("rate_limited")
