package com.devindie.cmptemplate.data.browse

import com.devindie.cmptemplate.data.browse.fake.FakeBrowseCardDao
import com.devindie.cmptemplate.data.coroutines.runDataTest
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BrowseCardRepositoryImplTest {
    @Test
    fun ensureCatalogSeeded_insertsWhenDatabaseEmpty() = runDataTest { dispatchers ->
        val dao = FakeBrowseCardDao()
        val localDataSource = BrowseCardLocalDataSourceImpl(dao)
        val repository = BrowseCardRepositoryImpl(localDataSource, dispatchers)

        val result = repository.ensureCatalogSeeded()
        advanceUntilIdle()

        assertTrue(result.isSuccess)
        assertTrue(dao.count() > 0)
    }

    @Test
    fun ensureCatalogSeeded_skipsInsertWhenDatabaseHasRows() = runDataTest { dispatchers ->
        val dao = FakeBrowseCardDao()
        dao.setCards(listOf(sampleBrowseCardEntity()))
        val localDataSource = BrowseCardLocalDataSourceImpl(dao)
        val repository = BrowseCardRepositoryImpl(localDataSource, dispatchers)
        val countBefore = dao.count()

        val result = repository.ensureCatalogSeeded()
        advanceUntilIdle()

        assertTrue(result.isSuccess)
        assertEquals(countBefore, dao.count())
    }
}
