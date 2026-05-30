package com.devindie.vaulty.data.vault.sync

import kotlinx.coroutines.flow.Flow

/**
 * Platform persistence for the background sync enabled flag.
 *
 * **Implemented by:** [com.devindie.vaulty.data.vault.sync.AndroidVaultSyncSettingsDataSource],
 * [com.devindie.vaulty.data.vault.sync.IosVaultSyncSettingsDataSource].
 */
interface VaultSyncSettingsDataSource {
    fun observeEnabled(): Flow<Boolean>

    suspend fun setEnabled(enabled: Boolean)
}
