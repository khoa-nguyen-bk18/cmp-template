package com.devindie.cmptemplate.data.browse

import com.devindie.cmptemplate.domain.model.browse.BrowseCategory

internal object BrowseCatalogSeeder {
    fun seedEntities(): List<BrowseCardEntity> =
        listOf(
            card("Charizard ex", "Obsidian Flames", "NM", 189.99, 2, BrowseCategory.Pokemon),
            card("Pikachu VMAX", "Vivid Voltage", "LP", 45.0, 1, BrowseCategory.Pokemon),
            card("Mewtwo GX", "Shining Legends", "NM", 32.5, 3, BrowseCategory.Pokemon),
            card("Black Lotus", "Alpha", "HP", 12_500.0, 1, BrowseCategory.Magic),
            card("Lightning Bolt", "Beta", "NM", 120.0, 4, BrowseCategory.Magic),
            card("Counterspell", "Ice Age", "NM", 8.5, 6, BrowseCategory.Magic),
            card("Michael Jordan RC", "Fleer '86", "NM", 2_200.0, 1, BrowseCategory.Sports),
            card("Patrick Mahomes Prizm", "2017 Prizm", "NM", 425.0, 2, BrowseCategory.Sports),
        )

    private fun card(
        name: String,
        setName: String,
        condition: String,
        priceDollars: Double,
        quantity: Int,
        category: BrowseCategory,
    ): BrowseCardEntity =
        BrowseCardEntity(
            name = name,
            setName = setName,
            condition = condition,
            priceCents = (priceDollars * 100).toLong(),
            quantity = quantity,
            category = category.name,
        )
}
