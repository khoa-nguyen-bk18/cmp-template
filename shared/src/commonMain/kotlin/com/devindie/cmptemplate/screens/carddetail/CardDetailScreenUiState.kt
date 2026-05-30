package com.devindie.cmptemplate.screens.carddetail

import com.devindie.cmptemplate.domain.model.carddetail.CardCondition
import com.devindie.cmptemplate.domain.model.carddetail.CardDetail

data class CardDetailScreenUiState(
    val isLoading: Boolean = true,
    val card: CardDetail? = null,
    val selectedCondition: CardCondition = CardCondition.NearMint,
    val selectedPriceDisplay: String = "",
    val errorMessage: String? = null,
)
