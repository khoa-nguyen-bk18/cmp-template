package com.devindie.cmptemplate

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.devindie.cmptemplate.core.navigation.MainShellRoute
import com.devindie.cmptemplate.core.ui.theme.AppTheme
import com.devindie.cmptemplate.feature.main.api.MainScreen
import com.devindie.cmptemplate.feature.splash.api.SplashRoute
import com.devindie.cmptemplate.feature.splash.api.SplashScreen

@Composable
@Preview
fun App(modifier: Modifier = Modifier) {
    AppTheme {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = SplashRoute,
            modifier = modifier,
        ) {
            composable<SplashRoute> {
                SplashScreen(
                    onNavigateToMain = {
                        navController.navigate(MainShellRoute) {
                            popUpTo<SplashRoute> { inclusive = true }
                        }
                    },
                )
            }
            composable<MainShellRoute> {
                MainScreen()
            }
        }
    }
}
