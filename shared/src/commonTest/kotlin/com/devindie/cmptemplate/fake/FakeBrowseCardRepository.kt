package com.devindie.cmptemplate.fake

import com.devindie.cmptemplate.domain.model.browse.BrowseCategory
import com.devindie.cmptemplate.domain.model.browse.CollectibleCard
import com.devindie.cmptemplate.domain.repository.BrowseCardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeBrowseCardRepository : BrowseCardRepository {
    var ensureCatalogSeededResult: Result<Unit> = Result.success(Unit)
    var ensureCatalogSeededCallCount: Int = 0
    var lastObserveQuery: String? = null
    var lastObserveCategory: BrowseCategory? = null

    private val cards = MutableStateFlow<List<CollectibleCard>>(emptyList())

    fun setCards(value: List<CollectibleCard>) {
        cards.value = value
    }

    override fun observeCards(query: String, category: BrowseCategory): Flow<List<CollectibleCard>> {
        lastObserveQuery = query
        lastObserveCategory = category
        return cards
    }

    override suspend fun ensureCatalogSeeded(): Result<Unit> {
        ensureCatalogSeededCallCount++
        return ensureCatalogSeededResult
    }
}
