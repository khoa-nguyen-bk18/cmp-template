package com.devindie.cmptemplate.data.auth

import com.devindie.cmptemplate.data.coroutines.DispatcherProvider
import com.devindie.cmptemplate.domain.model.auth.AuthError
import com.devindie.cmptemplate.domain.model.auth.CountryDialCode
import com.devindie.cmptemplate.domain.model.auth.OtpChallenge
import com.devindie.cmptemplate.domain.model.auth.PhoneNumber
import com.devindie.cmptemplate.domain.model.auth.SupportedCountries
import com.devindie.cmptemplate.domain.model.user.UserSession
import com.devindie.cmptemplate.domain.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class FakeAuthRepository(
    private val dispatchers: DispatcherProvider,
) : AuthRepository {
    private val challenges = mutableMapOf<String, PhoneNumber>()
    private var challengeCounter = 0

    override fun getSupportedCountries(): List<CountryDialCode> = SupportedCountries.all

    override suspend fun sendOtp(phone: PhoneNumber): Result<OtpChallenge> =
        withContext(dispatchers.io) {
            delay(OPERATION_DELAY_MS)
            Result.success(createChallenge(phone))
        }

    override suspend fun resendOtp(challengeId: String): Result<OtpChallenge> =
        withContext(dispatchers.io) {
            delay(OPERATION_DELAY_MS)
            val phone = challenges.remove(challengeId) ?: return@withContext Result.failure(AuthError.OtpExpired)
            Result.success(createChallenge(phone))
        }

    override suspend fun verifyOtp(challengeId: String, code: String): Result<UserSession> =
        withContext(dispatchers.io) {
            delay(OPERATION_DELAY_MS)
            if (challenges[challengeId] == null) {
                return@withContext Result.failure(AuthError.OtpExpired)
            }
            if (code != VALID_CODE) {
                return@withContext Result.failure(AuthError.OtpInvalid)
            }
            challenges.remove(challengeId)
            Result.success(
                UserSession(
                    accessToken = "fake-access-token",
                    refreshToken = "fake-refresh-token",
                ),
            )
        }

    private fun createChallenge(phone: PhoneNumber): OtpChallenge {
        challengeCounter++
        val challengeId = "fake-challenge-$challengeCounter"
        challenges[challengeId] = phone
        return OtpChallenge(
            challengeId = challengeId,
            resendAvailableAtEpochMs = currentEpochMillis() + RESEND_DELAY_MS,
        )
    }

    companion object {
        const val VALID_CODE: String = "123456"
        private const val OPERATION_DELAY_MS: Long = 500L
        private const val RESEND_DELAY_MS: Long = 30_000L
    }
}
