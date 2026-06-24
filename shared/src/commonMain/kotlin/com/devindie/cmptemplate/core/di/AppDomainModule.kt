package com.devindie.cmptemplate.core.di

import com.devindie.cmptemplate.domain.usecase.carddetail.GetCardDetailUseCase
import com.devindie.cmptemplate.domain.usecase.startup.InitializeAppUseCase
import com.devindie.cmptemplate.domain.usecase.user.ClearUserSessionUseCase
import com.devindie.cmptemplate.domain.usecase.user.GetUserSessionUseCase
import com.devindie.cmptemplate.domain.usecase.user.SaveUserSessionUseCase
import com.devindie.cmptemplate.feature.browse.api.browseFeatureModule
import com.devindie.cmptemplate.feature.carddetail.api.cardDetailFeatureModule
import com.devindie.cmptemplate.feature.collection.api.collectionFeatureModule
import com.devindie.cmptemplate.feature.main.api.mainFeatureModule
import com.devindie.cmptemplate.feature.splash.api.splashFeatureModule
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val appDomainModule =
    module {
        factoryOf(::GetCardDetailUseCase)
        factoryOf(::InitializeAppUseCase)
        factoryOf(::GetUserSessionUseCase)
        factoryOf(::SaveUserSessionUseCase)
        factoryOf(::ClearUserSessionUseCase)
        includes(
            browseFeatureModule,
            cardDetailFeatureModule,
            mainFeatureModule,
            collectionFeatureModule,
            splashFeatureModule,
        )
    }
