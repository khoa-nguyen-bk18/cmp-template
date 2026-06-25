package com.devindie.cmptemplate.feature.main.api

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import com.devindie.cmptemplate.core.navigation.MainRoute
import com.devindie.cmptemplate.feature.browse.api.browseEntry
import com.devindie.cmptemplate.feature.carddetail.api.CardDetailRoute
import com.devindie.cmptemplate.feature.carddetail.api.cardDetailEntry
import com.devindie.cmptemplate.feature.collection.api.collectionEntry
import com.devindie.cmptemplate.feature.main.impl.EmptyTabContent
import com.devindie.cmptemplate.navigation.navKeysSavedStateConfiguration

fun MainTabNavigator.navigateToMainTab(destination: MainDestination) {
    navigate(destination.route)
}

fun MainTabNavigator.navigateToCardDetail(cardId: Long) {
    navigate(CardDetailRoute(cardId = cardId))
}

fun MainTabNavigationState.selectedMainDestination(): MainDestination =
    MainDestination.entries.firstOrNull { it.route == topLevelRoute } ?: MainDestination.Start

fun mainTabEntryProvider(
    storeName: String,
    navigator: MainTabNavigator,
): (NavKey) -> NavEntry<NavKey> {
    val provider =
        entryProvider {
            browseEntry(onNavigateToCardDetail = navigator::navigateToCardDetail)
            entry<MainRoute.Cart> {
                EmptyTabContent(modifier = Modifier.fillMaxSize())
            }
            collectionEntry(onNavigateToCardDetail = navigator::navigateToCardDetail)
            entry<MainRoute.Profile> {
                EmptyTabContent(modifier = Modifier.fillMaxSize())
            }
            cardDetailEntry(
                storeName = storeName,
                onDismiss = navigator::goBack,
            )
        }
    return { key -> provider(key) }
}

@Composable
fun MainTabNavDisplay(
    navigationState: MainTabNavigationState,
    navigator: MainTabNavigator,
    innerPadding: PaddingValues,
    storeName: String,
    modifier: Modifier = Modifier,
) {
    val entryProvider = remember(storeName, navigator) { mainTabEntryProvider(storeName, navigator) }

    NavDisplay(
        entries = navigationState.toDecoratedEntries(entryProvider),
        onBack = navigator::goBack,
        sceneStrategies = remember { listOf(DialogSceneStrategy()) },
        modifier =
            modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
    )
}

@Composable
fun rememberMainTabNavigation(
    storeName: String = "",
): Pair<MainTabNavigationState, MainTabNavigator> {
    val navigationState =
        rememberMainTabNavigationState(
            savedStateConfiguration = navKeysSavedStateConfiguration,
        )
    val navigator = remember(navigationState) { MainTabNavigator(navigationState) }
    return navigationState to navigator
}
