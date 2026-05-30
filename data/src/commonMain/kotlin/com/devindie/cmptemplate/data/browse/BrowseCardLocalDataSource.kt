package com.devindie.cmptemplate.data.browse

import com.devindie.cmptemplate.domain.model.browse.BrowseCategory
import com.devindie.cmptemplate.domain.model.browse.CollectibleCard
import com.devindie.cmptemplate.domain.model.carddetail.CardDetail
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface BrowseCardLocalDataSource {
    fun observeCards(
        query: String,
        category: BrowseCategory,
    ): Flow<List<CollectibleCard>>

    suspend fun count(): Int

    suspend fun insertAll(cards: List<BrowseCardEntity>)

    suspend fun getCardDetail(cardId: Long): CardDetail?
}

class BrowseCardLocalDataSourceImpl(
    private val dao: BrowseCardDao,
) : BrowseCardLocalDataSource {
    override fun observeCards(
        query: String,
        category: BrowseCategory,
    ): Flow<List<CollectibleCard>> =
        dao.observeFiltered(
            query = query.trim(),
            category = category.name,
        ).map { entities -> entities.map { it.toDomain() } }

    override suspend fun count(): Int = dao.count()

    override suspend fun insertAll(cards: List<BrowseCardEntity>) {
        dao.insertAll(cards)
    }

    override suspend fun getCardDetail(cardId: Long): CardDetail? =
        dao.getById(cardId)?.toCardDetail()
}
