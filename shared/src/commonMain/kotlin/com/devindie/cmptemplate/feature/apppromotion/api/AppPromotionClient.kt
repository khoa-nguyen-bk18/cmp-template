package com.devindie.cmptemplate.feature.apppromotion.api

interface AppPromotionClient {
    suspend fun requestInAppReview(): AppPromotionResult

    suspend fun shareApp(): AppPromotionResult
}
