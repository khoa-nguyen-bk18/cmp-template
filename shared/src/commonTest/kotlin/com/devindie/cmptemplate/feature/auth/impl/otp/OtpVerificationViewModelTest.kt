package com.devindie.cmptemplate.feature.auth.impl.otp

import app.cash.turbine.test
import com.devindie.cmptemplate.domain.model.auth.PhoneNumber
import com.devindie.cmptemplate.domain.usecase.auth.ResendOtpUseCase
import com.devindie.cmptemplate.domain.usecase.auth.VerifyOtpUseCase
import com.devindie.cmptemplate.domain.usecase.onboarding.CompleteOnboardingUseCase
import com.devindie.cmptemplate.domain.usecase.user.SaveUserSessionUseCase
import com.devindie.cmptemplate.fake.FakeAuthRepository
import com.devindie.cmptemplate.fake.FakeOnboardingRepository
import com.devindie.cmptemplate.fake.FakeUserRepository
import com.devindie.cmptemplate.test.advanceMainUntilIdle
import com.devindie.cmptemplate.test.runViewModelTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class OtpVerificationViewModelTest {
    @Test
    fun wrongCode_showsError() = runViewModelTest {
        val repository = FakeAuthRepository()
        val challenge = repository.sendOtp(samplePhone()).getOrThrow()
        val viewModel = createViewModel(repository, challenge.challengeId)
        advanceMainUntilIdle()

        viewModel.onDigitsChange("000000")
        advanceMainUntilIdle()

        assertEquals("Invalid code", viewModel.uiState.value.otpError)
        assertEquals("000000", viewModel.uiState.value.digits)
    }

    @Test
    fun success_savesSession() = runViewModelTest {
        val repository = FakeAuthRepository()
        val userRepository = FakeUserRepository()
        val onboardingRepository = FakeOnboardingRepository()
        val challenge = repository.sendOtp(samplePhone()).getOrThrow()
        val viewModel =
            createViewModel(
                repository = repository,
                challengeId = challenge.challengeId,
                userRepository = userRepository,
                onboardingRepository = onboardingRepository,
            )
        advanceMainUntilIdle()

        viewModel.authComplete.test {
            viewModel.onDigitsChange(FakeAuthRepository.VALID_CODE_DEFAULT)
            advanceMainUntilIdle()

            awaitItem()
            assertNotNull(userRepository.session)
            assertEquals(1, onboardingRepository.markCompletedCallCount)
        }
    }

    @Test
    fun clear_resetsDigits() = runViewModelTest {
        val repository = FakeAuthRepository()
        val challenge = repository.sendOtp(samplePhone()).getOrThrow()
        val viewModel = createViewModel(repository, challenge.challengeId)
        advanceMainUntilIdle()

        viewModel.onDigitsChange("123")
        viewModel.onClearClick()

        assertEquals("", viewModel.uiState.value.digits)
        assertNull(viewModel.uiState.value.otpError)
    }

    @Test
    fun resend_issuesNewChallengeId() = runViewModelTest {
        val repository = FakeAuthRepository()
        val challenge = repository.sendOtp(samplePhone()).getOrThrow()
        val viewModel = createViewModel(repository, challenge.challengeId, resendCooldownSeconds = 0)
        advanceMainUntilIdle()

        viewModel.onResendClick()
        advanceMainUntilIdle()

        val oldVerify =
            repository.verifyOtp(
                challenge.challengeId,
                FakeAuthRepository.VALID_CODE_DEFAULT,
            )
        assertTrue(oldVerify.isFailure)

        viewModel.onDigitsChange(FakeAuthRepository.VALID_CODE_DEFAULT)
        advanceMainUntilIdle()

        assertNull(viewModel.uiState.value.otpError)
    }

    private fun createViewModel(
        repository: FakeAuthRepository,
        challengeId: String,
        resendCooldownSeconds: Int = 30,
        userRepository: FakeUserRepository = FakeUserRepository(),
        onboardingRepository: FakeOnboardingRepository = FakeOnboardingRepository(),
    ): OtpVerificationViewModel =
        OtpVerificationViewModel(
            maskedPhone = "+65 •••• 4321",
            challengeId = challengeId,
            resendCooldownSeconds = resendCooldownSeconds,
            verifyOtp = VerifyOtpUseCase(repository),
            resendOtp = ResendOtpUseCase(repository),
            saveUserSession = SaveUserSessionUseCase(userRepository),
            completeOnboarding = CompleteOnboardingUseCase(onboardingRepository),
        )

    private fun samplePhone(): PhoneNumber = PhoneNumber(e164 = "+6587654321")
}
