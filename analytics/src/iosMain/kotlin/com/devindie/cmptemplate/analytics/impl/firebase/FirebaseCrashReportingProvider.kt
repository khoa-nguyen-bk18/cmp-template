package com.devindie.cmptemplate.analytics.impl.firebase

import com.devindie.cmptemplate.analytics.api.provider.CrashReportingProvider
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.crashlytics.crashlytics

internal class FirebaseCrashReportingProvider : CrashReportingProvider {
    private val crashlytics = Firebase.crashlytics

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
