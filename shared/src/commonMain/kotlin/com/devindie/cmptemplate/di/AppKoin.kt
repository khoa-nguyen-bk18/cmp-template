package com.devindie.cmptemplate.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module

fun startKoinApp(appModules: List<Module>, configure: KoinApplication.() -> Unit = {}): KoinApplication = startKoin {
    modules(appModules + appDomainModule)
    configure()
}
