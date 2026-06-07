package com.devindie.cmptemplate.screens.browse

import androidx.paging.PagingData
import com.devindie.cmptemplate.domain.model.browse.BrowseCategory
import com.devindie.cmptemplate.domain.model.browse.CollectibleCard
import com.devindie.cmptemplate.test.advanceMainUntilIdle
import com.devindie.cmptemplate.test.runViewModelTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
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

        viewModel.onSearchQueryChange("char")
        viewModel.onCategorySelected(BrowseCategory.Pokemon)
        advanceMainUntilIdle()

        assertEquals("char", viewModel.uiState.value.searchQuery)
        assertEquals(BrowseCategory.Pokemon, viewModel.uiState.value.selectedCategory)
    }

    @Test
    fun onSearchQueryChange_updatesDisplayQueryImmediately() = runViewModelTest {
        val viewModel =
            BrowseViewModel(
                pagerFactory = BrowseCardPagerFactory { _ -> flowOf(PagingData.empty()) },
            )

        viewModel.onSearchQueryChange("char")
        advanceMainUntilIdle()

        assertEquals("char", viewModel.uiState.value.searchQuery)
    }

    @Test
    fun onCategorySelected_updatesSelectedCategoryInUiState() = runViewModelTest {
        val viewModel =
            BrowseViewModel(
                pagerFactory = BrowseCardPagerFactory { _ -> flowOf(PagingData.empty()) },
            )

        viewModel.onCategorySelected(BrowseCategory.Pokemon)
        advanceMainUntilIdle()

        assertEquals(BrowseCategory.Pokemon, viewModel.uiState.value.selectedCategory)
    }
}
