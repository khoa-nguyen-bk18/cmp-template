package com.devindie.cmptemplate.domain.usecase.auth

import com.devindie.cmptemplate.domain.model.auth.OtpChallenge
import com.devindie.cmptemplate.domain.model.auth.PhoneNumber
import com.devindie.cmptemplate.domain.repository.AuthRepository
import com.devindie.cmptemplate.domain.usecase.UseCase

class SendOtpUseCase(private val repository: AuthRepository) : UseCase<PhoneNumber, Result<OtpChallenge>> {
    override suspend fun invoke(parameters: PhoneNumber): Result<OtpChallenge> = repository.sendOtp(parameters)
}
