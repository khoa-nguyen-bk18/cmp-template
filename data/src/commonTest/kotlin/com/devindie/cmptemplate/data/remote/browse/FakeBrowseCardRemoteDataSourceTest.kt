package com.devindie.cmptemplate.data.remote.browse

import com.devindie.cmptemplate.data.network.ApiResult
import com.devindie.cmptemplate.data.source.remote.browse.FakeBrowseCardRemoteDataSource
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FakeBrowseCardRemoteDataSourceTest {
    @Test
    fun fetchCatalogPage_returnsFirstPageWithMore() = runTest {
        val dataSource = FakeBrowseCardRemoteDataSource()
        val pageSize = 20

        val result = dataSource.fetchCatalogPage(page = 1, pageSize = pageSize)

        assertTrue(result is ApiResult.Success)
        val page = result.data
        assertEquals(20, page.cards.size)
        assertTrue(page.pagination.hasMore)
        assertEquals(1, page.pagination.page)
    }

    @Test
    fun fetchCatalogPage_terminalPageHasNoMore() = runTest {
        val dataSource = FakeBrowseCardRemoteDataSource()
        val pageSize = 20
        var pageNum = 1
        var terminalPage = dataSource.fetchCatalogPage(pageNum, pageSize)
        while (terminalPage is ApiResult.Success && terminalPage.data.pagination.hasMore) {
            pageNum++
            terminalPage = dataSource.fetchCatalogPage(pageNum, pageSize)
        }

        assertTrue(terminalPage is ApiResult.Success)
        val page = terminalPage.data
        assertTrue(page.cards.isNotEmpty())
        assertEquals(false, page.pagination.hasMore)
    }

    @Test
    fun fetchCatalogPage_returnsEmptyWhenBeyondCatalog() = runTest {
        val dataSource = FakeBrowseCardRemoteDataSource()

        val result = dataSource.fetchCatalogPage(page = 99, pageSize = 20)

        assertTrue(result is ApiResult.Success)
        val page = result.data
        assertTrue(page.cards.isEmpty())
        assertEquals(false, page.pagination.hasMore)
    }
}
