package com.devindie.cmptemplate.feature.auth.impl.phone

import app.cash.turbine.test
import com.devindie.cmptemplate.domain.model.auth.AuthError
import com.devindie.cmptemplate.domain.model.auth.SupportedCountries
import com.devindie.cmptemplate.domain.usecase.auth.GetSupportedCountriesUseCase
import com.devindie.cmptemplate.domain.usecase.auth.SendOtpUseCase
import com.devindie.cmptemplate.domain.usecase.auth.ValidatePhoneNumberUseCase
import com.devindie.cmptemplate.fake.FakeAuthRepository
import com.devindie.cmptemplate.test.advanceMainUntilIdle
import com.devindie.cmptemplate.test.runViewModelTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PhoneNumberViewModelTest {
    @Test
    fun emptyPhone_disablesNext() = runViewModelTest {
        val viewModel = createViewModel()
        advanceMainUntilIdle()

        assertFalse(viewModel.uiState.value.isNextEnabled)
    }

    @Test
    fun countryChange_revalidates() = runViewModelTest {
        val viewModel = createViewModel()
        advanceMainUntilIdle()

        viewModel.onLocalDigitsChange("87654321")
        advanceMainUntilIdle()
        assertTrue(viewModel.uiState.value.isNextEnabled)

        val malaysia = SupportedCountries.all.first { it.isoCode == "MY" }
        viewModel.onCountrySelected(malaysia)
        advanceMainUntilIdle()

        assertEquals(malaysia, viewModel.uiState.value.selectedCountry)
        assertFalse(viewModel.uiState.value.isNextEnabled)
    }

    @Test
    fun sendFailure_showsError() = runViewModelTest {
        val repository =
            FakeAuthRepository().apply {
                sendOtpFailure = AuthError.OtpSendFailed
            }
        val viewModel = createViewModel(repository)
        advanceMainUntilIdle()

        viewModel.onLocalDigitsChange("87654321")
        advanceMainUntilIdle()
        viewModel.onNextClick()
        advanceMainUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Unable to send code", viewModel.uiState.value.sendError)
    }

    @Test
    fun loading_ignoresNext() = runViewModelTest {
        val repository =
            FakeAuthRepository().apply {
                sendOtpDelayMs = 5_000L
            }
        val viewModel = createViewModel(repository)
        advanceMainUntilIdle()

        viewModel.onLocalDigitsChange("87654321")
        advanceMainUntilIdle()
        viewModel.onNextClick()
        runCurrent()

        assertTrue(viewModel.uiState.value.isLoading)
        viewModel.onNextClick()
        runCurrent()
        assertTrue(viewModel.uiState.value.isLoading)

        advanceTimeBy(5_000L)
        advanceMainUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun validPhone_navigatesToOtp() = runViewModelTest {
        val viewModel = createViewModel()
        advanceMainUntilIdle()

        viewModel.events.test {
            viewModel.onLocalDigitsChange("87654321")
            advanceMainUntilIdle()
            viewModel.onNextClick()
            advanceMainUntilIdle()

            val event = awaitItem()
            assertTrue(event is PhoneNumberEvent.NavigateToOtp)
            assertEquals("+65 •••• 4321", event.route.maskedPhone)
            assertTrue(event.route.challengeId.isNotBlank())
        }
    }

    private fun createViewModel(repository: FakeAuthRepository = FakeAuthRepository()): PhoneNumberViewModel =
        PhoneNumberViewModel(
            getSupportedCountries = GetSupportedCountriesUseCase(repository),
            validatePhoneNumber = ValidatePhoneNumberUseCase(),
            sendOtp = SendOtpUseCase(repository),
        )
}
