package com.devindie.vaulty.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Persists whether background vault sync is enabled.
 *
 * **Upstream:** [com.devindie.vaulty.domain.usecase.sync.ObserveVaultSyncEnabledUseCase],
 * [com.devindie.vaulty.domain.usecase.sync.SetVaultSyncEnabledUseCase].
 * **Downstream:** [com.devindie.vaulty.data.vault.sync.VaultSyncSettingsRepositoryImpl].
 */
interface VaultSyncSettingsRepository {
    fun observeEnabled(): Flow<Boolean>

    suspend fun setEnabled(enabled: Boolean): Result<Unit>
}
