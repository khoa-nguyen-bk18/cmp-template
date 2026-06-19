package com.devindie.cmptemplate

import com.devindie.cmptemplate.browsePagingModule
import com.devindie.cmptemplate.core.di.startKoinApp
import com.devindie.cmptemplate.data.di.platformDataModule

fun doInitKoin() {
    startKoinApp(appModules = listOf(platformDataModule(), browsePagingModule))
}
