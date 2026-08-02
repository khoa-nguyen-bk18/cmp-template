package com.devindie.cmptemplate.domain.usecase.auth

import com.devindie.cmptemplate.domain.fake.FakeAuthRepository
import com.devindie.cmptemplate.domain.model.auth.AuthError
import com.devindie.cmptemplate.domain.model.auth.PhoneNumber
import com.devindie.cmptemplate.domain.model.auth.PhoneNumberInput
import com.devindie.cmptemplate.domain.model.auth.SupportedCountries
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ValidatePhoneNumberUseCaseTest {
    @Test
    fun invoke_validInput_returnsPhoneNumber() = runTest {
        val useCase = ValidatePhoneNumberUseCase()
        val result = useCase(PhoneNumberInput(SupportedCountries.default, "87654321"))

        assertTrue(result.isSuccess)
        assertEquals("+6587654321", result.getOrThrow().e164)
    }
}

class SendOtpUseCaseTest {
    @Test
    fun invoke_delegatesToRepository() = runTest {
        val repository = FakeAuthRepository()
        val useCase = SendOtpUseCase(repository)

        val result = useCase(PhoneNumber(e164 = "+6587654321"))

        assertTrue(result.isSuccess)
    }
}

class VerifyOtpUseCaseTest {
    @Test
    fun invoke_validCode_returnsSession() = runTest {
        val repository = FakeAuthRepository()
        val challenge = repository.sendOtp(PhoneNumber(e164 = "+6587654321")).getOrThrow()
        val useCase = VerifyOtpUseCase(repository)

        val result = useCase(VerifyOtpRequest(challenge.challengeId, FakeAuthRepository.VALID_CODE_DEFAULT))

        assertTrue(result.isSuccess)
    }

    @Test
    fun invoke_invalidCode_fails() = runTest {
        val repository = FakeAuthRepository()
        val challenge = repository.sendOtp(PhoneNumber(e164 = "+6587654321")).getOrThrow()
        val useCase = VerifyOtpUseCase(repository)

        val result = useCase(VerifyOtpRequest(challenge.challengeId, "000000"))

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is AuthError.OtpInvalid)
    }
}
