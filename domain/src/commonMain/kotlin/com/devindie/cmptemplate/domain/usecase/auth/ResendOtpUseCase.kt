package com.devindie.cmptemplate.domain.usecase.auth

import com.devindie.cmptemplate.domain.model.auth.OtpChallenge
import com.devindie.cmptemplate.domain.repository.AuthRepository
import com.devindie.cmptemplate.domain.usecase.UseCase

class ResendOtpUseCase(private val repository: AuthRepository) : UseCase<String, Result<OtpChallenge>> {
    override suspend fun invoke(parameters: String): Result<OtpChallenge> = repository.resendOtp(parameters)
}
