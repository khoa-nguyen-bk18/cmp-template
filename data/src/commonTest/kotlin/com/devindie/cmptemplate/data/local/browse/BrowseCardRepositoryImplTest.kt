package com.devindie.cmptemplate.data.local.browse

import com.devindie.cmptemplate.data.coroutines.runDataTest
import com.devindie.cmptemplate.data.local.browse.fake.FakeBrowseCardDao
import com.devindie.cmptemplate.data.network.ApiResult
import com.devindie.cmptemplate.data.source.remote.browse.BrowseCardRemoteDataSource
import com.devindie.cmptemplate.data.source.remote.browse.FakeBrowseCardRemoteDataSource
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BrowseCardRepositoryImplTest {
    @Test
    fun ensureCatalogSeeded_syncsFromRemoteWhenDatabaseEmpty() = runDataTest { dispatchers ->
        val dao = FakeBrowseCardDao()
        val localDataSource =
            _root_ide_package_.com.devindie.cmptemplate.data.source.local.browse.BrowseCardLocalDataSourceImpl(
                dao
            )
        val repository =
            _root_ide_package_.com.devindie.cmptemplate.data.source.local.browse.BrowseCardRepositoryImpl(
                localDataSource = localDataSource,
                remoteDataSource = FakeBrowseCardRemoteDataSource(),
                dispatchers = dispatchers,
            )

        val result = repository.ensureCatalogSeeded()
        advanceUntilIdle()

        assertTrue(result.isSuccess)
        assertTrue(dao.count() > 0)
    }

    @Test
    fun ensureCatalogSeeded_replacesLocalCatalogWhenRemoteSucceeds() = runDataTest { dispatchers ->
        val dao = FakeBrowseCardDao()
        dao.setCards(listOf(sampleBrowseCardEntity()))
        val localDataSource =
            _root_ide_package_.com.devindie.cmptemplate.data.source.local.browse.BrowseCardLocalDataSourceImpl(
                dao
            )
        val repository =
            _root_ide_package_.com.devindie.cmptemplate.data.source.local.browse.BrowseCardRepositoryImpl(
                localDataSource = localDataSource,
                remoteDataSource = FakeBrowseCardRemoteDataSource(),
                dispatchers = dispatchers,
            )

        val result = repository.ensureCatalogSeeded()
        advanceUntilIdle()

        assertTrue(result.isSuccess)
        assertTrue(dao.count() > 1)
    }

    @Test
    fun ensureCatalogSeeded_keepsLocalDataWhenRemoteFailsAndDatabasePopulated() = runDataTest { dispatchers ->
        val dao = FakeBrowseCardDao()
        dao.setCards(listOf(sampleBrowseCardEntity()))
        val localDataSource =
            _root_ide_package_.com.devindie.cmptemplate.data.source.local.browse.BrowseCardLocalDataSourceImpl(
                dao
            )
        val repository =
            _root_ide_package_.com.devindie.cmptemplate.data.source.local.browse.BrowseCardRepositoryImpl(
                localDataSource = localDataSource,
                remoteDataSource = OfflineBrowseCardRemoteDataSource(),
                dispatchers = dispatchers,
            )
        val countBefore = dao.count()

        val result = repository.ensureCatalogSeeded()
        advanceUntilIdle()

        assertTrue(result.isSuccess)
        assertEquals(countBefore, dao.count())
    }

    @Test
    fun ensureCatalogSeeded_fallsBackToSeederWhenRemoteFailsAndDatabaseEmpty() = runDataTest { dispatchers ->
        val dao = FakeBrowseCardDao()
        val localDataSource =
            _root_ide_package_.com.devindie.cmptemplate.data.source.local.browse.BrowseCardLocalDataSourceImpl(
                dao
            )
        val repository =
            _root_ide_package_.com.devindie.cmptemplate.data.source.local.browse.BrowseCardRepositoryImpl(
                localDataSource = localDataSource,
                remoteDataSource = OfflineBrowseCardRemoteDataSource(),
                dispatchers = dispatchers,
            )

        val result = repository.ensureCatalogSeeded()
        advanceUntilIdle()

        assertTrue(result.isSuccess)
        assertTrue(dao.count() > 0)
    }
}

private class OfflineBrowseCardRemoteDataSource : BrowseCardRemoteDataSource {
    override suspend fun fetchCatalog(): ApiResult<List<com.devindie.cmptemplate.data.network.dto.BrowseCardDto>> =
        ApiResult.NetworkError(IllegalStateException("offline"))
}
