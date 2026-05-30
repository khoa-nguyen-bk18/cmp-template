package com.devindie.vaulty.domain.usecase.sync

import com.devindie.vaulty.domain.repository.VaultBackgroundSyncSchedulerRepository
import com.devindie.vaulty.domain.usecase.UseCaseNoParams

/**
 * Registers OS background sync work (WorkManager / BG refresh).
 *
 * **Flow:** [com.devindie.vaulty.sync.VaultSyncCoordinator] → this →
 * [VaultBackgroundSyncSchedulerRepository].
 */
class ScheduleVaultBackgroundSyncUseCase(private val repository: VaultBackgroundSyncSchedulerRepository) :
    UseCaseNoParams<Result<Unit>> {
    override suspend fun invoke(): Result<Unit> = repository.schedule()
}
