package com.devindie.cmptemplate

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.devindie.cmptemplate.core.navigation.MainShellRoute
import com.devindie.cmptemplate.core.ui.theme.AppTheme
import com.devindie.cmptemplate.feature.main.api.MainScreen
import com.devindie.cmptemplate.feature.onboarding.api.OnboardingRoute
import com.devindie.cmptemplate.feature.onboarding.api.OnboardingScreen
import com.devindie.cmptemplate.feature.splash.api.SplashRoute
import com.devindie.cmptemplate.feature.splash.api.SplashScreen
import com.devindie.cmptemplate.navigation.navKeysSavedStateConfiguration

@Composable
fun App(modifier: Modifier = Modifier) {
    AppTheme {
        val backStack =
            rememberNavBackStack(
                configuration = navKeysSavedStateConfiguration,
                SplashRoute,
            )

        val entryProvider: (NavKey) -> NavEntry<NavKey> =
            remember {
                { key ->
                    when (key) {
                        SplashRoute ->
                            NavEntry(key) {
                                SplashScreen(
                                    onNavigateToMain = {
                                        backStack.run {
                                            clear()
                                            add(MainShellRoute)
                                        }
                                    },
                                    onNavigateToOnboarding = {
                                        backStack.run {
                                            clear()
                                            add(OnboardingRoute)
                                        }
                                    },
                                )
                            }

                        OnboardingRoute ->
                            NavEntry(key) {
                                OnboardingScreen(
                                    onNavigateToMain = {
                                        backStack.run {
                                            clear()
                                            add(MainShellRoute)
                                        }
                                    },
                                )
                            }

                        MainShellRoute ->
                            NavEntry(key) {
                                MainScreen()
                            }

                        else -> error("Unknown route: $key")
                    }
                }
            }

        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryProvider = entryProvider,
            modifier = modifier,
        )
    }
}
