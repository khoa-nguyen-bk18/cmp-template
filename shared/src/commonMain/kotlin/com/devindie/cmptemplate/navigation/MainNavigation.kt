package com.devindie.cmptemplate.navigation

import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.devindie.cmptemplate.screens.main.MainDestination

fun NavHostController.navigateToMainTab(destination: MainDestination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

fun NavHostController.selectedMainDestination(): MainDestination {
    val currentDestination = currentBackStackEntry?.destination
    val previousDestination = previousBackStackEntry?.destination

    return selectedMainDestination(
        currentDestination = currentDestination,
        previousDestination = previousDestination,
    )
}

internal fun selectedMainDestination(
    currentDestination: NavDestination?,
    previousDestination: NavDestination?,
): MainDestination {
    destinationFor(currentDestination)?.let { return it }
    destinationFor(previousDestination)?.let { return it }
    return MainDestination.Start
}

private fun destinationFor(destination: NavDestination?): MainDestination? =
    MainDestination.entries.firstOrNull { tab ->
        destination?.hasRoute(tab.route::class) == true
    }
