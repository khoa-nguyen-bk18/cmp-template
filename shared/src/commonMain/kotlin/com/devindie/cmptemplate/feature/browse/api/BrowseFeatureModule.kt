package com.devindie.cmptemplate.feature.browse.api

import com.devindie.cmptemplate.feature.browse.impl.BrowseViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val browseFeatureModule =
    module {
        viewModelOf(::BrowseViewModel)
    }
