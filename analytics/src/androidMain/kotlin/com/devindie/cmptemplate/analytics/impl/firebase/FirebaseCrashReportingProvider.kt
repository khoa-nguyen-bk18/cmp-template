package com.devindie.cmptemplate.analytics.impl.firebase

import com.devindie.cmptemplate.analytics.api.provider.CrashReportingProvider
import com.google.firebase.crashlytics.FirebaseCrashlytics

internal class FirebaseCrashReportingProvider : CrashReportingProvider {
    private val crashlytics = FirebaseCrashlytics.getInstance()

    override fun recordException(
        throwable: Throwable,
        message: String?,
    ) {
        message?.let { crashlytics.log(it) }
        crashlytics.recordException(throwable)
    }

    override fun log(message: String) {
        crashlytics.log(message)
    }

    override fun setUserId(userId: String?) {
        if (userId != null) {
            crashlytics.setUserId(userId)
        }
    }
}
