package com.devindie.cmptemplate.navigation

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.DialogNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.devindie.cmptemplate.screens.main.MainDestination
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

    private fun createNavController(): TestNavHostController {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        navController.navigatorProvider.addNavigator(ComposeNavigator())
        navController.navigatorProvider.addNavigator(DialogNavigator())
        return navController
    }

    @Test
    fun navigateToCart_updatesSelectedDestination() {
        val navController = createNavController()

        composeRule.setContent {
            NavHost(
                navController = navController,
                startDestination = MainRoute.Browse,
            ) {
                composable<MainRoute.Browse> { }
                composable<MainRoute.Cart> { }
                composable<MainRoute.Collection> { }
                composable<MainRoute.Profile> { }
                dialog<MainRoute.CardDetail> { }
            }
        }

        composeRule.runOnUiThread {
            navController.navigateToMainTab(MainDestination.Cart)
        }
        composeRule.waitForIdle()

        composeRule.runOnUiThread {
            assertEquals(MainDestination.Cart, navController.selectedMainDestination())
        }
    }

    @Test
    fun cardDetailOverlay_keepsBrowseSelected() {
        val navController = createNavController()

        composeRule.setContent {
            NavHost(
                navController = navController,
                startDestination = MainRoute.Browse,
            ) {
                composable<MainRoute.Browse> { }
                composable<MainRoute.Cart> { }
                composable<MainRoute.Collection> { }
                composable<MainRoute.Profile> { }
                dialog<MainRoute.CardDetail> { }
            }
        }

        composeRule.runOnUiThread {
            navController.navigate(MainRoute.CardDetail(cardId = 42L))
        }
        composeRule.waitForIdle()

        composeRule.runOnUiThread {
            assertEquals(MainDestination.Browse, navController.selectedMainDestination())
        }
    }

    @Test
    fun switchingTabFromCardDetail_popsOverlay() {
        val navController = createNavController()

        composeRule.setContent {
            NavHost(
                navController = navController,
                startDestination = MainRoute.Browse,
            ) {
                composable<MainRoute.Browse> { }
                composable<MainRoute.Cart> { }
                composable<MainRoute.Collection> { }
                composable<MainRoute.Profile> { }
                dialog<MainRoute.CardDetail> { }
            }
        }

        composeRule.runOnUiThread {
            navController.navigate(MainRoute.CardDetail(cardId = 7L))
            navController.navigateToMainTab(MainDestination.Cart)
        }
        composeRule.waitForIdle()

        composeRule.runOnUiThread {
            assertEquals(MainDestination.Cart, navController.selectedMainDestination())
            assertFalse(navController.currentBackStackEntry?.destination?.hasRoute(MainRoute.CardDetail::class) == true)
        }
    }
}
