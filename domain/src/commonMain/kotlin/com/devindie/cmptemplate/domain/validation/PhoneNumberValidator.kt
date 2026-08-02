package com.devindie.cmptemplate.domain.validation

import com.devindie.cmptemplate.domain.model.auth.AuthError
import com.devindie.cmptemplate.domain.model.auth.PhoneNumber
import com.devindie.cmptemplate.domain.model.auth.PhoneNumberInput

object PhoneNumberValidator {
    fun validate(input: PhoneNumberInput): Result<PhoneNumber> {
        val digits = input.localDigits.filter { it.isDigit() }
        if (digits.isEmpty() || digits != input.localDigits) {
            return Result.failure(AuthError.InvalidPhoneNumber)
        }

        if (!isValidForCountry(input.country.isoCode, digits)) {
            return Result.failure(AuthError.InvalidPhoneNumber)
        }

        val e164 = "${input.country.dialCode}$digits"
        return Result.success(PhoneNumber(e164 = e164))
    }

    private fun isValidForCountry(isoCode: String, digits: String): Boolean =
        when (isoCode) {
            "SG" -> digits.length == 8 && digits.first() in '8'..'9'
            "MY" -> digits.length in 9..10 && digits.first() == '1'
            "TH" -> digits.length == 9 && digits.first() in '6'..'9'
            "ID" -> digits.length in 9..12 && digits.first() == '8'
            "PH" -> digits.length == 10 && digits.first() == '9'
            "VN" -> digits.length == 9 && digits.first() in '3'..'9'
            else -> false
        }
}
