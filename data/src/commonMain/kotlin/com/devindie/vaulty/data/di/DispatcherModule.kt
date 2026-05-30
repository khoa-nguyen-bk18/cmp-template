package com.devindie.vaulty.data.di

import com.devindie.vaulty.data.coroutines.DefaultDispatcherProvider
import com.devindie.vaulty.data.coroutines.DispatcherProvider
import org.koin.dsl.module

/** Koin bindings for [com.devindie.vaulty.data.coroutines.DispatcherProvider]. */
val dispatcherModule =
    module {
        single<DispatcherProvider> { DefaultDispatcherProvider() }
    }
