package com.devindie.cmptemplate.analytics.api

data class AnalyticsEvent(
    val name: String,
    val params: Map<String, Any> = emptyMap(),
)

fun AnalyticsClient.logEvent(event: AnalyticsEvent) {
    logEvent(event.name, event.params)
}
