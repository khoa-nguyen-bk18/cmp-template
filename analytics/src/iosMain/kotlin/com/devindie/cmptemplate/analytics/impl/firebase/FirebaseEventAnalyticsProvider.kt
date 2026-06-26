package com.devindie.cmptemplate.analytics.impl.firebase

import com.devindie.cmptemplate.analytics.api.provider.EventAnalyticsProvider
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.analytics.analytics
import dev.gitlive.firebase.analytics.logEvent

internal class FirebaseEventAnalyticsProvider : EventAnalyticsProvider {
    private val analytics = Firebase.analytics

    override fun logEvent(
        name: String,
        params: Map<String, Any>,
    ) {
        analytics.logEvent(name, params)
    }

    override fun logScreen(
        screenName: String,
        screenClass: String?,
    ) {
        val params =
            buildMap<String, Any> {
                put("screen_name", screenName)
                screenClass?.let { put("screen_class", it) }
            }
        analytics.logEvent("screen_view", params)
    }

    override fun setUserProperty(
        name: String,
        value: String,
    ) {
        analytics.setUserProperty(name, value)
    }

    override fun setUserId(userId: String?) {
        analytics.setUserId(userId)
    }
}
