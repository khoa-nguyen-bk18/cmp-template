package com.devindie.cmptemplate.domain.model.auth

data class CountryDialCode(
    val isoCode: String,
    val dialCode: String,
    val displayName: String,
    val flagEmoji: String,
)
