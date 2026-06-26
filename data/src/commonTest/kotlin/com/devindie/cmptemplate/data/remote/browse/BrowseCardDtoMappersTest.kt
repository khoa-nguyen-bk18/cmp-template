package com.devindie.cmptemplate.data.remote.browse

import com.devindie.cmptemplate.data.source.local.browse.BrowseCardEntity
import com.devindie.cmptemplate.data.source.remote.browse.toDto
import com.devindie.cmptemplate.data.source.remote.browse.toEntity
import kotlin.test.Test
import kotlin.test.assertEquals

class BrowseCardDtoMappersTest {
    @Test
    fun dtoEntityRoundTrip_preservesFields() {
        val entity =
            BrowseCardEntity(
                name = "Test Card",
                setName = "Test Set",
                condition = "NM",
                priceCents = 999,
                quantity = 2,
                category = "Pokemon",
                gameName = "Pokémon",
            )
        val roundTripped = entity.toDto().toEntity()

        assertEquals(entity.name, roundTripped.name)
        assertEquals(entity.setName, roundTripped.setName)
        assertEquals(entity.priceCents, roundTripped.priceCents)
        assertEquals(entity.category, roundTripped.category)
    }
}
