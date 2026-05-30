package com.devindie.vaulty.domain.usecase.index

import com.devindie.vaulty.domain.repository.VaultIndexRepository
import com.devindie.vaulty.domain.usecase.UseCase

/**
 * Starts indexing for the currently selected vault folder.
 *
 * **Flow:** [com.devindie.vaulty.screens.list.DashboardViewModel] → this →
 * [VaultIndexRepository.indexSelectedVault].
 * **Side effects:** Persists files and wiki-links in Room; progress via
 * [ObserveVaultIndexProgressUseCase].
 *
 * @param parameters `true` for a full rebuild (clears vault rows first).
 * @see VaultIndexRepository
 */
class IndexVaultUseCase(private val repository: VaultIndexRepository) : UseCase<Boolean, Result<Unit>> {
    override suspend fun invoke(parameters: Boolean): Result<Unit> =
        repository.indexSelectedVault(fullRebuild = parameters)
}
