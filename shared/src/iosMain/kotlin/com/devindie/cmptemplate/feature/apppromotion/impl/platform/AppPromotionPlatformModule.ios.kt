package com.devindie.cmptemplate.feature.apppromotion.impl.platform

import org.koin.core.module.Module
import org.koin.dsl.module

internal actual fun appPromotionPlatformModule(): Module =
    module {
        single<AppPromotionPlatform> {
            IosAppPromotionPlatform(config = get())
        }
    }
