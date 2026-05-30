package com.devindie.vaulty.data.vault.sync

import android.content.Context

/** Persists encoded metadata snapshots in SharedPreferences. */
class AndroidVaultFolderSnapshotStore(context: Context) : VaultFolderSnapshotStore {
    private val prefs =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override suspend fun load(storageKey: String): VaultFolderMetadataSnapshot? {
        val encoded = prefs.getString(prefKey(storageKey), null) ?: return null
        return decodeVaultFolderMetadataSnapshot(encoded)
    }

    override suspend fun save(storageKey: String, snapshot: VaultFolderMetadataSnapshot) {
        prefs.edit().putString(prefKey(storageKey), snapshot.encode()).apply()
    }

    override suspend fun clear(storageKey: String) {
        prefs.edit().remove(prefKey(storageKey)).apply()
    }

    private fun prefKey(storageKey: String): String = KEY_PREFIX + storageKey.hashCode()

    companion object {
        private const val PREFS_NAME = "vault_sync_snapshots"
        private const val KEY_PREFIX = "snapshot:"
    }
}
