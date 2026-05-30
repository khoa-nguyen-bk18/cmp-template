package com.devindie.vaulty.domain.usecase.sync

import com.devindie.vaulty.domain.repository.VaultBackgroundSyncSchedulerRepository
import com.devindie.vaulty.domain.repository.VaultSyncSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VaultSyncUseCaseTest {
    @Test
    fun observeVaultSyncEnabled_defaultsToFalse() = runTest {
        val repository = FakeVaultSyncSettingsRepository()
        val useCase = ObserveVaultSyncEnabledUseCase(repository)

        assertFalse(useCase().first())
    }

    @Test
    fun setVaultSyncEnabled_persistsValue() = runTest {
        val repository = FakeVaultSyncSettingsRepository()
        val setUseCase = SetVaultSyncEnabledUseCase(repository)
        val observeUseCase = ObserveVaultSyncEnabledUseCase(repository)

        setUseCase(true)
        assertTrue(observeUseCase().first())

        setUseCase(false)
        assertFalse(observeUseCase().first())
    }

    @Test
    fun scheduleAndCancelBackgroundSync_invokeRepository() = runTest {
        val repository = FakeVaultBackgroundSyncSchedulerRepository()
        val schedule = ScheduleVaultBackgroundSyncUseCase(repository)
        val cancel = CancelVaultBackgroundSyncUseCase(repository)

        schedule()
        cancel()

        assertEquals(1, repository.scheduleCount)
        assertEquals(1, repository.cancelCount)
    }
}

private class FakeVaultSyncSettingsRepository : VaultSyncSettingsRepository {
    private val enabled = MutableStateFlow(false)

    override fun observeEnabled(): Flow<Boolean> = enabled

    override suspend fun setEnabled(enabled: Boolean): Result<Unit> {
        this.enabled.value = enabled
        return Result.success(Unit)
    }
}

private class FakeVaultBackgroundSyncSchedulerRepository : VaultBackgroundSyncSchedulerRepository {
    var scheduleCount = 0
    var cancelCount = 0

    override suspend fun schedule(): Result<Unit> {
        scheduleCount++
        return Result.success(Unit)
    }

    override suspend fun cancel(): Result<Unit> {
        cancelCount++
        return Result.success(Unit)
    }
}
