package com.devindie.vaulty.data.vault.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Periodic WorkManager job: diff vault metadata and run incremental index when needed.
 *
 * Scheduled by [AndroidVaultBackgroundSyncSchedulerDataSource].
 */
class VaultSyncWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params),
    KoinComponent {
    private val executor: VaultBackgroundSyncExecutor by inject()

    override suspend fun doWork(): Result = when (val outcome = executor.run()) {
        is VaultBackgroundSyncResult.Indexed -> Result.success()
        is VaultBackgroundSyncResult.Skipped -> Result.success()
        is VaultBackgroundSyncResult.Failed -> Result.retry()
    }
}
