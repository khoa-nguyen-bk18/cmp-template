package com.devindie.vaulty.data.vault.sync

/**
 * Platform port to schedule OS background vault sync.
 *
 * **Implemented by:** [AndroidVaultBackgroundSyncSchedulerDataSource],
 * [IosVaultBackgroundSyncSchedulerDataSource].
 */
interface VaultBackgroundSyncSchedulerDataSource {
    suspend fun schedule()

    suspend fun cancel()
}
