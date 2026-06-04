package com.devindie.cmptemplate.data.local.browse

import com.devindie.cmptemplate.data.local.browse.fake.FakeBrowseCardDao
import com.devindie.cmptemplate.data.source.local.browse.BrowseCardLocalDataSourceImpl
import com.devindie.cmptemplate.domain.model.browse.BrowseCategory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BrowseCardLocalDataSourceImplTest {
    private val dao = FakeBrowseCardDao()
    private val dataSource =
        BrowseCardLocalDataSourceImpl(
            dao
        )

    @Test
    fun observeCards_trimsQueryAndMapsEntities() = runTest {
        dao.setCards(
            listOf(
                sampleBrowseCardEntity(id = 1L, name = "Charizard ex"),
                sampleBrowseCardEntity(id = 2L, name = "Pikachu VMAX"),
            ),
        )

        val cards =
            dataSource
                .observeCards(
                    query = "  char  ",
                    category = BrowseCategory.All,
                ).first()

        assertEquals(1, cards.size)
        assertEquals("Charizard ex", cards.first().name)
        assertEquals("$189.99", cards.first().priceDisplay)
    }

    @Test
    fun getCardDetail_returnsNullWhenMissing() = runTest {
        assertNull(dataSource.getCardDetail(404L))
    }

    @Test
    fun getCardDetail_mapsEntityToCardDetail() = runTest {
        dao.setCards(listOf(sampleBrowseCardEntity(id = 7L)))

        val detail = dataSource.getCardDetail(7L)

        assertEquals(7L, detail?.id)
        assertEquals("Charizard ex", detail?.name)
    }
}
