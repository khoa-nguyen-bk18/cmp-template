package com.devindie.vaulty.data.vault.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/** Schedules [VaultSyncWorker] via WorkManager (15-minute minimum interval). */
class AndroidVaultBackgroundSyncSchedulerDataSource(private val context: Context) :
    VaultBackgroundSyncSchedulerDataSource {
    override suspend fun schedule() {
        val constraints =
            Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()

        val request =
            PeriodicWorkRequestBuilder<VaultSyncWorker>(
                repeatInterval = PERIOD_MINUTES,
                repeatIntervalTimeUnit = TimeUnit.MINUTES,
            )
                .setConstraints(constraints)
                .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
    }

    override suspend fun cancel() {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
    }

    companion object {
        const val UNIQUE_WORK_NAME = "vault_background_sync"
        private const val PERIOD_MINUTES = 15L
    }
}
