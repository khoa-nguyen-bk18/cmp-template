package com.devindie.cmptemplate.analytics.impl.firebase

import android.content.Context
import android.os.Bundle
import com.devindie.cmptemplate.analytics.api.provider.EventAnalyticsProvider
import com.google.firebase.analytics.FirebaseAnalytics

internal class FirebaseEventAnalyticsProvider(
    context: Context,
) : EventAnalyticsProvider {
    private val analytics = FirebaseAnalytics.getInstance(context)

    override fun logEvent(
        name: String,
        params: Map<String, Any>,
    ) {
        analytics.logEvent(name, params.toAnalyticsBundle())
    }

    override fun logScreen(
        screenName: String,
        screenClass: String?,
    ) {
        val bundle =
            Bundle().apply {
                putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
                screenClass?.let { putString(FirebaseAnalytics.Param.SCREEN_CLASS, it) }
            }
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
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

    private fun Map<String, Any>.toAnalyticsBundle(): Bundle =
        Bundle().apply {
            forEach { (key, value) ->
                when (value) {
                    is String -> putString(key, value)
                    is Int -> putLong(key, value.toLong())
                    is Long -> putLong(key, value)
                    is Double -> putDouble(key, value)
                    is Boolean -> putString(key, value.toString())
                    else -> Unit
                }
            }
        }
}
