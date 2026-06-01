package com.devindie.cmptemplate.data.local.browse

import com.devindie.cmptemplate.data.source.local.browse.toCardDetail
import com.devindie.cmptemplate.domain.model.browse.BrowseCategory
import com.devindie.cmptemplate.domain.model.carddetail.CardCondition
import kotlin.test.Test
import kotlin.test.assertEquals

class CardDetailMappersTest {
    @Test
    fun toCardDetail_usesExplicitTierPricesWhenPresent() {
        val entity =
            sampleBrowseCardEntity(
                marketPriceCents = 18_039,
                buylistPriceCents = 11_019,
                lpPriceCents = 16_399,
                mpPriceCents = 13_109,
                hpPriceCents = 7_219,
                dPriceCents = 3_609,
            )

        val detail = entity.toCardDetail()

        assertEquals("$180.39", detail.marketPriceDisplay)
        assertEquals("$110.19", detail.buylistPriceDisplay)
        assertEquals(
            "$163.99",
            detail.conditionPricing.first {
                it.condition == CardCondition.LightlyPlayed
            }.priceDisplay,
        )
    }

    @Test
    fun toCardDetail_scalesMissingTierPricesFromNearMint() {
        val entity = sampleBrowseCardEntity(priceCents = 1_000)

        val detail = entity.toCardDetail()

        assertEquals("$9.60", detail.marketPriceDisplay)
        assertEquals("$5.80", detail.buylistPriceDisplay)
        assertEquals(
            "$8.60",
            detail.conditionPricing.first {
                it.condition == CardCondition.LightlyPlayed
            }.priceDisplay,
        )
    }

    @Test
    fun toCardDetail_fillsBlankMetadataDefaults() {
        val entity =
            sampleBrowseCardEntity(
                gameName = "",
                rarityLabel = "",
                editionLabel = "",
                category = BrowseCategory.Pokemon,
            )

        val detail = entity.toCardDetail()

        assertEquals("Pokémon", detail.gameName)
        assertEquals("Common", detail.rarityLabel)
        assertEquals("Normal Edition", detail.editionLabel)
        assertEquals(CardCondition.NearMint, detail.listingCondition)
    }
}
