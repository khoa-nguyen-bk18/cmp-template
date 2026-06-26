package com.devindie.cmptemplate.feature.apppromotion.impl.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

@Composable
internal actual fun BindAppPromotionPlatformContext() {
    val context = LocalContext.current
    DisposableEffect(context) {
        AppPromotionContextHolder.context = context
        onDispose {
            if (AppPromotionContextHolder.context == context) {
                AppPromotionContextHolder.context = null
            }
        }
    }
}
