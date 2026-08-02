package com.devindie.cmptemplate.domain.fake

import com.devindie.cmptemplate.domain.model.auth.AuthError
import com.devindie.cmptemplate.domain.model.auth.PhoneNumber
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class FakeAuthRepositoryTest {
    @Test
    fun sendOtp_returnsChallenge() = runTest {
        val repository = FakeAuthRepository()
        val phone = PhoneNumber(e164 = "+6587654321")

        val result = repository.sendOtp(phone)

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().challengeId.isNotBlank())
    }

    @Test
    fun verifyOtp_validCode_returnsSession() = runTest {
        val repository = FakeAuthRepository()
        val challenge = repository.sendOtp(PhoneNumber(e164 = "+6587654321")).getOrThrow()

        val result = repository.verifyOtp(challenge.challengeId, FakeAuthRepository.VALID_CODE_DEFAULT)

        assertTrue(result.isSuccess)
    }

    @Test
    fun verifyOtp_invalidCode_fails() = runTest {
        val repository = FakeAuthRepository()
        val challenge = repository.sendOtp(PhoneNumber(e164 = "+6587654321")).getOrThrow()

        val result = repository.verifyOtp(challenge.challengeId, "000000")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is AuthError.OtpInvalid)
    }

    @Test
    fun resendOtp_returnsNewChallengeId() = runTest {
        val repository = FakeAuthRepository()
        val phone = PhoneNumber(e164 = "+6587654321")
        val first = repository.sendOtp(phone).getOrThrow()

        val resent = repository.resendOtp(first.challengeId).getOrThrow()

        assertNotEquals(first.challengeId, resent.challengeId)
    }
}
