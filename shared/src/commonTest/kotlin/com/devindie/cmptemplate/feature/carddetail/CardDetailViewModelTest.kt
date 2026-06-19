package com.devindie.cmptemplate.feature.carddetail

import com.devindie.cmptemplate.domain.model.carddetail.CardCondition
import com.devindie.cmptemplate.domain.usecase.carddetail.GetCardDetailUseCase
import com.devindie.cmptemplate.fake.FakeCardDetailRepository
import com.devindie.cmptemplate.fake.sampleCardDetail
import com.devindie.cmptemplate.test.advanceMainUntilIdle
import com.devindie.cmptemplate.test.runViewModelTest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class CardDetailViewModelTest {
    @Test
    fun uiState_loadsCardDetailOnInit() = runViewModelTest {
        val repository = FakeCardDetailRepository().apply {
            getCardDetailResult = Result.success(sampleCardDetail(id = 5L))
        }
        val viewModel = CardDetailViewModel(
            getCardDetail = GetCardDetailUseCase(repository),
            cardId = 5L,
        )

        advanceMainUntilIdle()

        assertEquals(false, viewModel.uiState.value.isLoading)
        assertEquals(5L, viewModel.uiState.value.card?.id)
        assertEquals(CardCondition.NearMint, viewModel.uiState.value.selectedCondition)
        assertEquals("$189.99", viewModel.uiState.value.selectedPriceDisplay)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun uiState_showsErrorWhenLoadFails() = runViewModelTest {
        val repository = FakeCardDetailRepository().apply {
            getCardDetailResult = Result.failure(IllegalStateException("Card not found"))
        }
        val viewModel = CardDetailViewModel(
            getCardDetail = GetCardDetailUseCase(repository),
            cardId = 99L,
        )

        advanceMainUntilIdle()

        assertEquals(false, viewModel.uiState.value.isLoading)
        assertEquals("Card not found", viewModel.uiState.value.errorMessage)
        assertNull(viewModel.uiState.value.card)
    }

    @Test
    fun uiState_notShowErrorWhenCancellation() = runViewModelTest {
        val repository = FakeCardDetailRepository().apply {
            getCardDetailResult = Result.failure(CancellationException("cancelled"))
        }
        val viewModel = CardDetailViewModel(
            getCardDetail = GetCardDetailUseCase(repository),
            cardId = 1L,
        )

        advanceMainUntilIdle()

        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun onConditionSelected_updatesSelectedPriceDisplay() = runViewModelTest {
        val repository = FakeCardDetailRepository().apply {
            getCardDetailResult = Result.success(sampleCardDetail())
        }
        val viewModel = CardDetailViewModel(
            getCardDetail = GetCardDetailUseCase(repository),
            cardId = 1L,
        )
        advanceMainUntilIdle()

        viewModel.onConditionSelected(CardCondition.LightlyPlayed)

        assertEquals(CardCondition.LightlyPlayed, viewModel.uiState.value.selectedCondition)
        assertEquals("$163.99", viewModel.uiState.value.selectedPriceDisplay)
    }
}
