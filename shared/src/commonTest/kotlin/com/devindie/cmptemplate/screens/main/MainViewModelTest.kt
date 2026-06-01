package com.devindie.cmptemplate.screens.main

import app.cash.turbine.test
import com.devindie.cmptemplate.test.advanceMainUntilIdle
import com.devindie.cmptemplate.test.runViewModelTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.test.Test
import kotlin.test.assertEquals

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
    fun onRouteChanged_updatesSelectedDestination() = runViewModelTest {
        val viewModel = MainViewModel()

        viewModel.onRouteChanged(MainDestination.Cart)

        assertEquals(MainDestination.Cart, viewModel.uiState.value.selectedDestination)
    }

    @Test
    fun onRouteChanged_sameDestinationIsNoOp() = runViewModelTest {
        val viewModel = MainViewModel()

        viewModel.onRouteChanged(MainDestination.Browse)
        viewModel.onRouteChanged(MainDestination.Browse)

        assertEquals(MainDestination.Browse, viewModel.uiState.value.selectedDestination)
    }
}
