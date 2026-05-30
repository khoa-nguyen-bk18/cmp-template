package com.devindie.cmptemplate.screens.main

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import com.devindie.cmptemplate.ui.insets.appNavigationBarsPadding
import com.devindie.cmptemplate.ui.insets.appStatusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.devindie.cmptemplate.screens.browse.BrowseScreen
import com.devindie.cmptemplate.ui.theme.AppTheme
import com.devindie.cmptemplate.ui.theme.LocalAppSpacing

/**
 * State-holder entry for the Stitch "Empty Nav Screen" shell (project 17128375841121903851).
 * Wires [NavHost] tab routes and delegates layout to the previewable UI overload.
 */
@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val selectedDestination = MainDestination.fromRoute(navBackStackEntry?.destination?.route)

    MainScreen(
        state = MainScreenUiState(
            storeName = DEFAULT_STORE_NAME,
            selectedDestination = selectedDestination,
        ),
        onDestinationSelected = { destination ->
            navController.navigate(destination.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        },
        onCartClick = {},
        modifier = modifier,
        tabContent = { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = MainDestination.Start.route,
                modifier = Modifier.fillMaxSize().padding(innerPadding)
                    .consumeWindowInsets(innerPadding),
            ) {
                MainDestination.entries.forEach { destination ->
                    composable(destination.route) {
                        when (destination) {
                            MainDestination.Browse -> BrowseScreen(
                                modifier = Modifier.fillMaxSize(),
                            )

                            else -> EmptyTabContent()
                        }
                    }
                }
            }
        },
    )
}

/**
 * Previewable UI for the empty navigation shell — no [NavController] or DI.
 */
@Composable
fun MainScreen(
    state: MainScreenUiState,
    onDestinationSelected: (MainDestination) -> Unit,
    onCartClick: () -> Unit,
    modifier: Modifier = Modifier,
    tabContent: @Composable (PaddingValues) -> Unit = { innerPadding ->
        EmptyTabContent(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        )
    },
) {
    val spacing = LocalAppSpacing.current

    Scaffold(
        modifier = modifier.testTag("main_screen"),
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
            ) {
                MainTopBar(
                    storeName = state.storeName,
                    onCartClick = onCartClick,
                    modifier = Modifier.appStatusBarsPadding().padding(
                        horizontal = spacing.screenMargin,
                        vertical = spacing.spaceSm,
                    ),
                )
            }
        },
        bottomBar = {
            ProCollectorBottomBar(
                selectedDestination = state.selectedDestination,
                onDestinationSelected = onDestinationSelected,
                modifier = Modifier.appNavigationBarsPadding(),
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { innerPadding ->
        tabContent(innerPadding)
    }
}

@Preview
@Composable
private fun MainScreenPreview() {
    AppTheme {
        MainScreen(
            state = MainScreenUiState(),
            onDestinationSelected = {},
            onCartClick = {},
        )
    }
}
