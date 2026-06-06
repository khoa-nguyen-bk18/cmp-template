package com.devindie.cmptemplate.navigation

import kotlinx.serialization.Serializable

/** Type-safe routes for the main bottom-nav [NavHost][androidx.navigation.compose.NavHost]. */
@Serializable
sealed interface MainRoute {
    @Serializable
    data object Browse : MainRoute

    @Serializable
    data object Cart : MainRoute

    @Serializable
    data object Collection : MainRoute

    @Serializable
    data object Profile : MainRoute
}
