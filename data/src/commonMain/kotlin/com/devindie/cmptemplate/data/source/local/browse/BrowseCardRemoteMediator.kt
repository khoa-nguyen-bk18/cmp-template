package com.devindie.cmptemplate.data.source.local.browse

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import co.touchlab.kermit.Logger
import com.devindie.cmptemplate.data.network.ApiResult
import com.devindie.cmptemplate.data.source.remote.browse.BrowseCardRemoteDataSource
import com.devindie.cmptemplate.data.source.remote.browse.toEntity
import kotlinx.coroutines.CancellationException

/**
 * Syncs paginated remote catalog pages into Room; local [BrowseCardDao.pagingSource] applies filters.
 *
 * @see BrowseCardPagerFactoryImpl
 * @see BrowseCardRemoteDataSource.fetchCatalogPage
 */
@OptIn(ExperimentalPagingApi::class)
class BrowseCardRemoteMediator(
    private val database: BrowseDatabase,
    private val remoteDataSource: BrowseCardRemoteDataSource,
) : RemoteMediator<Int, BrowseCardEntity>() {
    private val cardDao = database.browseCardDao()
    private val remoteKeyDao = database.browseRemoteKeyDao()

    override suspend fun initialize(): InitializeAction {
        return if (cardDao.count() > 0) {
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, BrowseCardEntity>,
    ): MediatorResult {
        return try {
            val page = when (loadType) {
                LoadType.REFRESH -> 1
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val remoteKey = remoteKeyDao.getRemoteKey(BrowseCatalogPaging.REMOTE_KEY)
                    remoteKey?.nextPage
                        ?: return MediatorResult.Success(endOfPaginationReached = true)
                }
            }

            when (val remote = remoteDataSource.fetchCatalogPage(page, state.config.pageSize)) {
                is ApiResult.Success -> {
                    val response = remote.data
                    database.useWriterConnection { transactor ->
                        transactor.immediateTransaction {
                            if (loadType == LoadType.REFRESH) {
                                remoteKeyDao.deleteAll()
                            }
                            cardDao.insertAll(response.cards.map { dto -> dto.toEntity() })
                            remoteKeyDao.insert(
                                BrowseRemoteKeyEntity(
                                    key = BrowseCatalogPaging.REMOTE_KEY,
                                    nextPage = if (response.pagination.hasMore) page + 1 else null,
                                ),
                            )
                        }
                    }
                    MediatorResult.Success(endOfPaginationReached = !response.pagination.hasMore)
                }

                else -> {
                    Logger.e("handleRemoteFailure: $loadType")
                    handleRemoteFailure(loadType)
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }

    private suspend fun handleRemoteFailure(loadType: LoadType): MediatorResult {
        if (loadType == LoadType.REFRESH && cardDao.count() == 0) {
            database.useWriterConnection { transactor ->
                transactor.immediateTransaction {
                    cardDao.insertAll(BrowseCatalogSeeder.seedEntities())
                    remoteKeyDao.insert(
                        BrowseRemoteKeyEntity(
                            key = BrowseCatalogPaging.REMOTE_KEY,
                            nextPage = null,
                        ),
                    )
                }
            }
            return MediatorResult.Success(endOfPaginationReached = true)
        }
        return MediatorResult.Error(IllegalStateException("Unable to load catalog from network"))
    }
}
