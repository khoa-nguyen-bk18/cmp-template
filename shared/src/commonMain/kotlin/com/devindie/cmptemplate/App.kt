package com.devindie.cmptemplate

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.devindie.cmptemplate.core.ui.theme.AppTheme
import com.devindie.cmptemplate.feature.main.MainScreen

@Composable
@Preview
fun App() {
    AppTheme {
        MainScreen()
    }
}
