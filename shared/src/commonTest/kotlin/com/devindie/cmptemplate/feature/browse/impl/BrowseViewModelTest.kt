package com.devindie.cmptemplate.feature.browse.impl

import androidx.paging.PagingData
import app.cash.turbine.test
import com.devindie.cmptemplate.domain.model.browse.BrowseCategory
import com.devindie.cmptemplate.feature.browse.api.BrowseCardPagerFactory
import com.devindie.cmptemplate.test.advanceMainUntilIdle
import com.devindie.cmptemplate.test.runViewModelTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class BrowseViewModelTest {
    @Test
    fun uiState_exposesSearchAndCategory() = runViewModelTest {
        val viewModel =
            BrowseViewModel(
                pagerFactory = { _ -> flowOf(PagingData.empty()) },
            )

        viewModel.uiState.test {
            awaitItem()

            viewModel.onSearchQueryChange("char")
            advanceMainUntilIdle()
            assertEquals("char", awaitItem().searchQuery)

            viewModel.onCategorySelected(BrowseCategory.Pokemon)
            advanceMainUntilIdle()
            val state = awaitItem()
            assertEquals("char", state.searchQuery)
            assertEquals(BrowseCategory.Pokemon, state.selectedCategory)
        }
        advanceMainUntilIdle()
    }

    @Test
    fun onSearchQueryChange_updatesDisplayQueryImmediately() = runViewModelTest {
        val viewModel =
            BrowseViewModel(
                pagerFactory = BrowseCardPagerFactory { _ -> flowOf(PagingData.empty()) },
            )

        viewModel.uiState.test {
            awaitItem()

            viewModel.onSearchQueryChange("char")
            advanceMainUntilIdle()

            assertEquals("char", awaitItem().searchQuery)
        }
        advanceMainUntilIdle()
    }

    @Test
    fun onCategorySelected_updatesSelectedCategoryInUiState() = runViewModelTest {
        val viewModel =
            BrowseViewModel(
                pagerFactory = BrowseCardPagerFactory { _ -> flowOf(PagingData.empty()) },
            )

        viewModel.uiState.test {
            awaitItem()

            viewModel.onCategorySelected(BrowseCategory.Pokemon)
            advanceMainUntilIdle()

            assertEquals(BrowseCategory.Pokemon, awaitItem().selectedCategory)
        }
        advanceMainUntilIdle()
    }
}
