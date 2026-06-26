package com.devindie.cmptemplate.feature.legal.impl.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal expect fun LegalWebView(
    url: String,
    reloadToken: Int,
    onPageStarted: () -> Unit,
    onPageFinished: () -> Unit,
    onError: () -> Unit,
    modifier: Modifier = Modifier,
)
