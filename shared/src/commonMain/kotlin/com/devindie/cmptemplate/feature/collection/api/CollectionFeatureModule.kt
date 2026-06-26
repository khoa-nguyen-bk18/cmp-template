package com.devindie.cmptemplate.feature.collection.api

import com.devindie.cmptemplate.feature.collection.impl.CollectionViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val collectionFeatureModule =
    module {
        viewModelOf(::CollectionViewModel)
    }
