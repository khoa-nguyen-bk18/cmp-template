package com.devindie.cmptemplate.domain.usecase.auth

import com.devindie.cmptemplate.domain.model.auth.VerifyOtpRequest
import com.devindie.cmptemplate.domain.model.user.UserSession
import com.devindie.cmptemplate.domain.repository.AuthRepository
import com.devindie.cmptemplate.domain.usecase.UseCase

class VerifyOtpUseCase(private val repository: AuthRepository) : UseCase<VerifyOtpRequest, Result<UserSession>> {
    override suspend fun invoke(parameters: VerifyOtpRequest): Result<UserSession> =
        repository.verifyOtp(parameters.challengeId, parameters.code)
}
