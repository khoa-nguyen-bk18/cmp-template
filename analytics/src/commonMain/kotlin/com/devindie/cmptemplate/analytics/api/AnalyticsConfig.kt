package com.devindie.cmptemplate.analytics.api

import com.devindie.cmptemplate.analytics.api.provider.CrashReportingProvider
import com.devindie.cmptemplate.analytics.api.provider.EventAnalyticsProvider

data class AnalyticsConfig(
    val enabled: Boolean = true,
    val eventProvider: EventAnalyticsProvider? = null,
    val crashProvider: CrashReportingProvider? = null,
)
