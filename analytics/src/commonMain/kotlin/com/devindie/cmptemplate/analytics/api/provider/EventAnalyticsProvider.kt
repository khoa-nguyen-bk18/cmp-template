package com.devindie.cmptemplate.analytics.api.provider

interface EventAnalyticsProvider {
    fun logEvent(
        name: String,
        params: Map<String, Any>,
    )

    fun logScreen(
        screenName: String,
        screenClass: String?,
    )

    fun setUserProperty(
        name: String,
        value: String,
    )

    fun setUserId(userId: String?)
}
