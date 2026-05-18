package com.jetbrains.kmpapp.data.preferences

/**
 * Non-sensitive UI preferences (workspace selection, pin/mute/archive overlays).
 *
 * Not for tokens or credentials — those live in [com.jetbrains.kmpapp.auth.TokenStorage].
 */
interface LocalPreferences {
    fun getSelectedWorkspaceId(): String?
    fun setSelectedWorkspaceId(id: String?)

    fun getPinnedGroupIds(): Set<String>
    fun setPinnedGroupIds(ids: Set<String>)

    fun getMutedGroupIds(): Set<String>
    fun setMutedGroupIds(ids: Set<String>)

    fun getArchivedGroupIds(): Set<String>
    fun setArchivedGroupIds(ids: Set<String>)

    fun getPinnedListIds(): Set<String>
    fun setPinnedListIds(ids: Set<String>)

    fun clear()
}

expect fun createLocalPreferences(platformContext: Any?): LocalPreferences
