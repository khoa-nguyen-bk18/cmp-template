package com.devindie.vaulty.data.vault.sync

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import platform.BackgroundTasks.BGAppRefreshTask
import platform.BackgroundTasks.BGAppRefreshTaskRequest
import platform.BackgroundTasks.BGTaskScheduler
import platform.Foundation.NSDate
import platform.Foundation.dateWithTimeIntervalSinceNow

/**
 * Registers the iOS BGAppRefresh handler. Call once at app launch ([com.devindie.vaulty.doInitKoin]).
 */
private const val REFRESH_INTERVAL_MINUTES = 15.0
private const val SECONDS_PER_MINUTE = 60.0

@OptIn(ExperimentalForeignApi::class)
object VaultBackgroundSyncTaskRegistrar : KoinComponent {
    const val TASK_IDENTIFIER = "com.devindie.vaulty.vault-sync"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val executor: VaultBackgroundSyncExecutor by inject()

    fun register() {
        BGTaskScheduler.sharedScheduler.registerForTaskWithIdentifier(
            identifier = TASK_IDENTIFIER,
            usingQueue = null,
        ) { task ->
            val refreshTask = task as? BGAppRefreshTask ?: return@registerForTaskWithIdentifier
            handleRefreshTask(refreshTask)
        }
    }

    private fun handleRefreshTask(task: BGAppRefreshTask) {
        scheduleNextRefresh()

        val job =
            scope.launch {
                try {
                    executor.run()
                    task.setTaskCompletedWithSuccess(true)
                } catch (_: Throwable) {
                    task.setTaskCompletedWithSuccess(false)
                }
            }

        task.expirationHandler = {
            job.cancel()
        }
    }

    private fun scheduleNextRefresh() {
        val request = BGAppRefreshTaskRequest(identifier = TASK_IDENTIFIER)
        request.earliestBeginDate =
            NSDate.dateWithTimeIntervalSinceNow(REFRESH_INTERVAL_MINUTES * SECONDS_PER_MINUTE)
        BGTaskScheduler.sharedScheduler.submitTaskRequest(request, null)
    }
}
