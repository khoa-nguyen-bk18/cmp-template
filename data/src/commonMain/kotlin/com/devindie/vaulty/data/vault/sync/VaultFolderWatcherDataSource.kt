package com.devindie.vaulty.data.vault.sync

import kotlinx.coroutines.flow.Flow

/**
 * Platform port that emits when vault folder content may have changed.
 *
 * **Implemented by:** [AndroidVaultFolderWatcherDataSource], [IosVaultFolderWatcherDataSource].
 */
fun interface VaultFolderWatcherDataSource {
    fun observeChanges(storageKey: String): Flow<Unit>
}
