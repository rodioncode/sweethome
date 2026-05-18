package com.jetbrains.kmpapp.data.preferences

import platform.Foundation.NSUserDefaults

actual fun createLocalPreferences(platformContext: Any?): LocalPreferences = IosLocalPreferences()

private class IosLocalPreferences : LocalPreferences {
    private val defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults

    override fun getSelectedWorkspaceId(): String? = defaults.stringForKey(KEY_SELECTED_WORKSPACE)
    override fun setSelectedWorkspaceId(id: String?) {
        if (id == null) defaults.removeObjectForKey(KEY_SELECTED_WORKSPACE)
        else defaults.setObject(id, KEY_SELECTED_WORKSPACE)
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
        listOf(
            KEY_SELECTED_WORKSPACE,
            KEY_PINNED_GROUPS,
            KEY_MUTED_GROUPS,
            KEY_ARCHIVED_GROUPS,
            KEY_PINNED_LISTS,
        ).forEach { defaults.removeObjectForKey(it) }
    }

    @Suppress("UNCHECKED_CAST")
    private fun readSet(key: String): Set<String> =
        (defaults.arrayForKey(key) as? List<String>)?.toSet() ?: emptySet()

    private fun writeSet(key: String, ids: Set<String>) {
        defaults.setObject(ids.toList(), key)
    }

    companion object {
        private const val KEY_SELECTED_WORKSPACE = "selected_workspace_id"
        private const val KEY_PINNED_GROUPS = "pinned_group_ids"
        private const val KEY_MUTED_GROUPS = "muted_group_ids"
        private const val KEY_ARCHIVED_GROUPS = "archived_group_ids"
        private const val KEY_PINNED_LISTS = "pinned_list_ids"
    }
}
