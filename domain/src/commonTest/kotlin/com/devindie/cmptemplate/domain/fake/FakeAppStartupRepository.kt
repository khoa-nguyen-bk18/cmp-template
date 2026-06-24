package com.devindie.cmptemplate.domain.fake

import com.devindie.cmptemplate.domain.repository.AppStartupRepository

class FakeAppStartupRepository(
    private var result: Result<Unit> = Result.success(Unit),
    var ensureReadyCallCount: Int = 0,
) : AppStartupRepository {
    override suspend fun ensureReady(): Result<Unit> {
        ensureReadyCallCount++
        return result
    }

    fun setResult(result: Result<Unit>) {
        this.result = result
    }
}
