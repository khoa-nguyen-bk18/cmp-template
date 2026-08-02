package com.devindie.cmptemplate.domain.usecase.auth

import com.devindie.cmptemplate.domain.model.user.UserSession
import com.devindie.cmptemplate.domain.repository.AuthRepository
import com.devindie.cmptemplate.domain.usecase.UseCase

data class VerifyOtpRequest(val challengeId: String, val code: String)

class VerifyOtpUseCase(private val repository: AuthRepository) : UseCase<VerifyOtpRequest, Result<UserSession>> {
    override suspend fun invoke(parameters: VerifyOtpRequest): Result<UserSession> =
        repository.verifyOtp(parameters.challengeId, parameters.code)
}
