package com.devindie.cmptemplate.ui.insets

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.Modifier

actual fun Modifier.appStatusBarsPadding(): Modifier = statusBarsPadding()

actual fun Modifier.appNavigationBarsPadding(): Modifier = navigationBarsPadding()
