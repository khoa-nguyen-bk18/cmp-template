package com.devindie.cmptemplate.data.browse

import com.devindie.cmptemplate.domain.model.browse.BrowseCategory
import kotlin.test.Test
import kotlin.test.assertEquals

class BrowseCardMappersTest {
    @Test
    fun toDomain_mapsPriceDisplayAndCategory() {
        val entity =
            sampleBrowseCardEntity(
                priceCents = 562,
                category = BrowseCategory.Magic,
            )

        val card = entity.toDomain()

        assertEquals("$5.62", card.priceDisplay)
        assertEquals(BrowseCategory.Magic, card.category)
    }

    @Test
    fun toDomain_unknownCategoryFallsBackToAll() {
        val entity =
            sampleBrowseCardEntity().copy(category = "UnknownCategory")

        val card = entity.toDomain()

        assertEquals(BrowseCategory.All, card.category)
    }
}
