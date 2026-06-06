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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import co.touchlab.kermit.Logger
import com.devindie.cmptemplate.navigation.MainRoute
import com.devindie.cmptemplate.navigation.navigateToMainTab
import com.devindie.cmptemplate.navigation.selectedMainDestination
import com.devindie.cmptemplate.screens.browse.BrowseScreen
import com.devindie.cmptemplate.screens.carddetail.CardDetailBottomSheet
import com.devindie.cmptemplate.ui.insets.appNavigationBarsPadding
import com.devindie.cmptemplate.ui.insets.appStatusBarsPadding
import com.devindie.cmptemplate.ui.theme.AppTheme
import com.devindie.cmptemplate.ui.theme.LocalAppSpacing
import org.koin.compose.viewmodel.koinViewModel

/**
 * State-holder entry for the Stitch "Empty Nav Screen" shell (project 17128375841121903851).
 * Collects [MainViewModel] state and wires type-safe navigation for tabs and card detail.
 */
@Composable
fun MainScreen(modifier: Modifier = Modifier, viewModel: MainViewModel = koinViewModel()) {
    val navController = rememberNavController()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val selectedDestination = selectedMainDestination(
        currentDestination = navBackStackEntry?.destination,
        previousDestination = navController.previousBackStackEntry?.destination,
    )

    MainScreen(
        state = state,
        selectedDestination = selectedDestination,
        onDestinationSelected = { destination ->
            if (destination != selectedDestination) {
                navController.navigateToMainTab(destination)
            }
        },
        onCartClick = {
            Logger.e("Open Cart Screen")
        },
        modifier = modifier,
        tabContent = { innerPadding ->
            MainTabNavHost(
                navController = navController,
                innerPadding = innerPadding,
                storeName = state.storeName,
                onNavigateToCardDetail = { cardId ->
                    navController.navigate(MainRoute.CardDetail(cardId = cardId))
                },
                onDismissCardDetail = navController::popBackStack,
            )
        },
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
        composable<MainRoute.Browse> {
            BrowseScreen(
                modifier = Modifier.fillMaxSize(),
                onCardClick = { card -> onNavigateToCardDetail(card.id) },
            )
        }
        composable<MainRoute.Cart> {
            EmptyTabContent(modifier = Modifier.fillMaxSize())
        }
        composable<MainRoute.Collection> {
            EmptyTabContent(modifier = Modifier.fillMaxSize())
        }
        composable<MainRoute.Profile> {
            EmptyTabContent(modifier = Modifier.fillMaxSize())
        }
        dialog<MainRoute.CardDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<MainRoute.CardDetail>()
            CardDetailBottomSheet(
                cardId = route.cardId,
                storeName = storeName,
                onDismiss = onDismissCardDetail,
            )
        }
    }
}

/**
 * Previewable UI for the empty navigation shell — no [NavHostController], ViewModel, or DI.
 */
@Composable
fun MainScreen(
    state: MainScreenUiState,
    selectedDestination: MainDestination,
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
            BottomNavigationBar(
                selectedDestination = selectedDestination,
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
            selectedDestination = MainDestination.Start,
            onDestinationSelected = {},
            onCartClick = {},
        )
    }
}
