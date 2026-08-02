package com.devindie.cmptemplate.feature.splash.impl

import com.devindie.cmptemplate.domain.usecase.startup.InitializeAppUseCase
import com.devindie.cmptemplate.domain.usecase.user.GetUserSessionUseCase
import com.devindie.cmptemplate.domain.model.user.UserSession
import com.devindie.cmptemplate.fake.FakeAppStartupRepository
import com.devindie.cmptemplate.fake.FakeUserRepository
import com.devindie.cmptemplate.test.advanceMainUntilIdle
import com.devindie.cmptemplate.test.runViewModelTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {
    @Test
    fun startup_navigatesToMainAfterOneSecondWhenInitIsFast() = runViewModelTest {
        val viewModel = createViewModel(hasSession = true)

        advanceTimeBy(SplashViewModel.MIN_DISPLAY_MS)
        advanceMainUntilIdle()

        assertEquals(SplashPostStartupDestination.Main, viewModel.uiState.value.postStartupDestination)
        assertEquals(SplashPhase.Loading, viewModel.uiState.value.phase)
    }

    @Test
    fun startup_waitsForSlowInit() = runViewModelTest {
        val repository =
            FakeAppStartupRepository(
                result = Result.success(Unit),
                initDelayMs = 2_000L,
            )
        val viewModel =
            SplashViewModel(
                initializeApp = InitializeAppUseCase(repository),
                getUserSession = GetUserSessionUseCase(FakeUserRepository(session = UserSession("a", "r"))),
            )

        advanceTimeBy(SplashViewModel.MIN_DISPLAY_MS)
        assertNull(viewModel.uiState.value.postStartupDestination)

        advanceTimeBy(1_000L)
        advanceMainUntilIdle()
        assertEquals(SplashPostStartupDestination.Main, viewModel.uiState.value.postStartupDestination)
    }

    @Test
    fun startup_showsErrorAfterMinimumDisplayWhenInitFails() = runViewModelTest {
        val repository =
            FakeAppStartupRepository(
                result = Result.failure(IllegalStateException("db down")),
            )
        val viewModel =
            SplashViewModel(
                initializeApp = InitializeAppUseCase(repository),
                getUserSession = GetUserSessionUseCase(FakeUserRepository()),
            )

        advanceTimeBy(SplashViewModel.MIN_DISPLAY_MS)
        advanceMainUntilIdle()

        assertEquals(SplashPhase.Error, viewModel.uiState.value.phase)
        assertEquals("db down", viewModel.uiState.value.errorMessage)
        assertNull(viewModel.uiState.value.postStartupDestination)
    }

    @Test
    fun retry_recoversAfterFailure() = runViewModelTest {
        val repository =
            FakeAppStartupRepository(
                result = Result.failure(IllegalStateException("db down")),
            )
        val viewModel =
            SplashViewModel(
                initializeApp = InitializeAppUseCase(repository),
                getUserSession = GetUserSessionUseCase(FakeUserRepository(session = UserSession("a", "r"))),
            )
        advanceTimeBy(SplashViewModel.MIN_DISPLAY_MS)
        advanceMainUntilIdle()

        repository.setResult(Result.success(Unit))
        viewModel.onRetryClick()
        advanceTimeBy(SplashViewModel.MIN_DISPLAY_MS)
        advanceMainUntilIdle()

        assertEquals(SplashPostStartupDestination.Main, viewModel.uiState.value.postStartupDestination)
        assertEquals(SplashPhase.Loading, viewModel.uiState.value.phase)
    }

    @Test
    fun startup_navigatesToAuthWhenNoSession() = runViewModelTest {
        val viewModel = createViewModel(hasSession = false)

        advanceTimeBy(SplashViewModel.MIN_DISPLAY_MS)
        advanceMainUntilIdle()

        assertEquals(SplashPostStartupDestination.Auth, viewModel.uiState.value.postStartupDestination)
    }

    @Test
    fun startup_navigatesToMainWhenSessionPresent() = runViewModelTest {
        val viewModel = createViewModel(hasSession = true)

        advanceTimeBy(SplashViewModel.MIN_DISPLAY_MS)
        advanceMainUntilIdle()

        assertEquals(SplashPostStartupDestination.Main, viewModel.uiState.value.postStartupDestination)
    }

    private fun createViewModel(hasSession: Boolean): SplashViewModel =
        SplashViewModel(
            initializeApp = InitializeAppUseCase(FakeAppStartupRepository(result = Result.success(Unit))),
            getUserSession =
                GetUserSessionUseCase(
                    FakeUserRepository(
                        session =
                            if (hasSession) {
                                UserSession(accessToken = "access", refreshToken = "refresh")
                            } else {
                                null
                            },
                    ),
                ),
        )
}
