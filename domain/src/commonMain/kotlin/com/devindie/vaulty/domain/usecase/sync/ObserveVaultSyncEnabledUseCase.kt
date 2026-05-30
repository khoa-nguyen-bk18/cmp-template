package com.devindie.vaulty.domain.usecase.sync

import com.devindie.vaulty.domain.repository.VaultSyncSettingsRepository
import kotlinx.coroutines.flow.Flow

/**
 * Observes whether background vault sync is enabled (default off until persisted).
 *
 * **Flow:** [com.devindie.vaulty.screens.settings.SettingsViewModel],
 * [com.devindie.vaulty.sync.VaultSyncCoordinator] → this → [VaultSyncSettingsRepository].
 */
class ObserveVaultSyncEnabledUseCase(private val repository: VaultSyncSettingsRepository) {
    operator fun invoke(): Flow<Boolean> = repository.observeEnabled()
}
