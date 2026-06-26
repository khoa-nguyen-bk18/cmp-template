package com.devindie.cmptemplate.analytics.impl.provider

import com.devindie.cmptemplate.analytics.api.provider.EventAnalyticsProvider

internal class NoOpEventAnalyticsProvider : EventAnalyticsProvider {
    override fun logEvent(
        name: String,
        params: Map<String, Any>,
    ) = Unit

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
