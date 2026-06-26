package com.devindie.cmptemplate.feature.apppromotion.impl.platform

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

internal object AppPromotionContextHolder {
    var context: Context? = null
}

internal fun Context.findActivity(): Activity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}
