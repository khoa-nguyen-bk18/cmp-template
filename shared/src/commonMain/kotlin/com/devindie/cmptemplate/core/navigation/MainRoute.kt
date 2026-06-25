package com.devindie.cmptemplate.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** Type-safe routes for the main bottom-nav shell. */
@Serializable
sealed interface MainRoute : NavKey {
    @Serializable
    data object Browse : MainRoute

    @Serializable
    data object Cart : MainRoute

    @Serializable
    data object Collection : MainRoute

    @Serializable
    data object Profile : MainRoute
}
