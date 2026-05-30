package com.devindie.vaulty.data.vault.sync

import platform.Foundation.NSUserDefaults

/** Persists encoded metadata snapshots in [NSUserDefaults]. */
class IosVaultFolderSnapshotStore : VaultFolderSnapshotStore {
    private val defaults = NSUserDefaults.standardUserDefaults

    override suspend fun load(storageKey: String): VaultFolderMetadataSnapshot? {
        val encoded = defaults.stringForKey(prefKey(storageKey)) ?: return null
        return decodeVaultFolderMetadataSnapshot(encoded)
    }

    override suspend fun save(storageKey: String, snapshot: VaultFolderMetadataSnapshot) {
        defaults.setObject(snapshot.encode(), prefKey(storageKey))
    }

    override suspend fun clear(storageKey: String) {
        defaults.removeObjectForKey(prefKey(storageKey))
    }

    private fun prefKey(storageKey: String): String = "$KEY_PREFIX${storageKey.hashCode()}"

    companion object {
        private const val KEY_PREFIX = "vault_sync_snapshot:"
    }
}
