package com.devindie.cmptemplate.domain.usecase.startup

import com.devindie.cmptemplate.domain.fake.FakeAppStartupRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InitializeAppUseCaseTest {
    @Test
    fun invoke_propagatesSuccessFromRepository() = runTest {
        val repository = FakeAppStartupRepository()
        val useCase = InitializeAppUseCase(repository)

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(1, repository.ensureReadyCallCount)
    }

    @Test
    fun invoke_propagatesFailureFromRepository() = runTest {
        val expectedFailure = Result.failure<Unit>(IllegalStateException("startup failed"))
        val repository =
            FakeAppStartupRepository().apply {
                setResult(expectedFailure)
            }
        val useCase = InitializeAppUseCase(repository)

        val result = useCase()

        assertEquals(expectedFailure, result)
        assertEquals(1, repository.ensureReadyCallCount)
    }
}
