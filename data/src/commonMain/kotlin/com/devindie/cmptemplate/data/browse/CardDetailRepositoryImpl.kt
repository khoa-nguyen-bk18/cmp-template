package com.devindie.cmptemplate.data.browse

import com.devindie.cmptemplate.data.coroutines.DispatcherProvider
import com.devindie.cmptemplate.domain.model.carddetail.CardDetail
import com.devindie.cmptemplate.domain.repository.CardDetailRepository
import kotlinx.coroutines.withContext

class CardDetailRepositoryImpl(
    private val localDataSource: BrowseCardLocalDataSource,
    private val dispatchers: DispatcherProvider,
) : CardDetailRepository {
    override suspend fun getCardDetail(cardId: Long): Result<CardDetail> =
        withContext(dispatchers.io) {
            runCatching {
                localDataSource.getCardDetail(cardId)
                    ?: error("Card not found")
            }
        }
}
