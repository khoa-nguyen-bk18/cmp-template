package com.devindie.cmptemplate.feature.main.api

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.savedstate.serialization.SavedStateConfiguration
import com.devindie.cmptemplate.core.navigation.MainRoute

@Composable
fun rememberMainTabNavigationState(
    savedStateConfiguration: SavedStateConfiguration,
    startDestination: MainDestination = MainDestination.Start,
    topLevelDestinations: Set<MainDestination> = MainDestination.entries.toSet(),
): MainTabNavigationState {
    val selectedDestinationState = rememberSaveable { mutableStateOf(startDestination) }

    val startRoute = startDestination.route
    val topLevelRoutes: Set<NavKey> = topLevelDestinations.map { it.route }.toSet()
    val backStacks: Map<NavKey, NavBackStack<NavKey>> =
        topLevelRoutes.associateWith { key ->
            rememberNavBackStack(savedStateConfiguration, key)
        }

    return remember(startRoute, topLevelRoutes) {
        MainTabNavigationState(
            startRoute = startRoute,
            selectedDestinationState = selectedDestinationState,
            backStacks = backStacks,
            topLevelDestinations = topLevelDestinations,
        )
    }
}

class MainTabNavigationState(
    val startRoute: NavKey,
    private val selectedDestinationState: MutableState<MainDestination>,
    val backStacks: Map<NavKey, NavBackStack<NavKey>>,
    private val topLevelDestinations: Set<MainDestination>,
) {
    var selectedDestination: MainDestination
        get() = selectedDestinationState.value
        set(value) {
            selectedDestinationState.value = value
        }

    var topLevelRoute: NavKey
        get() = selectedDestination.route
        set(route) {
            topLevelDestinations.firstOrNull { it.route == route }?.let { destination ->
                selectedDestination = destination
            }
        }

    private val stacksInUse: List<NavKey>
        get() =
            if (topLevelRoute == startRoute) {
                listOf(startRoute)
            } else {
                listOf(startRoute, topLevelRoute)
            }

    @Composable
    fun toDecoratedEntries(entryProvider: (NavKey) -> NavEntry<NavKey>): List<NavEntry<NavKey>> {
        val decoratedEntries =
            backStacks.mapValues { (_, stack) ->
                val decorators =
                    listOf(
                        rememberSaveableStateHolderNavEntryDecorator<NavKey>(),
                    )
                rememberDecoratedNavEntries(
                    backStack = stack,
                    entryDecorators = decorators,
                    entryProvider = entryProvider,
                )
            }

        return stacksInUse.flatMap { decoratedEntries[it] ?: emptyList() }
    }
}
