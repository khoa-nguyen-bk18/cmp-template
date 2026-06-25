package com.devindie.cmptemplate.feature.legal.impl.platform

import androidx.compose.runtime.Composable

@Composable
internal expect fun rememberOpenUrlExternally(): (String) -> Unit
