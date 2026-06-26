package com.devindie.cmptemplate.domain.fake

import com.devindie.cmptemplate.domain.model.carddetail.CardDetail
import com.devindie.cmptemplate.domain.repository.CardDetailRepository

class FakeCardDetailRepository : CardDetailRepository {
    var getCardDetailResult: Result<CardDetail>? = null
    var lastRequestedCardId: Long? = null

    override suspend fun getCardDetail(cardId: Long): Result<CardDetail> {
        lastRequestedCardId = cardId
        return getCardDetailResult ?: Result.failure(IllegalStateException("No stubbed result"))
    }
}
