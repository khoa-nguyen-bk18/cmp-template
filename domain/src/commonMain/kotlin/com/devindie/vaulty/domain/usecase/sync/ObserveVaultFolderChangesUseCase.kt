package com.devindie.vaulty.domain.usecase.sync

import com.devindie.vaulty.domain.repository.VaultFolderWatchRepository
import kotlinx.coroutines.flow.Flow

/**
 * Emits when the vault folder at [storageKey] may have changed on disk.
 *
 * **Flow:** [com.devindie.vaulty.sync.VaultSyncCoordinator] → this →
 * [VaultFolderWatchRepository].
 */
class ObserveVaultFolderChangesUseCase(private val repository: VaultFolderWatchRepository) {
    operator fun invoke(storageKey: String): Flow<Unit> = repository.observeChanges(storageKey)
}
