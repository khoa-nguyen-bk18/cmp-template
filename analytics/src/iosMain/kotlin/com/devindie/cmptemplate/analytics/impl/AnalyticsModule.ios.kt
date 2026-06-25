package com.devindie.cmptemplate.analytics.impl

import com.devindie.cmptemplate.analytics.api.provider.CrashReportingProvider
import com.devindie.cmptemplate.analytics.api.provider.EventAnalyticsProvider
import com.devindie.cmptemplate.analytics.impl.firebase.FirebaseCrashReportingProvider
import com.devindie.cmptemplate.analytics.impl.firebase.FirebaseEventAnalyticsProvider
import org.koin.core.scope.Scope

internal actual fun Scope.defaultEventAnalyticsProvider(): EventAnalyticsProvider =
    FirebaseEventAnalyticsProvider()

internal actual fun Scope.defaultCrashReportingProvider(): CrashReportingProvider =
    FirebaseCrashReportingProvider()
