package com.devindie.cmptemplate

import android.app.Application
import com.devindie.cmptemplate.analytics.api.AnalyticsConfig
import com.devindie.cmptemplate.analytics.api.analyticsFeatureModule
import com.devindie.cmptemplate.browsePagingModule
import com.devindie.cmptemplate.core.di.startKoinApp
import com.devindie.cmptemplate.data.di.platformDataModule
import com.devindie.cmptemplate.settings.settingsCatalogModule
import org.koin.android.ext.koin.androidContext

class CmpTemplateApplication : Application() {
    override fun onCreate() {
        super.onCreate()
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
                ),
        ) {
            androidContext(this@CmpTemplateApplication)
        }
    }
}
