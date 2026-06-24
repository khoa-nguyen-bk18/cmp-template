package com.devindie.cmptemplate.domain.usecase.onboarding

import com.devindie.cmptemplate.domain.fake.FakeOnboardingRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CompleteOnboardingUseCaseTest {
    @Test
    fun invoke_marksOnboardingCompleted() = runTest {
        val repository = FakeOnboardingRepository(completed = false)
        val useCase = CompleteOnboardingUseCase(repository)

        useCase()

        assertEquals(1, repository.markCompletedCallCount)
        assertTrue(repository.hasCompleted())
    }
}
