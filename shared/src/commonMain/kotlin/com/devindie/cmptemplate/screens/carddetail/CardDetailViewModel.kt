package com.devindie.cmptemplate.screens.carddetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devindie.cmptemplate.domain.model.carddetail.CardCondition
import com.devindie.cmptemplate.domain.model.carddetail.CardDetail
import com.devindie.cmptemplate.domain.usecase.carddetail.GetCardDetailUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CardDetailViewModel(
    private val getCardDetail: GetCardDetailUseCase,
    private val cardId: Long,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CardDetailScreenUiState())
    val uiState: StateFlow<CardDetailScreenUiState> = _uiState.asStateFlow()

    init {
        loadCardDetail()
    }

    fun onConditionSelected(condition: CardCondition) {
        _uiState.update { state ->
            val card = state.card ?: return@update state
            state.copy(
                selectedCondition = condition,
                selectedPriceDisplay = card.priceFor(condition),
            )
        }
    }

    fun onAddToCartClick() = Unit

    fun onSellYoursClick() = Unit

    private fun loadCardDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            getCardDetail(cardId)
                .onSuccess { card ->
                    _uiState.update {
                        CardDetailScreenUiState(
                            isLoading = false,
                            card = card,
                            selectedCondition = card.listingCondition,
                            selectedPriceDisplay = card.priceFor(card.listingCondition),
                        )
                    }
                }
                .onFailure { error ->
                    if (error is CancellationException) throw error
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Unable to load card details",
                        )
                    }
                }
        }
    }

    private fun CardDetail.priceFor(condition: CardCondition): String =
        conditionPricing.firstOrNull { it.condition == condition }?.priceDisplay.orEmpty()
}
