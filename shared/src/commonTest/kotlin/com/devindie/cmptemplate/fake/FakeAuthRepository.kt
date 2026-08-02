package com.devindie.cmptemplate.fake

import com.devindie.cmptemplate.domain.model.auth.AuthError
import com.devindie.cmptemplate.domain.model.auth.CountryDialCode
import com.devindie.cmptemplate.domain.model.auth.OtpChallenge
import com.devindie.cmptemplate.domain.model.auth.PhoneNumber
import com.devindie.cmptemplate.domain.model.auth.SupportedCountries
import com.devindie.cmptemplate.domain.model.user.UserSession
import com.devindie.cmptemplate.domain.repository.AuthRepository
import kotlinx.coroutines.delay

class FakeAuthRepository : AuthRepository {
    private val challenges = mutableMapOf<String, PhoneNumber>()
    private var challengeCounter = 0
    var nowEpochMs: Long = 1_000_000L
    var validCode: String = VALID_CODE_DEFAULT
    var sendOtpFailure: Throwable? = null
    var resendOtpFailure: Throwable? = null
    var sendOtpDelayMs: Long = 0L

    override fun getSupportedCountries(): List<CountryDialCode> = SupportedCountries.all

    override suspend fun sendOtp(phone: PhoneNumber): Result<OtpChallenge> {
        sendOtpFailure?.let { return Result.failure(it) }
        if (sendOtpDelayMs > 0L) {
            delay(sendOtpDelayMs)
        }
        return Result.success(createChallenge(phone))
    }

    override suspend fun resendOtp(challengeId: String): Result<OtpChallenge> {
        resendOtpFailure?.let { return Result.failure(it) }
        val phone = challenges.remove(challengeId) ?: return Result.failure(AuthError.OtpExpired)
        return Result.success(createChallenge(phone))
    }

    override suspend fun verifyOtp(challengeId: String, code: String): Result<UserSession> {
        if (challenges[challengeId] == null) {
            return Result.failure(AuthError.OtpExpired)
        }
        if (code != validCode) {
            return Result.failure(AuthError.OtpInvalid)
        }
        challenges.remove(challengeId)
        return Result.success(
            UserSession(
                accessToken = "fake-access-token",
                refreshToken = "fake-refresh-token",
            ),
        )
    }

    fun lastChallengeId(): String? = challenges.keys.lastOrNull()

    private fun createChallenge(phone: PhoneNumber): OtpChallenge {
        challengeCounter++
        val challengeId = "fake-challenge-$challengeCounter"
        challenges[challengeId] = phone
        return OtpChallenge(
            challengeId = challengeId,
            resendAvailableAtEpochMs = nowEpochMs + RESEND_DELAY_MS,
        )
    }

    companion object {
        const val VALID_CODE_DEFAULT: String = "123456"
        const val RESEND_DELAY_MS: Long = 30_000L
    }
}
