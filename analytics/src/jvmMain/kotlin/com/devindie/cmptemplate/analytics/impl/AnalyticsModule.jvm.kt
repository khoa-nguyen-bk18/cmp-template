package com.devindie.cmptemplate.analytics.impl

import com.devindie.cmptemplate.analytics.api.provider.CrashReportingProvider
import com.devindie.cmptemplate.analytics.api.provider.EventAnalyticsProvider
import com.devindie.cmptemplate.analytics.impl.provider.NoOpCrashReportingProvider
import com.devindie.cmptemplate.analytics.impl.provider.NoOpEventAnalyticsProvider
import org.koin.core.scope.Scope

internal actual fun Scope.defaultEventAnalyticsProvider(): EventAnalyticsProvider =
    NoOpEventAnalyticsProvider()

internal actual fun Scope.defaultCrashReportingProvider(): CrashReportingProvider =
    NoOpCrashReportingProvider()
