package com.devindie.cmptemplate.feature.splash.api

import com.devindie.cmptemplate.feature.splash.impl.SplashViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val splashFeatureModule =
    module {
        viewModelOf(::SplashViewModel)
    }
