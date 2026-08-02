package com.devindie.cmptemplate.domain.model.auth

data class PhoneNumberInput(
    val country: CountryDialCode,
    val localDigits: String,
)
