package com.devindie.cmptemplate.feature.auth.api

import kotlinx.serialization.Serializable

@Serializable
data object AuthRoute

@Serializable
data object PhoneNumberRoute

@Serializable
data class OtpVerificationRoute(
    val challengeId: String,
    val maskedPhone: String,
    val resendAvailableAtEpochMs: Long,
)
