package com.devindie.vaulty.data.vault.sync

import com.devindie.vaulty.domain.repository.VaultFolderWatchRepository
import kotlinx.coroutines.flow.Flow

/** Implements [VaultFolderWatchRepository] via [VaultFolderWatcherDataSource]. */
class VaultFolderWatchRepositoryImpl(private val watcher: VaultFolderWatcherDataSource) : VaultFolderWatchRepository {
    override fun observeChanges(storageKey: String): Flow<Unit> = watcher.observeChanges(storageKey)
}
