package com.devindie.cmptemplate.feature.apppromotion.impl.platform

import com.devindie.cmptemplate.feature.apppromotion.api.AppPromotionConfig
import com.devindie.cmptemplate.feature.apppromotion.api.AppPromotionError
import com.devindie.cmptemplate.feature.apppromotion.api.AppPromotionResult
import platform.StoreKit.SKStoreReviewController
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

internal class IosAppPromotionPlatform(
    private val config: AppPromotionConfig,
) : AppPromotionPlatform {
    override suspend fun requestInAppReview(): AppPromotionResult =
        runCatching {
            SKStoreReviewController.requestReview()
            AppPromotionResult.Success
        }.getOrElse {
            AppPromotionResult.Failure(AppPromotionError.Unknown)
        }

    override suspend fun shareApp(): AppPromotionResult {
        val rootViewController =
            UIApplication.sharedApplication.keyWindow?.rootViewController
                ?: return AppPromotionResult.Failure(AppPromotionError.PlatformUnavailable)

        val shareText =
            buildString {
                append(config.resolvedShareMessage())
                append('\n')
                append(config.appStoreUrl)
            }
        val controller = UIActivityViewController(listOf(shareText), null)
        return runCatching {
            rootViewController.presentViewController(controller, animated = true, completion = null)
            AppPromotionResult.Success
        }.getOrElse {
            AppPromotionResult.Failure(AppPromotionError.PlatformUnavailable)
        }
    }
}
