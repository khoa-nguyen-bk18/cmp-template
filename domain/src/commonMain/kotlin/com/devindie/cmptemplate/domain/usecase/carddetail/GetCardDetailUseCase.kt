package com.devindie.cmptemplate.domain.usecase.carddetail

import com.devindie.cmptemplate.domain.model.carddetail.CardDetail
import com.devindie.cmptemplate.domain.repository.CardDetailRepository

class GetCardDetailUseCase(private val repository: CardDetailRepository) {
    suspend operator fun invoke(cardId: Long): Result<CardDetail> = repository.getCardDetail(cardId)
}
