package com.devindie.cmptemplate

import android.app.Application
import com.devindie.cmptemplate.browsePagingModule
import com.devindie.cmptemplate.core.di.startKoinApp
import com.devindie.cmptemplate.data.di.platformDataModule
import org.koin.android.ext.koin.androidContext

class CmpTemplateApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoinApp(appModules = listOf(platformDataModule(), browsePagingModule)) {
            androidContext(this@CmpTemplateApplication)
        }
    }
}
