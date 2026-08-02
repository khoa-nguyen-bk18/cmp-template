package com.devindie.cmptemplate.domain.model.auth

data class OtpChallenge(
    val challengeId: String,
    val resendAvailableAtEpochMs: Long,
)
