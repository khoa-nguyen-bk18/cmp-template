package com.devindie.cmptemplate.core.di

import com.devindie.cmptemplate.apppromotion.appPromotionConfigForTemplate
import com.devindie.cmptemplate.domain.usecase.carddetail.GetCardDetailUseCase
import com.devindie.cmptemplate.domain.usecase.onboarding.CompleteOnboardingUseCase
import com.devindie.cmptemplate.domain.usecase.onboarding.HasCompletedOnboardingUseCase
import com.devindie.cmptemplate.domain.usecase.startup.InitializeAppUseCase
import com.devindie.cmptemplate.domain.usecase.user.ClearUserSessionUseCase
import com.devindie.cmptemplate.domain.usecase.user.GetUserSessionUseCase
import com.devindie.cmptemplate.domain.usecase.user.SaveUserSessionUseCase
import com.devindie.cmptemplate.domain.usecase.settings.GetSettingUseCase
import com.devindie.cmptemplate.domain.usecase.settings.ObserveSettingUseCase
import com.devindie.cmptemplate.domain.usecase.settings.ObserveSettingsScreenUseCase
import com.devindie.cmptemplate.domain.usecase.settings.UpdateSettingUseCase
import com.devindie.cmptemplate.feature.apppromotion.api.appPromotionFeatureModule
import com.devindie.cmptemplate.feature.browse.api.browseFeatureModule
import com.devindie.cmptemplate.feature.carddetail.api.cardDetailFeatureModule
import com.devindie.cmptemplate.feature.collection.api.collectionFeatureModule
import com.devindie.cmptemplate.feature.main.api.mainFeatureModule
import com.devindie.cmptemplate.feature.onboarding.api.onboardingFeatureModule
import com.devindie.cmptemplate.feature.legal.api.legalFeatureModule
import com.devindie.cmptemplate.feature.settings.api.settingsFeatureModule
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
        factoryOf(::HasCompletedOnboardingUseCase)
        factoryOf(::CompleteOnboardingUseCase)
        factoryOf(::GetSettingUseCase)
        factory { ObserveSettingUseCase(get(), get()) }
        factory { ObserveSettingsScreenUseCase(get(), get()) }
        factory { UpdateSettingUseCase(get(), get()) }
        includes(
            browseFeatureModule,
            cardDetailFeatureModule,
            mainFeatureModule,
            collectionFeatureModule,
            splashFeatureModule,
            onboardingFeatureModule,
            legalFeatureModule,
            settingsFeatureModule,
            appPromotionFeatureModule(appPromotionConfigForTemplate()),
        )
    }
