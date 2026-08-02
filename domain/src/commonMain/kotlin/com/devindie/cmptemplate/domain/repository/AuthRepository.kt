package com.devindie.cmptemplate.domain.repository

import com.devindie.cmptemplate.domain.model.auth.CountryDialCode
import com.devindie.cmptemplate.domain.model.auth.OtpChallenge
import com.devindie.cmptemplate.domain.model.auth.PhoneNumber
import com.devindie.cmptemplate.domain.model.user.UserSession

interface AuthRepository {
    fun getSupportedCountries(): List<CountryDialCode>

    suspend fun sendOtp(phone: PhoneNumber): Result<OtpChallenge>

    suspend fun resendOtp(challengeId: String): Result<OtpChallenge>

    suspend fun verifyOtp(challengeId: String, code: String): Result<UserSession>
}
