package com.devindie.cmptemplate.feature.apppromotion.impl.platform

import android.content.Context
import android.content.Intent
import com.devindie.cmptemplate.feature.apppromotion.api.AppPromotionConfig
import com.devindie.cmptemplate.feature.apppromotion.api.AppPromotionError
import com.devindie.cmptemplate.feature.apppromotion.api.AppPromotionResult
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.tasks.await

internal class AndroidAppPromotionPlatform(
    private val applicationContext: Context,
    private val config: AppPromotionConfig,
) : AppPromotionPlatform {
    override suspend fun requestInAppReview(): AppPromotionResult {
        val activity =
            AppPromotionContextHolder.context?.findActivity()
                ?: applicationContext.findActivity()
                ?: return AppPromotionResult.Failure(AppPromotionError.PlatformUnavailable)

        return runCatching {
            val manager = ReviewManagerFactory.create(activity)
            val reviewInfo = manager.requestReviewFlow().await()
            manager.launchReviewFlow(activity, reviewInfo).await()
            AppPromotionResult.Success
        }.getOrElse {
            AppPromotionResult.Failure(AppPromotionError.Unknown)
        }
    }

    override suspend fun shareApp(): AppPromotionResult {
        val context = AppPromotionContextHolder.context ?: applicationContext
        return runCatching {
            val shareText =
                buildString {
                    append(config.resolvedShareMessage())
                    append('\n')
                    append(config.playStoreUrl)
                }
            val intent =
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            context.startActivity(
                Intent.createChooser(intent, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
            AppPromotionResult.Success
        }.getOrElse {
            AppPromotionResult.Failure(AppPromotionError.PlatformUnavailable)
        }
    }
}
