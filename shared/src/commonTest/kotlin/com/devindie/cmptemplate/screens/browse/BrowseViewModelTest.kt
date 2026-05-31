package com.devindie.cmptemplate.screens.browse

import com.devindie.cmptemplate.domain.model.browse.BrowseCategory
import com.devindie.cmptemplate.domain.model.browse.CollectibleCard
import com.devindie.cmptemplate.domain.usecase.browse.EnsureBrowseCatalogSeededUseCase
import com.devindie.cmptemplate.domain.usecase.browse.ObserveBrowseCardsUseCase
import com.devindie.cmptemplate.fake.FakeBrowseCardRepository
import com.devindie.cmptemplate.test.advanceMainUntilIdle
import com.devindie.cmptemplate.test.runViewModelTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class BrowseViewModelTest {
    private suspend fun TestScope.drainBrowsePipeline() {
        repeat(5) {
            advanceMainUntilIdle()
            advanceTimeBy(300)
        }
        advanceMainUntilIdle()
    }

    @Test
    fun uiState_showsCardsAfterCatalogSeeds() = runViewModelTest {
        val repository = FakeBrowseCardRepository()
        repository.setCards(
            listOf(
                CollectibleCard(
                    id = 1L,
                    name = "Charizard ex",
                    setName = "Obsidian Flames",
                    condition = "NM",
                    priceDisplay = "$189.99",
                    quantity = 2,
                    category = BrowseCategory.Pokemon,
                ),
            ),
        )
        val viewModel =
            BrowseViewModel(
                observeBrowseCards = ObserveBrowseCardsUseCase(repository),
                ensureBrowseCatalogSeeded = EnsureBrowseCatalogSeededUseCase(repository),
            )

        drainBrowsePipeline()

        assertEquals(1, repository.ensureCatalogSeededCallCount)
        assertEquals(false, viewModel.uiState.value.isLoading)
        assertEquals(1, viewModel.uiState.value.cards.size)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun uiState_showsErrorWhenCatalogSeedFails() = runViewModelTest {
        val repository =
            FakeBrowseCardRepository().apply {
                ensureCatalogSeededResult = Result.failure(IllegalStateException("seed failed"))
            }
        val viewModel =
            BrowseViewModel(
                observeBrowseCards = ObserveBrowseCardsUseCase(repository),
                ensureBrowseCatalogSeeded = EnsureBrowseCatalogSeededUseCase(repository),
            )

        advanceMainUntilIdle()

        assertEquals("seed failed", viewModel.uiState.value.errorMessage)
        assertEquals(false, viewModel.uiState.value.isLoading)
    }

    @Test
    fun onSearchQueryChange_updatesDisplayQueryImmediately() = runViewModelTest {
        val repository = FakeBrowseCardRepository()
        val viewModel =
            BrowseViewModel(
                observeBrowseCards = ObserveBrowseCardsUseCase(repository),
                ensureBrowseCatalogSeeded = EnsureBrowseCatalogSeededUseCase(repository),
            )
        advanceMainUntilIdle()

        viewModel.onSearchQueryChange("char")
        advanceMainUntilIdle()

        assertEquals("char", viewModel.uiState.value.searchQuery)
    }

    @Test
    fun onSearchQueryChange_debouncesBeforeQueryingRepository() = runViewModelTest {
        val repository = FakeBrowseCardRepository()
        val viewModel =
            BrowseViewModel(
                observeBrowseCards = ObserveBrowseCardsUseCase(repository),
                ensureBrowseCatalogSeeded = EnsureBrowseCatalogSeededUseCase(repository),
            )
        drainBrowsePipeline()

        viewModel.onSearchQueryChange("char")
        assertEquals("", repository.lastObserveQuery)

        advanceTimeBy(300)
        advanceMainUntilIdle()

        assertEquals("char", repository.lastObserveQuery)
    }

    @Test
    fun onCategorySelected_forwardsCategoryToRepository() = runViewModelTest {
        val repository = FakeBrowseCardRepository()
        val viewModel =
            BrowseViewModel(
                observeBrowseCards = ObserveBrowseCardsUseCase(repository),
                ensureBrowseCatalogSeeded = EnsureBrowseCatalogSeededUseCase(repository),
            )
        drainBrowsePipeline()

        viewModel.onCategorySelected(BrowseCategory.Pokemon)
        advanceMainUntilIdle()

        assertEquals(BrowseCategory.Pokemon, viewModel.uiState.value.selectedCategory)
        assertEquals(BrowseCategory.Pokemon, repository.lastObserveCategory)
    }
}
