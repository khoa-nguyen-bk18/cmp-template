package com.devindie.cmptemplate.analytics.api.provider

interface CrashReportingProvider {
    fun recordException(
        throwable: Throwable,
        message: String?,
    )

    fun log(message: String)

    fun setUserId(userId: String?)
}
