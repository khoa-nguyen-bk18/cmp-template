package com.devindie.vaulty.data.vault.sync

import com.devindie.vaulty.domain.repository.VaultBackgroundSyncSchedulerRepository

/** Implements [VaultBackgroundSyncSchedulerRepository] via [VaultBackgroundSyncSchedulerDataSource]. */
class VaultBackgroundSyncSchedulerRepositoryImpl(private val scheduler: VaultBackgroundSyncSchedulerDataSource) :
    VaultBackgroundSyncSchedulerRepository {
    override suspend fun schedule(): Result<Unit> = runCatching {
        scheduler.schedule()
    }

    override suspend fun cancel(): Result<Unit> = runCatching {
        scheduler.cancel()
    }
}
