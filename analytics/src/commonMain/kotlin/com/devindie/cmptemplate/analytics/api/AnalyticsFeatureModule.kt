package com.devindie.cmptemplate.analytics.api

import com.devindie.cmptemplate.analytics.impl.createAnalyticsModule
import org.koin.core.module.Module

fun analyticsFeatureModule(config: AnalyticsConfig): Module = createAnalyticsModule(config)
