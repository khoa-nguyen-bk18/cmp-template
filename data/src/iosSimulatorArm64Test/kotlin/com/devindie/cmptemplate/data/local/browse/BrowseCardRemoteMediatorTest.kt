package com.devindie.cmptemplate.data.local.browse

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingState
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.devindie.cmptemplate.data.coroutines.runDataTest
import com.devindie.cmptemplate.data.network.ApiResult
import com.devindie.cmptemplate.data.network.dto.BrowseCardDto
import com.devindie.cmptemplate.data.network.dto.BrowseCatalogPageDto
import com.devindie.cmptemplate.data.network.dto.BrowsePaginationDto
import com.devindie.cmptemplate.data.source.local.browse.BrowseCardEntity
import com.devindie.cmptemplate.data.source.local.browse.BrowseCardRemoteMediator
import com.devindie.cmptemplate.data.source.local.browse.BrowseCatalogPaging
import com.devindie.cmptemplate.data.source.local.browse.BrowseDatabase
import com.devindie.cmptemplate.data.source.remote.browse.BrowseCardRemoteDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import platform.Foundation.NSTemporaryDirectory
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalPagingApi::class)
class BrowseCardRemoteMediatorTest {
    @Test
    fun refresh_insertsFirstPageAndRemoteKey() = runDataTest { dispatchers ->
        val database = createTestBrowseDatabase(dispatchers.io)
        val remote = FakePagedBrowseCardRemoteDataSource()
        val mediator = BrowseCardRemoteMediator(database, remote)
        val state = emptyPagingState()

        val result = mediator.load(LoadType.REFRESH, state)
        advanceUntilIdle()

        assertTrue(result is androidx.paging.RemoteMediator.MediatorResult.Success)
        assertTrue(database.browseCardDao().count() > 0)
        val remoteKey = database.browseRemoteKeyDao().getRemoteKey(BrowseCatalogPaging.REMOTE_KEY)
        assertEquals(2, remoteKey?.nextPage)
    }

    @Test
    fun append_fetchesNextPage() = runDataTest { dispatchers ->
        val database = createTestBrowseDatabase(dispatchers.io)
        val remote = FakePagedBrowseCardRemoteDataSource()
        val mediator = BrowseCardRemoteMediator(database, remote)
        val state = emptyPagingState()

        mediator.load(LoadType.REFRESH, state)
        val appendResult = mediator.load(LoadType.APPEND, state)
        advanceUntilIdle()

        assertTrue(appendResult is androidx.paging.RemoteMediator.MediatorResult.Success)
        assertEquals(2, remote.lastRequestedPage)
    }

    @Test
    fun refresh_seedsLocallyWhenRemoteFailsAndDatabaseEmpty() = runDataTest { dispatchers ->
        val database = createTestBrowseDatabase(dispatchers.io)
        val mediator = BrowseCardRemoteMediator(database, OfflinePagedBrowseCardRemoteDataSource())
        val state = emptyPagingState()

        val result = mediator.load(LoadType.REFRESH, state)
        advanceUntilIdle()

        assertTrue(result is androidx.paging.RemoteMediator.MediatorResult.Success)
        assertTrue(database.browseCardDao().count() > 0)
    }

    private fun emptyPagingState(): PagingState<Int, BrowseCardEntity> = PagingState(
        pages = emptyList(),
        anchorPosition = null,
        config = PagingConfig(pageSize = BrowseCatalogPaging.PAGE_SIZE),
        leadingPlaceholderCount = 0,
    )

    private fun createTestBrowseDatabase(ioDispatcher: CoroutineDispatcher): BrowseDatabase {
        val dbPath = "${NSTemporaryDirectory()}browse-test-${Random.nextInt()}.db"
        return Room.databaseBuilder<BrowseDatabase>(name = dbPath)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(ioDispatcher)
            .build()
    }
}

private class FakePagedBrowseCardRemoteDataSource : BrowseCardRemoteDataSource {
    var lastRequestedPage: Int = 0

    override suspend fun fetchCatalogPage(page: Int, pageSize: Int): ApiResult<BrowseCatalogPageDto> {
        lastRequestedPage = page
        val cards =
            List(pageSize) { index ->
                BrowseCardDto(
                    name = "Card ${(page - 1) * pageSize + index}",
                    setName = "Set",
                    condition = "NM",
                    priceCents = 100,
                    quantity = 1,
                    category = "Pokemon",
                )
            }
        return ApiResult.Success(
            BrowseCatalogPageDto(
                cards = cards,
                pagination =
                BrowsePaginationDto(
                    page = page,
                    pageSize = pageSize,
                    hasMore = page < 3,
                ),
            ),
        )
    }
}

private class OfflinePagedBrowseCardRemoteDataSource : BrowseCardRemoteDataSource {
    override suspend fun fetchCatalogPage(page: Int, pageSize: Int): ApiResult<BrowseCatalogPageDto> =
        ApiResult.NetworkError(IllegalStateException("offline"))
}
