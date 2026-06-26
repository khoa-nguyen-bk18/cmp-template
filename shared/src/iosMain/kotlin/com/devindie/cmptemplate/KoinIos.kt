package com.devindie.cmptemplate

import com.devindie.cmptemplate.analytics.api.AnalyticsConfig
import com.devindie.cmptemplate.analytics.api.analyticsFeatureModule
import com.devindie.cmptemplate.billing.api.billingFeatureModule
import com.devindie.cmptemplate.billing.billingConfigForIos
import com.devindie.cmptemplate.browsePagingModule
import com.devindie.cmptemplate.core.di.startKoinApp
import com.devindie.cmptemplate.data.di.platformDataModule
import com.devindie.cmptemplate.settings.settingsCatalogModule

fun doInitKoin() {
    startKoinApp(
        appModules =
            listOf(
                platformDataModule(),
                settingsCatalogModule(),
                browsePagingModule,
                analyticsFeatureModule(AnalyticsConfig(enabled = true)),
                billingFeatureModule(billingConfigForIos()),
            ),
    )
}
