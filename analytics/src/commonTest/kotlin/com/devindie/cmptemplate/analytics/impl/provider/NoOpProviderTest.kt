package com.devindie.cmptemplate.analytics.impl.provider

import kotlin.test.Test
import kotlin.test.assertTrue

class NoOpProviderTest {
    @Test
    fun noOpEventProvider_doesNotThrow() {
        val provider = NoOpEventAnalyticsProvider()

        provider.logEvent("event", emptyMap())
        provider.logScreen("screen", null)
        provider.setUserProperty("tier", "free")
        provider.setUserId("user")

        assertTrue(true)
    }

    @Test
    fun noOpCrashProvider_doesNotThrow() {
        val provider = NoOpCrashReportingProvider()

        provider.log("breadcrumb")
        provider.recordException(IllegalStateException("x"), "msg")
        provider.setUserId("user")

        assertTrue(true)
    }
}
