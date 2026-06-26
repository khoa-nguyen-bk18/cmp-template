package com.devindie.cmptemplate.feature.apppromotion.impl.platform

import com.devindie.cmptemplate.feature.apppromotion.api.AppPromotionResult

internal interface AppPromotionPlatform {
    suspend fun requestInAppReview(): AppPromotionResult

    suspend fun shareApp(): AppPromotionResult
}
