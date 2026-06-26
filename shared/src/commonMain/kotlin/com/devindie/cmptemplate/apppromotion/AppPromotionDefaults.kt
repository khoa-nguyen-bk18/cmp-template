package com.devindie.cmptemplate.apppromotion

import com.devindie.cmptemplate.feature.apppromotion.api.AppPromotionConfig

fun appPromotionConfigForTemplate(): AppPromotionConfig =
    AppPromotionConfig(
        enabled = true,
        appDisplayName = "Cmp Template",
        playStoreUrl = "https://play.google.com/store/apps/details?id=com.devindie.cmptemplate",
        appStoreUrl = "https://apps.apple.com/app/id000000000",
    )
