package com.devindie.cmptemplate.analytics.impl.provider

import com.devindie.cmptemplate.analytics.api.provider.CrashReportingProvider

internal class NoOpCrashReportingProvider : CrashReportingProvider {
    override fun recordException(
        throwable: Throwable,
        message: String?,
    ) = Unit

    override fun log(message: String) = Unit

    override fun setUserId(userId: String?) = Unit
}
