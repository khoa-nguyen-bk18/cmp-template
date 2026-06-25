package com.devindie.cmptemplate.analytics.api

interface AnalyticsClient {
    fun logEvent(
        name: String,
        params: Map<String, Any> = emptyMap(),
    )

    fun logScreen(
        screenName: String,
        screenClass: String? = null,
    )

    fun setUserProperty(
        name: String,
        value: String,
    )

    fun setUserId(userId: String?)

    fun recordException(
        throwable: Throwable,
        message: String? = null,
    )

    fun log(message: String)
}
