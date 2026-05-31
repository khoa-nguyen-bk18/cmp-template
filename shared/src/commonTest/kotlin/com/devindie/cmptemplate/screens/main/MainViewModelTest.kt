package com.devindie.cmptemplate.screens.main

import app.cash.turbine.test
import com.devindie.cmptemplate.test.advanceMainUntilIdle
import com.devindie.cmptemplate.test.runViewModelTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {
    @Test
    fun onDestinationSelected_updatesStateAndEmitsNavigateEvent() = runViewModelTest {
        val viewModel = MainViewModel()

        viewModel.events.test {
            viewModel.onDestinationSelected(MainDestination.Cart)
            advanceMainUntilIdle()

            assertEquals(MainDestination.Cart, viewModel.uiState.value.selectedDestination)
            assertEquals(MainEvent.NavigateToTab(MainDestination.Cart), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onDestinationSelected_sameTabIsNoOp() = runViewModelTest {
        val viewModel = MainViewModel()

        viewModel.events.test {
            viewModel.onDestinationSelected(MainDestination.Browse)
            advanceMainUntilIdle()
            expectNoEvents()
        }
    }

    @Test
    fun onDestinationSelected_leavingBrowseClearsDetailCardId() = runViewModelTest {
        val viewModel = MainViewModel()
        viewModel.onCardClick(42L)

        viewModel.onDestinationSelected(MainDestination.Cart)
        advanceMainUntilIdle()

        assertNull(viewModel.uiState.value.detailCardId)
    }

    @Test
    fun onCardClick_setsDetailCardId() = runViewModelTest {
        val viewModel = MainViewModel()

        viewModel.onCardClick(7L)

        assertEquals(7L, viewModel.uiState.value.detailCardId)
    }

    @Test
    fun onCardDetailDismiss_clearsDetailCardId() = runViewModelTest {
        val viewModel = MainViewModel()
        viewModel.onCardClick(7L)

        viewModel.onCardDetailDismiss()

        assertNull(viewModel.uiState.value.detailCardId)
    }

    @Test
    fun visibleDetailCardId_onlyOnBrowseTab() = runViewModelTest {
        val viewModel = MainViewModel()
        viewModel.onCardClick(12L)

        assertEquals(12L, viewModel.uiState.value.visibleDetailCardId)

        viewModel.onDestinationSelected(MainDestination.Profile)
        advanceMainUntilIdle()

        assertNull(viewModel.uiState.value.visibleDetailCardId)
    }
}
