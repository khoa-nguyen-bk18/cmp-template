package com.devindie.cmptemplate.data.browse

import com.devindie.cmptemplate.domain.model.browse.BrowseCategory
import kotlin.test.Test
import kotlin.test.assertEquals

class PriceFormattingTest {
    @Test
    fun formatPriceCents_wholeDollars_omitsCents() {
        assertEquals("$5", formatPriceCents(500))
    }

    @Test
    fun formatPriceCents_withCents_padsToTwoDigits() {
        assertEquals("$5.62", formatPriceCents(562))
        assertEquals("$5.05", formatPriceCents(505))
    }

    @Test
    fun scaledPriceCents_appliesRatio() {
        assertEquals(480L, scaledPriceCents(baseCents = 500, numerator = 96))
        assertEquals(250L, scaledPriceCents(baseCents = 500, numerator = 50))
    }
}

fun sampleBrowseCardEntity(
    id: Long = 1L,
    name: String = "Charizard ex",
    setName: String = "Obsidian Flames",
    condition: String = "NM",
    priceCents: Long = 18_999,
    quantity: Int = 2,
    category: BrowseCategory = BrowseCategory.Pokemon,
    gameName: String = "",
    rarityLabel: String = "",
    editionLabel: String = "",
    marketPriceCents: Long = 0,
    buylistPriceCents: Long = 0,
    lpPriceCents: Long = 0,
    mpPriceCents: Long = 0,
    hpPriceCents: Long = 0,
    dPriceCents: Long = 0,
): BrowseCardEntity = BrowseCardEntity(
    id = id,
    name = name,
    setName = setName,
    condition = condition,
    priceCents = priceCents,
    quantity = quantity,
    category = category.name,
    gameName = gameName,
    rarityLabel = rarityLabel,
    editionLabel = editionLabel,
    marketPriceCents = marketPriceCents,
    buylistPriceCents = buylistPriceCents,
    lpPriceCents = lpPriceCents,
    mpPriceCents = mpPriceCents,
    hpPriceCents = hpPriceCents,
    dPriceCents = dPriceCents,
)
