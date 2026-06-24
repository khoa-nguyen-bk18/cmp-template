package com.devindie.cmptemplate.fake

import com.devindie.cmptemplate.domain.repository.AppStartupRepository
import kotlinx.coroutines.delay

class FakeAppStartupRepository(
    private var result: Result<Unit> = Result.success(Unit),
    var ensureReadyCallCount: Int = 0,
    var initDelayMs: Long = 0L,
) : AppStartupRepository {
    override suspend fun ensureReady(): Result<Unit> {
        ensureReadyCallCount++
        if (initDelayMs > 0) delay(initDelayMs)
        return result
    }

    fun setResult(result: Result<Unit>) {
        this.result = result
    }
}
