package com.devindie.cmptemplate.feature.apppromotion.api

import androidx.compose.runtime.Composable
import com.devindie.cmptemplate.feature.apppromotion.impl.platform.BindAppPromotionPlatformContext

/** Mount once at app root so Android in-app review can resolve a foreground Activity. */
@Composable
fun AppPromotionPlatformBinding() {
    BindAppPromotionPlatformContext()
}
