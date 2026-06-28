package com.devindie.cmptemplate.feature.apppromotion.impl.platform

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import java.lang.ref.WeakReference

internal object AppPromotionContextHolder {
    private var activityRef: WeakReference<Activity>? = null

    fun bind(activity: Activity) {
        activityRef = WeakReference(activity)
    }
    fun clear(activity: Activity) {
        if (activityRef?.get() === activity) {
            activityRef = null
        }
    }
    fun currentActivity(): Activity? =
        activityRef?.get()?.takeUnless { it.isFinishing || it.isDestroyed }
}

internal fun Context.findActivity(): Activity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}
