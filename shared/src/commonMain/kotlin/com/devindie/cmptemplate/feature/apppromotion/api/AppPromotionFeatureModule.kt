package com.devindie.cmptemplate.feature.apppromotion.api

import com.devindie.cmptemplate.feature.apppromotion.impl.AppPromotionClientImpl
import com.devindie.cmptemplate.feature.apppromotion.impl.NoOpAppPromotionClient
import com.devindie.cmptemplate.feature.apppromotion.impl.platform.appPromotionPlatformModule
import org.koin.core.module.Module
import org.koin.dsl.module

fun appPromotionFeatureModule(config: AppPromotionConfig): Module =
    module {
        single { config }
        includes(appPromotionPlatformModule())
        single<AppPromotionClient> {
            if (config.enabled) {
                AppPromotionClientImpl(platform = get())
            } else {
                NoOpAppPromotionClient()
            }
        }
    }
