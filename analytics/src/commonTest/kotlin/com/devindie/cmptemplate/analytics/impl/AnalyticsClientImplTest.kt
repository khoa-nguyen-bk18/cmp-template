package com.devindie.cmptemplate.analytics.impl

import com.devindie.cmptemplate.analytics.api.provider.CrashReportingProvider
import com.devindie.cmptemplate.analytics.api.provider.EventAnalyticsProvider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AnalyticsClientImplTest {
    @Test
    fun logEvent_delegatesToEventProvider() {
        val events = RecordingEventProvider()
        val crashes = RecordingCrashProvider()
        val client = AnalyticsClientImpl(events, crashes)

        client.logEvent("card_tapped", mapOf("card_id" to "42"))

        assertEquals("card_tapped", events.lastEventName)
        assertEquals("42", events.lastEventParams["card_id"])
    }

    @Test
    fun setUserId_updatesEventAndCrashProviders() {
        val events = RecordingEventProvider()
        val crashes = RecordingCrashProvider()
        val client = AnalyticsClientImpl(events, crashes)

        client.setUserId("user-1")

        assertEquals("user-1", events.lastUserId)
        assertEquals("user-1", crashes.lastUserId)
    }

    @Test
    fun recordException_delegatesToCrashProvider() {
        val events = RecordingEventProvider()
        val crashes = RecordingCrashProvider()
        val client = AnalyticsClientImpl(events, crashes)
        val error = IllegalStateException("boom")

        client.recordException(error, message = "handled")

        assertEquals(error, crashes.lastThrowable)
        assertEquals("handled", crashes.lastMessage)
    }

    @Test
    fun providerFailure_doesNotThrow() {
        val events =
            object : EventAnalyticsProvider {
                override fun logEvent(
                    name: String,
                    params: Map<String, Any>,
                ) {
                    error("provider down")
                }

                override fun logScreen(
                    screenName: String,
                    screenClass: String?,
                ) = Unit

                override fun setUserProperty(
                    name: String,
                    value: String,
                ) = Unit

                override fun setUserId(userId: String?) = Unit
            }
        val client = AnalyticsClientImpl(events, RecordingCrashProvider())

        client.logEvent("safe_event")

        assertTrue(true)
    }
}

private class RecordingEventProvider : EventAnalyticsProvider {
    var lastEventName: String? = null
    var lastEventParams: Map<String, Any> = emptyMap()
    var lastUserId: String? = null

    override fun logEvent(
        name: String,
        params: Map<String, Any>,
    ) {
        lastEventName = name
        lastEventParams = params
    }

    override fun logScreen(
        screenName: String,
        screenClass: String?,
    ) = Unit

    override fun setUserProperty(
        name: String,
        value: String,
    ) = Unit

    override fun setUserId(userId: String?) {
        lastUserId = userId
    }
}

private class RecordingCrashProvider : CrashReportingProvider {
    var lastThrowable: Throwable? = null
    var lastMessage: String? = null
    var lastUserId: String? = null

    override fun recordException(
        throwable: Throwable,
        message: String?,
    ) {
        lastThrowable = throwable
        lastMessage = message
    }

    override fun log(message: String) = Unit

    override fun setUserId(userId: String?) {
        lastUserId = userId
    }
}
