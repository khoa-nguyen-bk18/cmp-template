package com.devindie.cmptemplate.domain.usecase.auth

import com.devindie.cmptemplate.domain.model.auth.PhoneNumber
import com.devindie.cmptemplate.domain.model.auth.PhoneNumberInput
import com.devindie.cmptemplate.domain.usecase.UseCase
import com.devindie.cmptemplate.domain.validation.PhoneNumberValidator

class ValidatePhoneNumberUseCase : UseCase<PhoneNumberInput, Result<PhoneNumber>> {
    override suspend fun invoke(parameters: PhoneNumberInput): Result<PhoneNumber> =
        PhoneNumberValidator.validate(parameters)
}
