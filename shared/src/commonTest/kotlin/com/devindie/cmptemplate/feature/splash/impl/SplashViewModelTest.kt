package com.devindie.cmptemplate.feature.splash.impl

import com.devindie.cmptemplate.fake.FakeAppStartupRepository
import com.devindie.cmptemplate.domain.usecase.startup.InitializeAppUseCase
import com.devindie.cmptemplate.test.advanceMainUntilIdle
import com.devindie.cmptemplate.test.runViewModelTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {
    @Test
    fun startup_marksCompleteAfterOneSecondWhenInitIsFast() = runViewModelTest {
        val repository = FakeAppStartupRepository(result = Result.success(Unit))
        val viewModel = SplashViewModel(InitializeAppUseCase(repository))

        advanceTimeBy(SplashViewModel.MIN_DISPLAY_MS)
        advanceMainUntilIdle()

        assertTrue(viewModel.uiState.value.isStartupComplete)
        assertEquals(SplashPhase.Loading, viewModel.uiState.value.phase)
    }

    @Test
    fun startup_waitsForSlowInit() = runViewModelTest {
        val repository =
            FakeAppStartupRepository(
                result = Result.success(Unit),
                initDelayMs = 2_000L,
            )
        val viewModel = SplashViewModel(InitializeAppUseCase(repository))

        advanceTimeBy(SplashViewModel.MIN_DISPLAY_MS)
        assertFalse(viewModel.uiState.value.isStartupComplete)

        advanceTimeBy(1_000L)
        advanceMainUntilIdle()
        assertTrue(viewModel.uiState.value.isStartupComplete)
    }

    @Test
    fun startup_showsErrorAfterMinimumDisplayWhenInitFails() = runViewModelTest {
        val repository =
            FakeAppStartupRepository(
                result = Result.failure(IllegalStateException("db down")),
            )
        val viewModel = SplashViewModel(InitializeAppUseCase(repository))

        advanceTimeBy(SplashViewModel.MIN_DISPLAY_MS)
        advanceMainUntilIdle()

        assertEquals(SplashPhase.Error, viewModel.uiState.value.phase)
        assertEquals("db down", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isStartupComplete)
    }

    @Test
    fun retry_recoversAfterFailure() = runViewModelTest {
        val repository =
            FakeAppStartupRepository(
                result = Result.failure(IllegalStateException("db down")),
            )
        val viewModel = SplashViewModel(InitializeAppUseCase(repository))
        advanceTimeBy(SplashViewModel.MIN_DISPLAY_MS)
        advanceMainUntilIdle()

        repository.setResult(Result.success(Unit))
        viewModel.onRetryClick()
        advanceTimeBy(SplashViewModel.MIN_DISPLAY_MS)
        advanceMainUntilIdle()

        assertTrue(viewModel.uiState.value.isStartupComplete)
        assertEquals(SplashPhase.Loading, viewModel.uiState.value.phase)
    }
}
