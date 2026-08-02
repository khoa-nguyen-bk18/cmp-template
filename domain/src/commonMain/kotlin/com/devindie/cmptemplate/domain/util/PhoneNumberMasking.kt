package com.devindie.cmptemplate.domain.util

import com.devindie.cmptemplate.domain.model.auth.PhoneNumber
import com.devindie.cmptemplate.domain.model.auth.SupportedCountries

fun maskPhoneNumber(phone: PhoneNumber): String {
    val e164 = phone.e164
    val country =
        SupportedCountries.all
            .filter { e164.startsWith(it.dialCode) }
            .maxByOrNull { it.dialCode.length }
            ?: return e164
    val localDigits = e164.removePrefix(country.dialCode)
    val lastFour = localDigits.takeLast(4)
    return "${country.dialCode} •••• $lastFour"
}
