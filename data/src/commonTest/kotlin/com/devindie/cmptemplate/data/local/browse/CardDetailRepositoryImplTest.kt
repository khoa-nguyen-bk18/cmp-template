package com.devindie.cmptemplate.data.local.browse

import com.devindie.cmptemplate.data.local.browse.fake.FakeBrowseCardDao
import com.devindie.cmptemplate.data.coroutines.runDataTest
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CardDetailRepositoryImplTest {
    @Test
    fun getCardDetail_returnsMappedDetail() = runDataTest { dispatchers ->
        val dao = FakeBrowseCardDao()
        dao.setCards(listOf(sampleBrowseCardEntity(id = 3L)))
        val repository =
            _root_ide_package_.com.devindie.cmptemplate.data.source.local.browse.CardDetailRepositoryImpl(
                localDataSource = _root_ide_package_.com.devindie.cmptemplate.data.source.local.browse.BrowseCardLocalDataSourceImpl(
                    dao
                ),
                dispatchers = dispatchers,
            )

        val result = repository.getCardDetail(3L)
        advanceUntilIdle()

        assertTrue(result.isSuccess)
        assertEquals(3L, result.getOrNull()?.id)
    }

    @Test
    fun getCardDetail_returnsFailureWhenMissing() = runDataTest { dispatchers ->
        val repository =
            _root_ide_package_.com.devindie.cmptemplate.data.source.local.browse.CardDetailRepositoryImpl(
                localDataSource = _root_ide_package_.com.devindie.cmptemplate.data.source.local.browse.BrowseCardLocalDataSourceImpl(
                    FakeBrowseCardDao()
                ),
                dispatchers = dispatchers,
            )

        val result = repository.getCardDetail(99L)
        advanceUntilIdle()

        assertTrue(result.isFailure)
        assertEquals("Card not found", result.exceptionOrNull()?.message)
    }
}
