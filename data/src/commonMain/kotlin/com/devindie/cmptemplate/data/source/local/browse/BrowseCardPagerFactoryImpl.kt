package com.devindie.cmptemplate.data.source.local.browse

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.devindie.cmptemplate.data.source.remote.browse.BrowseCardRemoteDataSource
import com.devindie.cmptemplate.domain.model.browse.BrowseCardsQuery
import com.devindie.cmptemplate.domain.model.browse.CollectibleCard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Builds [Pager] instances for the Browse screen (RemoteMediator + filtered Room [PagingSource]).
 *
 * Bound as [com.devindie.cmptemplate.screens.browse.BrowseCardPagerFactory] at the app composition root.
 *
 * @see BrowseCardRemoteMediator
 */
class BrowseCardPagerFactoryImpl(
    private val database: BrowseDatabase,
    private val remoteDataSource: BrowseCardRemoteDataSource,
) {
    private val pagingConfig =
        PagingConfig(
            pageSize = BrowseCatalogPaging.PAGE_SIZE,
            prefetchDistance = BrowseCatalogPaging.PREFETCH_DISTANCE,
            enablePlaceholders = false,
            initialLoadSize = BrowseCatalogPaging.INITIAL_LOAD_SIZE,
            maxSize = BrowseCatalogPaging.MAX_SIZE,
        )

    @OptIn(ExperimentalPagingApi::class)
    fun pages(query: BrowseCardsQuery): Flow<PagingData<CollectibleCard>> {
        val cardDao = database.browseCardDao()
        return Pager(
            config = pagingConfig,
            remoteMediator =
                BrowseCardRemoteMediator(
                    database = database,
                    remoteDataSource = remoteDataSource,
                ),
            pagingSourceFactory = {
                cardDao.pagingSource(
                    query = query.query.trim(),
                    category = query.category.name,
                )
            },
        ).flow.map { pagingData ->
            pagingData.map { entity -> entity.toDomain() }
        }
    }
}
