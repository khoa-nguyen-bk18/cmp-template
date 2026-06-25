package com.devindie.cmptemplate.analytics.impl

import com.devindie.cmptemplate.analytics.api.AnalyticsClient
import com.devindie.cmptemplate.analytics.api.provider.CrashReportingProvider
import com.devindie.cmptemplate.analytics.api.provider.EventAnalyticsProvider

internal class AnalyticsClientImpl(
    private val eventProvider: EventAnalyticsProvider,
    private val crashProvider: CrashReportingProvider,
) : AnalyticsClient {
    override fun logEvent(
        name: String,
        params: Map<String, Any>,
    ) {
        runSafely("logEvent") {
            eventProvider.logEvent(name, params)
        }
    }

    override fun logScreen(
        screenName: String,
        screenClass: String?,
    ) {
        runSafely("logScreen") {
            eventProvider.logScreen(screenName, screenClass)
        }
    }

    override fun setUserProperty(
        name: String,
        value: String,
    ) {
        runSafely("setUserProperty") {
            eventProvider.setUserProperty(name, value)
        }
    }

    override fun setUserId(userId: String?) {
        runSafely("setUserId") {
            eventProvider.setUserId(userId)
            crashProvider.setUserId(userId)
        }
    }

    override fun recordException(
        throwable: Throwable,
        message: String?,
    ) {
        runSafely("recordException") {
            crashProvider.recordException(throwable, message)
        }
    }

    override fun log(message: String) {
        runSafely("log") {
            crashProvider.log(message)
        }
    }

    private inline fun runSafely(
        operation: String,
        block: () -> Unit,
    ) {
        try {
            block()
        } catch (@Suppress("TooGenericExceptionCaught") error: Exception) {
            println("AnalyticsClient.$operation failed: ${error.message}")
        }
    }
}
