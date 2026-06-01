package com.devindie.cmptemplate.screens.main

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.devindie.cmptemplate.navigation.MainNavRoutes
import com.devindie.cmptemplate.navigation.cardIdArg
import com.devindie.cmptemplate.screens.browse.BrowseScreen
import com.devindie.cmptemplate.screens.carddetail.CardDetailBottomSheet
import com.devindie.cmptemplate.ui.insets.appNavigationBarsPadding
import com.devindie.cmptemplate.ui.insets.appStatusBarsPadding
import com.devindie.cmptemplate.ui.theme.AppTheme
import com.devindie.cmptemplate.ui.theme.LocalAppSpacing
import org.koin.compose.viewmodel.koinViewModel

/**
 * State-holder entry for the Stitch "Empty Nav Screen" shell (project 17128375841121903851).
 * Collects [MainViewModel] state/events and delegates layout to the previewable UI overload.
 */
@Composable
fun MainScreen(modifier: Modifier = Modifier, viewModel: MainViewModel = koinViewModel()) {
    val navController = rememberNavController()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    MainScreenEffects(
        viewModel = viewModel,
        navController = navController,
        selectedDestination = state.selectedDestination,
    )

    MainScreen(
        state = state,
        onDestinationSelected = viewModel::onDestinationSelected,
        onCartClick = viewModel::onCartClick,
        modifier = modifier,
        tabContent = { innerPadding ->
            MainTabNavHost(
                navController = navController,
                innerPadding = innerPadding,
                storeName = state.storeName,
            )
        },
    )
}

@Composable
private fun MainScreenEffects(
    viewModel: MainViewModel,
    navController: NavHostController,
    selectedDestination: MainDestination,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val routeDestination = MainDestination.fromRoute(currentRoute)

    LaunchedEffect(routeDestination) {
        viewModel.onRouteChanged(routeDestination)
    }

    LaunchedEffect(selectedDestination) {
        if (selectedDestination != MainDestination.Browse && MainNavRoutes.isCardDetailRoute(
                currentRoute
            )
        ) {
            navController.popBackStack()
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is MainEvent.NavigateToTab -> {
                    navController.navigate(event.destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        }
    }
}

@Composable
private fun MainTabNavHost(
    navController: NavHostController,
    innerPadding: PaddingValues,
    storeName: String,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = MainDestination.Start.route,
        modifier = modifier.fillMaxSize().padding(innerPadding).consumeWindowInsets(innerPadding),
    ) {
        MainDestination.entries.forEach { destination ->
            composable(destination.route) {
                when (destination) {
                    MainDestination.Browse -> BrowseScreen(
                        modifier = Modifier.fillMaxSize(),
                        onCardClick = { card ->
                            navController.navigate(MainNavRoutes.cardDetailRoute(card.id))
                        },
                    )

                    else -> EmptyTabContent()
                }
            }
        }

        dialog(
            route = MainNavRoutes.CardDetailPattern,
            arguments = listOf(
                navArgument(MainNavRoutes.CardIdArg) { type = NavType.LongType },
            ),
        ) { backStackEntry ->
            CardDetailBottomSheet(
                cardId = backStackEntry.cardIdArg(),
                storeName = storeName,
                onDismiss = { navController.popBackStack() },
            )
        }
    }
}

/**
 * Previewable UI for the empty navigation shell — no [NavController], ViewModel, or DI.
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
