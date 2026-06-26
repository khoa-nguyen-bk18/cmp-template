package com.devindie.cmptemplate.feature.apppromotion.impl

import com.devindie.cmptemplate.feature.apppromotion.api.AppPromotionClient
import com.devindie.cmptemplate.feature.apppromotion.api.AppPromotionError
import com.devindie.cmptemplate.feature.apppromotion.api.AppPromotionResult

internal class NoOpAppPromotionClient : AppPromotionClient {
    override suspend fun requestInAppReview(): AppPromotionResult =
        AppPromotionResult.Failure(AppPromotionError.NotConfigured)

    override suspend fun shareApp(): AppPromotionResult =
        AppPromotionResult.Failure(AppPromotionError.NotConfigured)
}
