package com.devindie.vaulty.domain.repository

/**
 * Schedules or cancels OS-level background sync when the app is not running.
 *
 * **Upstream:** [com.devindie.vaulty.domain.usecase.sync.ScheduleVaultBackgroundSyncUseCase],
 * [com.devindie.vaulty.domain.usecase.sync.CancelVaultBackgroundSyncUseCase].
 * **Downstream:** [com.devindie.vaulty.data.vault.sync.VaultBackgroundSyncSchedulerRepositoryImpl].
 */
interface VaultBackgroundSyncSchedulerRepository {
    suspend fun schedule(): Result<Unit>

    suspend fun cancel(): Result<Unit>
}
