package com.devindie.vaulty.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Observes filesystem change signals for a vault folder.
 *
 * **Upstream:** [com.devindie.vaulty.domain.usecase.sync.ObserveVaultFolderChangesUseCase].
 * **Downstream:** [com.devindie.vaulty.data.vault.sync.VaultFolderWatchRepositoryImpl].
 */
fun interface VaultFolderWatchRepository {
    fun observeChanges(storageKey: String): Flow<Unit>
}
