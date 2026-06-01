package com.devindie.cmptemplate.data.remote.browse

import com.devindie.cmptemplate.data.network.ApiResult
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class FakeBrowseCardRemoteDataSourceTest {
    @Test
    fun fetchCatalog_returnsSuccessWithCards() = runTest {
        val dataSource =
            _root_ide_package_.com.devindie.cmptemplate.data.source.remote.browse.FakeBrowseCardRemoteDataSource()

        val result = dataSource.fetchCatalog()

        assertTrue(result is ApiResult.Success)
        assertTrue((result as ApiResult.Success).data.isNotEmpty())
    }
}
