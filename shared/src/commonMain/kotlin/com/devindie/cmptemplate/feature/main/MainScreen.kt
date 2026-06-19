package com.devindie.cmptemplate.feature.main

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
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import co.touchlab.kermit.Logger
import com.devindie.cmptemplate.core.ui.insets.appNavigationBarsPadding
import com.devindie.cmptemplate.core.ui.insets.appStatusBarsPadding
import com.devindie.cmptemplate.core.ui.theme.AppTheme
import com.devindie.cmptemplate.core.ui.theme.LocalAppSpacing
import com.devindie.cmptemplate.feature.carddetail.navigateToCardDetail
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
                onNavigateToCardDetail = navController::navigateToCardDetail,
                onDismissCardDetail = navController::popBackStack,
            )
        },
    )
}

/**
 * Previewable UI for the empty navigation shell — no [androidx.navigation.NavHostController], ViewModel, or DI.
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
