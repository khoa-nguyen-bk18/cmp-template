package com.devindie.cmptemplate

import androidx.compose.ui.window.ComposeUIViewController
import com.devindie.cmptemplate.billing.configureBillingPlatform

fun MainViewController() =
    ComposeUIViewController {
        configureBillingPlatform()
        doInitKoin()
        App()
    }
