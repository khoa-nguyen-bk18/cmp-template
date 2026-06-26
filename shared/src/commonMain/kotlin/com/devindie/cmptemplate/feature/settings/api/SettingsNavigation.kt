package com.devindie.cmptemplate.feature.settings.api

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

fun NavGraphBuilder.settingsDestination(onBack: () -> Unit) {
    composable<SettingsRoute> {
        SettingsScreen(onBack = onBack)
    }
}
