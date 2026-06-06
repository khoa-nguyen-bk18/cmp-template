package com.devindie.cmptemplate.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.devindie.cmptemplate.screens.browse.browseDestination
import com.devindie.cmptemplate.screens.carddetail.cardDetailDestination
import com.devindie.cmptemplate.screens.main.EmptyTabContent
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

fun NavGraphBuilder.cartDestination(
) {
    composable<MainRoute.Cart> {
        EmptyTabContent(modifier = Modifier.fillMaxSize())
    }
}

fun NavGraphBuilder.collectionDestination(
) {
    composable<MainRoute.Collection> {
        EmptyTabContent(modifier = Modifier.fillMaxSize())
    }
}

fun NavGraphBuilder.profileDestination(
) {
    composable<MainRoute.Profile> {
        EmptyTabContent(modifier = Modifier.fillMaxSize())
    }
}

fun NavGraphBuilder.mainTabNavGraph(
    storeName: String,
    onNavigateToCardDetail: (Long) -> Unit,
    onDismissCardDetail: () -> Unit,
) {
    browseDestination(
        onNavigateToCardDetail = onNavigateToCardDetail,
    )
    cartDestination()
    collectionDestination()
    profileDestination()
    cardDetailDestination(
        storeName = storeName,
        onDismiss = onDismissCardDetail,
    )
}

@Composable
fun MainTabNavHost(
    navController: NavHostController,
    innerPadding: PaddingValues,
    storeName: String,
    onNavigateToCardDetail: (Long) -> Unit,
    onDismissCardDetail: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = MainRoute.Browse,
        modifier = modifier.fillMaxSize().padding(innerPadding).consumeWindowInsets(innerPadding),
    ) {
        mainTabNavGraph(
            storeName = storeName,
            onNavigateToCardDetail = onNavigateToCardDetail,
            onDismissCardDetail = onDismissCardDetail,
        )
    }
}
