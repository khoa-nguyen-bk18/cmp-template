package com.devindie.cmptemplate.feature.apppromotion.impl

import com.devindie.cmptemplate.feature.apppromotion.api.AppPromotionClient
import com.devindie.cmptemplate.feature.apppromotion.api.AppPromotionResult
import com.devindie.cmptemplate.feature.apppromotion.impl.platform.AppPromotionPlatform

internal class AppPromotionClientImpl(
    private val platform: AppPromotionPlatform,
) : AppPromotionClient {
    override suspend fun requestInAppReview(): AppPromotionResult = platform.requestInAppReview()

    override suspend fun shareApp(): AppPromotionResult = platform.shareApp()
}
