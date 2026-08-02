package com.devindie.cmptemplate.domain.model.auth

data class VerifyOtpRequest(val challengeId: String, val code: String)
