package com.devindie.cmptemplate.domain.repository

import com.devindie.cmptemplate.domain.model.carddetail.CardDetail

interface CardDetailRepository {
    suspend fun getCardDetail(cardId: Long): Result<CardDetail>
}
