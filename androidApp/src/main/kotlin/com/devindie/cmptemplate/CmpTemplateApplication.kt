package com.devindie.cmptemplate

import android.app.Application
import com.devindie.cmptemplate.analytics.api.AnalyticsConfig
import com.devindie.cmptemplate.analytics.api.analyticsFeatureModule
import com.devindie.cmptemplate.billing.billingKoinModuleForAndroid
import com.devindie.cmptemplate.billing.configureBillingPlatform
import com.devindie.cmptemplate.browsePagingModule
import com.devindie.cmptemplate.core.di.startKoinApp
import com.devindie.cmptemplate.data.di.platformDataModule
import com.devindie.cmptemplate.settings.settingsCatalogModule
import org.koin.android.ext.koin.androidContext

class CmpTemplateApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val billingEnabled =
            BuildConfig.BILLING_ENABLED && BuildConfig.REVENUECAT_API_KEY_ANDROID.isNotBlank()
        if (billingEnabled) {
            configureBillingPlatform(apiKey = BuildConfig.REVENUECAT_API_KEY_ANDROID)
        }

        startKoinApp(
            appModules =
                listOf(
                    platformDataModule(),
                    settingsCatalogModule(),
                    browsePagingModule,
                    analyticsFeatureModule(
                        AnalyticsConfig(
                            enabled = !BuildConfig.DEBUG,
                        ),
                    ),
                    billingKoinModuleForAndroid(
                        enabled = BuildConfig.BILLING_ENABLED,
                        apiKey = BuildConfig.REVENUECAT_API_KEY_ANDROID,
                    ),
                ),
        ) {
            androidContext(this@CmpTemplateApplication)
        }
    }
}
