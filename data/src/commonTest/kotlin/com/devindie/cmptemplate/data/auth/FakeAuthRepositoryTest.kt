package com.devindie.cmptemplate.data.auth

import com.devindie.cmptemplate.data.coroutines.DispatcherProvider
import com.devindie.cmptemplate.domain.model.auth.PhoneNumber
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class FakeAuthRepositoryTest {
  private val testDispatcher = StandardTestDispatcher()
  private val dispatchers =
      object : DispatcherProvider {
          override val io = testDispatcher
          override val default = testDispatcher
          override val main = testDispatcher
      }

    @Test
    fun verifyOtp_validCode_succeeds() = runTest(testDispatcher) {
        val repository = FakeAuthRepository(dispatchers)
        val challenge = repository.sendOtp(PhoneNumber(e164 = "+6587654321")).getOrThrow()
        testScheduler.advanceUntilIdle()

        val result = repository.verifyOtp(challenge.challengeId, FakeAuthRepository.VALID_CODE)
        testScheduler.advanceUntilIdle()

        assertTrue(result.isSuccess)
    }

    @Test
    fun resendOtp_issuesNewChallengeId() = runTest(testDispatcher) {
        val repository = FakeAuthRepository(dispatchers)
        val first = repository.sendOtp(PhoneNumber(e164 = "+6587654321")).getOrThrow()
        testScheduler.advanceUntilIdle()

        val second = repository.resendOtp(first.challengeId).getOrThrow()
        testScheduler.advanceUntilIdle()

        assertNotEquals(first.challengeId, second.challengeId)
    }
}
