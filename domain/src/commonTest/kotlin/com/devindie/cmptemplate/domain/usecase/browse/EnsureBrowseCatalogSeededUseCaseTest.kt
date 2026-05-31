package com.devindie.cmptemplate.domain.usecase.browse

import com.devindie.cmptemplate.domain.fake.FakeBrowseCardRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EnsureBrowseCatalogSeededUseCaseTest {
    @Test
    fun invoke_forwardsSuccessFromRepository() = runTest {
        val repository =
            FakeBrowseCardRepository().apply {
                ensureCatalogSeededResult = Result.success(Unit)
            }
        val useCase = EnsureBrowseCatalogSeededUseCase(repository)

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(1, repository.ensureCatalogSeededCallCount)
    }

    @Test
    fun invoke_forwardsFailureFromRepository() = runTest {
        val repository =
            FakeBrowseCardRepository().apply {
                ensureCatalogSeededResult = Result.failure(IllegalStateException("seed failed"))
            }
        val useCase = EnsureBrowseCatalogSeededUseCase(repository)

        val result = useCase()

        assertTrue(result.isFailure)
        assertEquals("seed failed", result.exceptionOrNull()?.message)
    }
}
