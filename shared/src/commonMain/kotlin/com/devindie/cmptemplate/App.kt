package com.devindie.cmptemplate

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.devindie.cmptemplate.screens.main.MainScreen
import com.devindie.cmptemplate.ui.theme.AppTheme

@Composable
@Preview
fun App() {
    AppTheme {
        MainScreen()
    }
}
