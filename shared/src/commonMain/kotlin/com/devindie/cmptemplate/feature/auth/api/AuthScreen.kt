package com.devindie.cmptemplate.feature.auth.api

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.devindie.cmptemplate.feature.auth.impl.otp.OtpVerificationScreen
import com.devindie.cmptemplate.feature.auth.impl.phone.PhoneNumberScreen

@Composable
fun AuthScreen(
    onAuthComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = PhoneNumberRoute,
        modifier = modifier,
    ) {
        composable<PhoneNumberRoute> {
            PhoneNumberScreen(
                onNavigateToOtp = { route ->
                    navController.navigate(route)
                },
            )
        }
        composable<OtpVerificationRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<OtpVerificationRoute>()
            OtpVerificationScreen(
                route = route,
                onAuthComplete = onAuthComplete,
                onBackClick = { navController.popBackStack() },
            )
        }
    }
}
