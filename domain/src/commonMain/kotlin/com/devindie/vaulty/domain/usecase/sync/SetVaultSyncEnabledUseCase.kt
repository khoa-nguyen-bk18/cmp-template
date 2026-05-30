package com.devindie.vaulty.domain.usecase.sync

import com.devindie.vaulty.domain.repository.VaultSyncSettingsRepository
import com.devindie.vaulty.domain.usecase.UseCase

/**
 * Persists the background vault sync enabled flag.
 *
 * **Flow:** [com.devindie.vaulty.screens.settings.SettingsViewModel] → this →
 * [VaultSyncSettingsRepository].
 */
class SetVaultSyncEnabledUseCase(private val repository: VaultSyncSettingsRepository) :
    UseCase<Boolean, Result<Unit>> {
    override suspend fun invoke(parameters: Boolean): Result<Unit> = repository.setEnabled(parameters)
}
