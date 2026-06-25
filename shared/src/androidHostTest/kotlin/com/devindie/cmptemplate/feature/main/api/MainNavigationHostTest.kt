package com.devindie.cmptemplate.feature.main.api

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import com.devindie.cmptemplate.core.navigation.MainRoute
import com.devindie.cmptemplate.feature.carddetail.api.CardDetailRoute
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class MainNavigationHostTest {
    @get:Rule
    val composeRule = createComposeRule()

    private fun createNavigationState(): MainTabNavigationState =
        MainTabNavigationState(
            startRoute = MainRoute.Browse,
            selectedDestinationState = androidx.compose.runtime.mutableStateOf(MainDestination.Start),
            backStacks =
                MainDestination.entries.associate { destination ->
                    destination.route to androidx.navigation3.runtime.NavBackStack(destination.route)
                },
            topLevelDestinations = MainDestination.entries.toSet(),
        )

    private fun setMainTabNavDisplay(
        navigationState: MainTabNavigationState,
        navigator: MainTabNavigator,
    ) {
        composeRule.setContent {
            TestMainTabNavDisplay(
                navigationState = navigationState,
                navigator = navigator,
            )
        }
    }

    @Test
    fun navigateToCart_updatesSelectedDestination() {
        val navigationState = createNavigationState()
        val navigator = MainTabNavigator(navigationState)
        setMainTabNavDisplay(navigationState, navigator)

        composeRule.runOnUiThread {
            navigator.navigateToMainTab(MainDestination.Cart)
        }
        composeRule.waitForIdle()

        composeRule.runOnUiThread {
            assertEquals(MainDestination.Cart, navigationState.selectedMainDestination())
        }
    }

    @Test
    fun cardDetailOverlay_keepsBrowseSelected() {
        val navigationState = createNavigationState()
        val navigator = MainTabNavigator(navigationState)
        setMainTabNavDisplay(navigationState, navigator)

        composeRule.runOnUiThread {
            navigator.navigateToCardDetail(cardId = 42L)
        }
        composeRule.waitForIdle()

        composeRule.runOnUiThread {
            assertEquals(MainDestination.Browse, navigationState.selectedMainDestination())
        }
    }

    @Test
    fun switchingTabFromCardDetail_popsOverlay() {
        val navigationState = createNavigationState()
        val navigator = MainTabNavigator(navigationState)
        setMainTabNavDisplay(navigationState, navigator)

        composeRule.runOnUiThread {
            navigator.navigateToCardDetail(cardId = 7L)
            navigator.navigateToMainTab(MainDestination.Cart)
        }
        composeRule.waitForIdle()

        composeRule.runOnUiThread {
            assertEquals(MainDestination.Cart, navigationState.selectedMainDestination())
            val currentStack = navigationState.backStacks[MainRoute.Cart]
            assertFalse(currentStack?.lastOrNull() is CardDetailRoute)
        }
    }
}

@Composable
private fun TestMainTabNavDisplay(
    navigationState: MainTabNavigationState,
    navigator: MainTabNavigator,
) {
    val entryProvider =
        remember {
            { key: NavKey ->
                NavEntry(key) {}
            }
        }

    NavDisplay(
        entries = navigationState.toDecoratedEntries(entryProvider),
        onBack = navigator::goBack,
        modifier = Modifier,
    )
}
