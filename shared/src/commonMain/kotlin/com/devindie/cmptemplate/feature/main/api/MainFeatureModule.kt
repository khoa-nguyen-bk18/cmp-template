package com.devindie.cmptemplate.feature.main.api

import com.devindie.cmptemplate.feature.main.impl.MainViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val mainFeatureModule =
    module {
        viewModelOf(::MainViewModel)
    }
