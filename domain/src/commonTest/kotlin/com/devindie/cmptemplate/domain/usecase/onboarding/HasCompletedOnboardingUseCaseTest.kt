package com.devindie.cmptemplate.domain.usecase.onboarding

import com.devindie.cmptemplate.domain.fake.FakeOnboardingRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HasCompletedOnboardingUseCaseTest {
    @Test
    fun invoke_returnsFalseWhenNotCompleted() = runTest {
        val repository = FakeOnboardingRepository(completed = false)
        val useCase = HasCompletedOnboardingUseCase(repository)

        assertFalse(useCase())
    }

    @Test
    fun invoke_returnsTrueWhenCompleted() = runTest {
        val repository = FakeOnboardingRepository(completed = true)
        val useCase = HasCompletedOnboardingUseCase(repository)

        assertTrue(useCase())
    }
}
