package com.devindie.cmptemplate.feature.legal.impl.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

@Composable
internal actual fun rememberOpenUrlExternally(): (String) -> Unit =
    remember {
        { url ->
            NSURL.URLWithString(url)?.let { nsUrl ->
                UIApplication.sharedApplication.openURL(nsUrl)
            }
        }
    }
