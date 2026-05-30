package com.devindie.cmptemplate

import android.app.Application
import com.devindie.cmptemplate.data.di.platformDataModule
import com.devindie.cmptemplate.di.startKoinApp
import org.koin.android.ext.koin.androidContext

class CmpTemplateApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoinApp(appModules = listOf(platformDataModule())) {
            androidContext(this@CmpTemplateApplication)
        }
    }
}
