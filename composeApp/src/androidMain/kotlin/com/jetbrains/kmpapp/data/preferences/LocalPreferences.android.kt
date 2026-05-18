package com.jetbrains.kmpapp.data.preferences

import android.content.Context
import android.content.SharedPreferences

actual fun createLocalPreferences(platformContext: Any?): LocalPreferences =
    AndroidLocalPreferences(platformContext as Context)

private class AndroidLocalPreferences(context: Context) : LocalPreferences {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun getSelectedWorkspaceId(): String? = prefs.getString(KEY_SELECTED_WORKSPACE, null)
    override fun setSelectedWorkspaceId(id: String?) {
        prefs.edit().apply {
            if (id == null) remove(KEY_SELECTED_WORKSPACE) else putString(KEY_SELECTED_WORKSPACE, id)
            apply()
        }
    }

    override fun getPinnedGroupIds(): Set<String> = readSet(KEY_PINNED_GROUPS)
    override fun setPinnedGroupIds(ids: Set<String>) = writeSet(KEY_PINNED_GROUPS, ids)

    override fun getMutedGroupIds(): Set<String> = readSet(KEY_MUTED_GROUPS)
    override fun setMutedGroupIds(ids: Set<String>) = writeSet(KEY_MUTED_GROUPS, ids)

    override fun getArchivedGroupIds(): Set<String> = readSet(KEY_ARCHIVED_GROUPS)
    override fun setArchivedGroupIds(ids: Set<String>) = writeSet(KEY_ARCHIVED_GROUPS, ids)

    override fun getPinnedListIds(): Set<String> = readSet(KEY_PINNED_LISTS)
    override fun setPinnedListIds(ids: Set<String>) = writeSet(KEY_PINNED_LISTS, ids)

    override fun clear() {
        prefs.edit().clear().apply()
    }

    private fun readSet(key: String): Set<String> =
        prefs.getStringSet(key, null)?.toSet() ?: emptySet()

    private fun writeSet(key: String, ids: Set<String>) {
        prefs.edit().putStringSet(key, ids).apply()
    }

    companion object {
        private const val PREFS_NAME = "sweethome_local_prefs"
        private const val KEY_SELECTED_WORKSPACE = "selected_workspace_id"
        private const val KEY_PINNED_GROUPS = "pinned_group_ids"
        private const val KEY_MUTED_GROUPS = "muted_group_ids"
        private const val KEY_ARCHIVED_GROUPS = "archived_group_ids"
        private const val KEY_PINNED_LISTS = "pinned_list_ids"
    }
}
