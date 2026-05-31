package com.devindie.cmptemplate.domain.usecase.browse

import com.devindie.cmptemplate.domain.model.browse.BrowseCardsQuery
import com.devindie.cmptemplate.domain.model.browse.CollectibleCard
import com.devindie.cmptemplate.domain.repository.BrowseCardRepository
import com.devindie.cmptemplate.domain.usecase.UseCase
import kotlinx.coroutines.flow.Flow

class ObserveBrowseCardsUseCase(private val repository: BrowseCardRepository) :
    UseCase<BrowseCardsQuery, Flow<List<CollectibleCard>>> {
    override suspend fun invoke(parameters: BrowseCardsQuery): Flow<List<CollectibleCard>> = repository.observeCards(
        query = parameters.query,
        category = parameters.category,
    )
}
