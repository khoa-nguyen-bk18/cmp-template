package com.devindie.cmptemplate.feature.main.api

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.DialogNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.testing.TestNavHostController
import androidx.paging.PagingData
import androidx.test.core.app.ApplicationProvider
import com.devindie.cmptemplate.core.navigation.MainRoute
import com.devindie.cmptemplate.feature.browse.impl.BrowseScreen
import com.devindie.cmptemplate.feature.browse.impl.BrowseViewModel
import com.devindie.cmptemplate.feature.carddetail.api.CardDetailRoute
import com.devindie.cmptemplate.feature.carddetail.api.navigateToCardDetail
import com.devindie.cmptemplate.feature.main.impl.EmptyTabContent
import kotlinx.coroutines.flow.flowOf
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

    private fun createStubBrowseViewModel(): BrowseViewModel =
        BrowseViewModel(pagerFactory = { _ -> flowOf(PagingData.empty()) })

    private fun NavGraphBuilder.mainTabNavGraphForTest(browseViewModel: BrowseViewModel) {
        composable<MainRoute.Browse> {
            BrowseScreen(
                modifier = Modifier.fillMaxSize(),
                viewModel = browseViewModel,
            )
        }
        cartDestination()
        composable<MainRoute.Collection> {
            EmptyTabContent(modifier = Modifier.fillMaxSize())
        }
        composable<MainRoute.Profile> {
            EmptyTabContent(modifier = Modifier.fillMaxSize())
        }
        dialog<CardDetailRoute> {
            Box(modifier = Modifier.fillMaxSize())
        }
    }

    private fun setMainTabNavHost(navController: TestNavHostController) {
        val browseViewModel = createStubBrowseViewModel()
        composeRule.setContent {
            NavHost(
                navController = navController,
                startDestination = MainRoute.Browse,
            ) {
                mainTabNavGraphForTest(browseViewModel = browseViewModel)
            }
        }
    }

    @Test
    fun navigateToCart_updatesSelectedDestination() {
        val navController = createNavController()
        setMainTabNavHost(navController)

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
        setMainTabNavHost(navController)

        composeRule.runOnUiThread {
            navController.navigateToCardDetail(cardId = 42L)
        }
        composeRule.waitForIdle()

        composeRule.runOnUiThread {
            assertEquals(MainDestination.Browse, navController.selectedMainDestination())
        }
    }

    @Test
    fun switchingTabFromCardDetail_popsOverlay() {
        val navController = createNavController()
        setMainTabNavHost(navController)

        composeRule.runOnUiThread {
            navController.navigateToCardDetail(cardId = 7L)
            navController.navigateToMainTab(MainDestination.Cart)
        }
        composeRule.waitForIdle()

        composeRule.runOnUiThread {
            assertEquals(MainDestination.Cart, navController.selectedMainDestination())
            assertFalse(
                navController.currentBackStackEntry?.destination
                    ?.hasRoute(CardDetailRoute::class) == true,
            )
        }
    }
}
