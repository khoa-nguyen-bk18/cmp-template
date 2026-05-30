package com.devindie.vaulty.data.vault.sync

import kotlinx.cinterop.ExperimentalForeignApi
import platform.BackgroundTasks.BGAppRefreshTaskRequest
import platform.BackgroundTasks.BGTaskScheduler
import platform.Foundation.NSDate
import platform.Foundation.dateWithTimeIntervalSinceNow

/** Submits and cancels BGAppRefresh tasks for vault sync. */
@OptIn(ExperimentalForeignApi::class)
class IosVaultBackgroundSyncSchedulerDataSource : VaultBackgroundSyncSchedulerDataSource {
    override suspend fun schedule() {
        val request =
            BGAppRefreshTaskRequest(identifier = VaultBackgroundSyncTaskRegistrar.TASK_IDENTIFIER)
        request.earliestBeginDate =
            NSDate.dateWithTimeIntervalSinceNow(REFRESH_INTERVAL_SECONDS)
        BGTaskScheduler.sharedScheduler.submitTaskRequest(request, null)
    }

    override suspend fun cancel() {
        BGTaskScheduler.sharedScheduler.cancelTaskRequestWithIdentifier(
            VaultBackgroundSyncTaskRegistrar.TASK_IDENTIFIER,
        )
    }

    companion object {
        private const val REFRESH_INTERVAL_SECONDS = 15.0 * 60.0
    }
}
