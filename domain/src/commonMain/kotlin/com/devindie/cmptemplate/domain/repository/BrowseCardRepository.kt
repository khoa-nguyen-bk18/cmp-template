package com.devindie.cmptemplate.domain.repository

import com.devindie.cmptemplate.domain.model.browse.BrowseCategory
import com.devindie.cmptemplate.domain.model.browse.CollectibleCard
import kotlinx.coroutines.flow.Flow

interface BrowseCardRepository {
    fun observeCards(query: String, category: BrowseCategory): Flow<List<CollectibleCard>>

    suspend fun ensureCatalogSeeded(): Result<Unit>
}
