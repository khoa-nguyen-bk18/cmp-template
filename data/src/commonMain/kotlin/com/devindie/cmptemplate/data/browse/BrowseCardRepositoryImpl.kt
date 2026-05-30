package com.devindie.cmptemplate.data.browse

import com.devindie.cmptemplate.data.coroutines.DispatcherProvider
import com.devindie.cmptemplate.domain.model.browse.BrowseCategory
import com.devindie.cmptemplate.domain.model.browse.CollectibleCard
import com.devindie.cmptemplate.domain.repository.BrowseCardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class BrowseCardRepositoryImpl(
    private val localDataSource: BrowseCardLocalDataSource,
    private val dispatchers: DispatcherProvider,
) : BrowseCardRepository {
    override fun observeCards(
        query: String,
        category: BrowseCategory,
    ): Flow<List<CollectibleCard>> = localDataSource.observeCards(query, category)

    override suspend fun ensureCatalogSeeded(): Result<Unit> = withContext(dispatchers.io) {
        runCatching {
            if (localDataSource.count() == 0) {
                localDataSource.insertAll(BrowseCatalogSeeder.seedEntities())
            }
        }
    }
}
