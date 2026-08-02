package com.devindie.cmptemplate.feature.auth.impl.otp

data class OtpVerificationUiState(
    val maskedPhone: String,
    val digits: String = "",
    val otpError: String? = null,
    val isVerifying: Boolean = false,
    val resendSecondsRemaining: Int = 0,
    val isResending: Boolean = false,
)
