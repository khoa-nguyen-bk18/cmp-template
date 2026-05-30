package com.devindie.cmptemplate

import com.devindie.cmptemplate.data.di.platformDataModule
import com.devindie.cmptemplate.di.startKoinApp

fun doInitKoin() {
    startKoinApp(appModules = listOf(platformDataModule()))
}
