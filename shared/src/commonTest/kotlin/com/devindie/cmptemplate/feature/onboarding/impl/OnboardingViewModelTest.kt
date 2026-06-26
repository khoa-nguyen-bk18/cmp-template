package com.devindie.cmptemplate.feature.onboarding.impl

import com.devindie.cmptemplate.domain.usecase.onboarding.CompleteOnboardingUseCase
import com.devindie.cmptemplate.fake.FakeOnboardingRepository
import com.devindie.cmptemplate.test.advanceMainUntilIdle
import com.devindie.cmptemplate.test.runViewModelTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OnboardingViewModelTest {
    @Test
    fun onNextClick_advancesPageIndex() = runViewModelTest {
        val viewModel = OnboardingViewModel(CompleteOnboardingUseCase(FakeOnboardingRepository()))

        viewModel.onNextClick()
        assertEquals(1, viewModel.uiState.value.currentPageIndex)

        viewModel.onNextClick()
        assertEquals(2, viewModel.uiState.value.currentPageIndex)
    }

    @Test
    fun onNextClick_doesNotAdvancePastLastPage() = runViewModelTest {
        val viewModel = OnboardingViewModel(CompleteOnboardingUseCase(FakeOnboardingRepository()))
        viewModel.onPageChanged(2)

        viewModel.onNextClick()

        assertEquals(2, viewModel.uiState.value.currentPageIndex)
        assertFalse(viewModel.uiState.value.isStartupComplete)
    }

    @Test
    fun onGetStartedClick_marksCompleteAndNavigates() = runViewModelTest {
        val repository = FakeOnboardingRepository()
        val viewModel = OnboardingViewModel(CompleteOnboardingUseCase(repository))
        viewModel.onPageChanged(2)

        viewModel.onGetStartedClick()
        advanceMainUntilIdle()

        assertEquals(1, repository.markCompletedCallCount)
        assertTrue(viewModel.uiState.value.isStartupComplete)
    }

    @Test
    fun onPageChanged_updatesCurrentPageIndex() = runViewModelTest {
        val viewModel = OnboardingViewModel(CompleteOnboardingUseCase(FakeOnboardingRepository()))

        viewModel.onPageChanged(1)

        assertEquals(1, viewModel.uiState.value.currentPageIndex)
    }
}
