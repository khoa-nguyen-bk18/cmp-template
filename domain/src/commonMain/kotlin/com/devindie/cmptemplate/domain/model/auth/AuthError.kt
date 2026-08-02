package com.devindie.cmptemplate.domain.model.auth

sealed class AuthError : Throwable() {
    data object InvalidPhoneNumber : AuthError()

    data object OtpSendFailed : AuthError()

    data object OtpInvalid : AuthError()

    data object OtpExpired : AuthError()

    data object TooManyAttempts : AuthError()

    data object NetworkUnavailable : AuthError()

    data object Unknown : AuthError()
}
