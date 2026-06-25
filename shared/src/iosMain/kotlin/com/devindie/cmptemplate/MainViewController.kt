package com.devindie.cmptemplate

import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = ComposeUIViewController {
    doInitKoin()
    App()
}
