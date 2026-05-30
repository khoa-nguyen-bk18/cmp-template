package com.devindie.vaulty.domain.usecase.sync

import com.devindie.vaulty.domain.repository.VaultBackgroundSyncSchedulerRepository
import com.devindie.vaulty.domain.usecase.UseCaseNoParams

/**
 * Cancels OS background sync work.
 *
 * **Flow:** [com.devindie.vaulty.sync.VaultSyncCoordinator] → this →
 * [VaultBackgroundSyncSchedulerRepository].
 */
class CancelVaultBackgroundSyncUseCase(private val repository: VaultBackgroundSyncSchedulerRepository) :
    UseCaseNoParams<Result<Unit>> {
    override suspend fun invoke(): Result<Unit> = repository.cancel()
}
