package com.devindie.cmptemplate.feature.apppromotion.impl

import com.devindie.cmptemplate.feature.apppromotion.api.AppPromotionResult
import com.devindie.cmptemplate.feature.apppromotion.impl.platform.AppPromotionPlatform
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppPromotionClientImplTest {
    @Test
    fun requestInAppReview_delegatesToPlatform() =
        runTest {
            var called = false
            val platform =
                object : AppPromotionPlatform {
                    override suspend fun requestInAppReview() =
                        AppPromotionResult.Success.also { called = true }

                    override suspend fun shareApp() = AppPromotionResult.Success
                }
            val client = AppPromotionClientImpl(platform)

            val result = client.requestInAppReview()

            assertEquals(AppPromotionResult.Success, result)
            assertTrue(called)
        }

    @Test
    fun shareApp_delegatesToPlatform() =
        runTest {
            var called = false
            val platform =
                object : AppPromotionPlatform {
                    override suspend fun requestInAppReview() = AppPromotionResult.Success

                    override suspend fun shareApp() =
                        AppPromotionResult.Success.also { called = true }
                }
            val client = AppPromotionClientImpl(platform)

            val result = client.shareApp()

            assertEquals(AppPromotionResult.Success, result)
            assertTrue(called)
        }
}
