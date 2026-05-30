package com.devindie.vaulty.data.vault.sync

import com.devindie.vaulty.domain.repository.VaultSyncSettingsRepository
import kotlinx.coroutines.flow.Flow

/** Implements [VaultSyncSettingsRepository] via [VaultSyncSettingsDataSource]. */
class VaultSyncSettingsRepositoryImpl(private val dataSource: VaultSyncSettingsDataSource) :
    VaultSyncSettingsRepository {
    override fun observeEnabled(): Flow<Boolean> = dataSource.observeEnabled()

    override suspend fun setEnabled(enabled: Boolean): Result<Unit> = runCatching {
        dataSource.setEnabled(enabled)
    }
}
