package com.devindie.cmptemplate.analytics.impl

import com.devindie.cmptemplate.analytics.api.AnalyticsClient
import com.devindie.cmptemplate.analytics.api.AnalyticsConfig
import com.devindie.cmptemplate.analytics.api.provider.CrashReportingProvider
import com.devindie.cmptemplate.analytics.api.provider.EventAnalyticsProvider
import com.devindie.cmptemplate.analytics.impl.provider.NoOpCrashReportingProvider
import com.devindie.cmptemplate.analytics.impl.provider.NoOpEventAnalyticsProvider
import org.koin.core.module.Module
import org.koin.core.scope.Scope
import org.koin.dsl.module

internal fun createAnalyticsModule(config: AnalyticsConfig): Module =
    module {
        single<AnalyticsClient> {
            val eventProvider =
                if (!config.enabled) {
                    NoOpEventAnalyticsProvider()
                } else {
                    config.eventProvider ?: defaultEventAnalyticsProvider()
                }
            val crashProvider =
                if (!config.enabled) {
                    NoOpCrashReportingProvider()
                } else {
                    config.crashProvider ?: defaultCrashReportingProvider()
                }
            AnalyticsClientImpl(
                eventProvider = eventProvider,
                crashProvider = crashProvider,
            )
        }
    }

internal expect fun Scope.defaultEventAnalyticsProvider(): EventAnalyticsProvider

internal expect fun Scope.defaultCrashReportingProvider(): CrashReportingProvider
