package com.devindie.cmptemplate.data.source.local.browse

import com.devindie.cmptemplate.data.coroutines.DispatcherProvider
import com.devindie.cmptemplate.data.coroutines.runIoResult
import com.devindie.cmptemplate.data.network.ApiResult
import com.devindie.cmptemplate.data.source.remote.browse.BrowseCardRemoteDataSource
import com.devindie.cmptemplate.data.source.remote.browse.toEntity
import com.devindie.cmptemplate.domain.model.browse.BrowseCategory
import com.devindie.cmptemplate.domain.model.browse.CollectibleCard
import com.devindie.cmptemplate.domain.repository.BrowseCardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Room is the SSOT for [observeCards]; [ensureCatalogSeeded] syncs remote → local with offline fallback.
 */
class BrowseCardRepositoryImpl(
    private val localDataSource: BrowseCardLocalDataSource,
    private val remoteDataSource: BrowseCardRemoteDataSource,
    private val dispatchers: DispatcherProvider,
) : BrowseCardRepository {
    override fun observeCards(query: String, category: BrowseCategory): Flow<List<CollectibleCard>> =
        localDataSource.observeCards(query, category)

    override suspend fun ensureCatalogSeeded(): Result<Unit> = withContext(dispatchers.io) {
        runIoResult {
            when (val remote = remoteDataSource.fetchCatalog()) {
                is ApiResult.Success -> {
                    localDataSource.replaceAllCatalog(
                        remote.data.map { dto -> dto.toEntity() },
                    )
                }
                //insert seedEntities locally for fake data test
                else -> {
                    if (localDataSource.count() == 0) {
                        localDataSource.insertAll(BrowseCatalogSeeder.seedEntities())
                    }
                }
            }
        }
    }
}
