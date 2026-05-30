package com.devindie.cmptemplate.data.browse

import com.devindie.cmptemplate.domain.model.browse.BrowseCategory

internal object BrowseCatalogSeeder {
    fun seedEntities(): List<BrowseCardEntity> =
        listOf(
            detailCard(
                name = "Gaeas Touch",
                setName = "The Dark",
                condition = "NM",
                priceDollars = 5.62,
                quantity = 3,
                category = BrowseCategory.Magic,
                rarityLabel = "Uncommon #77/119",
                abilitiesText =
                    "You may put one additional land in play during each of your turns, " +
                        "but that land must be a basic forest. You may sacrifice Gaeas Touch to add " +
                        "GG to your mana pool. This ability is played as an interrupt.",
                flavorText =
                    "\"The forest provides for those who cherish its roots as much as its leaves.\"",
                marketPriceDollars = 5.40,
                buylistPriceDollars = 3.25,
                lpPriceDollars = 4.85,
                mpPriceDollars = 3.90,
                hpPriceDollars = 2.15,
                dPriceDollars = 1.05,
            ),
            card("Charizard ex", "Obsidian Flames", "NM", 189.99, 2, BrowseCategory.Pokemon),
            card("Pikachu VMAX", "Vivid Voltage", "LP", 45.0, 1, BrowseCategory.Pokemon),
            card("Mewtwo GX", "Shining Legends", "NM", 32.5, 3, BrowseCategory.Pokemon),
            card("Blastoise", "Base Set", "NM", 89.0, 4, BrowseCategory.Pokemon),
            card("Venusaur", "Base Set", "LP", 67.5, 2, BrowseCategory.Pokemon),
            card("Lugia", "Neo Genesis", "NM", 240.0, 1, BrowseCategory.Pokemon),
            card("Rayquaza EX", "Roaring Skies", "NM", 30.0, 5, BrowseCategory.Pokemon),
            card("Umbreon GX", "Sun & Moon", "NM", 64.5, 2, BrowseCategory.Pokemon),
            card("Espeon GX", "Sun & Moon", "LP", 58.0, 1, BrowseCategory.Pokemon),
            card("Charizard VMAX", "Champion's Path", "NM", 390.0, 1, BrowseCategory.Pokemon),
            card("Gengar", "Fossil", "HP", 25.0, 4, BrowseCategory.Pokemon),
            card("Snorlax", "Jungle", "NM", 12.5, 2, BrowseCategory.Pokemon),
            card("Eevee", "Hidden Fates", "NM", 8.0, 6, BrowseCategory.Pokemon),
            card("Sylveon VMAX", "Evolving Skies", "NM", 185.0, 2, BrowseCategory.Pokemon),
            card("Inteleon VMAX", "Sword & Shield", "NM", 21.0, 3, BrowseCategory.Pokemon),
            card("Zacian V", "Sword & Shield", "NM", 25.0, 3, BrowseCategory.Pokemon),
            card("Marnie", "Shining Fates", "NM", 41.0, 1, BrowseCategory.Pokemon),
            card("Cynthia", "Hidden Fates", "NM", 32.0, 2, BrowseCategory.Pokemon),
            card("Arceus VSTAR", "Brilliant Stars", "NM", 49.0, 2, BrowseCategory.Pokemon),
            card("Garchomp C Lv.X", "Platinum: Supreme Victors", "LP", 34.5, 1, BrowseCategory.Pokemon),
            //
            card("Black Lotus", "Alpha", "HP", 12_500.0, 1, BrowseCategory.Magic),
            card("Lightning Bolt", "Beta", "NM", 120.0, 4, BrowseCategory.Magic),
            card("Counterspell", "Ice Age", "NM", 8.5, 6, BrowseCategory.Magic),
            card("Time Walk", "Alpha", "HP", 8000.0, 1, BrowseCategory.Magic),
            card("Ancestral Recall", "Unlimited", "PLD", 7200.0, 1, BrowseCategory.Magic),
            card("Mox Sapphire", "Beta", "NM", 5400.0, 1, BrowseCategory.Magic),
            card("Tarmogoyf", "Future Sight", "NM", 45.0, 3, BrowseCategory.Magic),
            card("Liliana of the Veil", "Innistrad", "LP", 60.0, 2, BrowseCategory.Magic),
            card("Snapcaster Mage", "Innistrad", "NM", 39.0, 4, BrowseCategory.Magic),
            card("Jace, the Mind Sculptor", "Worldwake", "NM", 98.0, 1, BrowseCategory.Magic),
            card("Chalice of the Void", "Mirrodin", "SP", 85.0, 1, BrowseCategory.Magic),
            card("Force of Will", "Alliances", "LP", 120.0, 1, BrowseCategory.Magic),
            card("Sword of Fire and Ice", "Darksteel", "NM", 48.0, 3, BrowseCategory.Magic),
            card("Wasteland", "Tempest", "NM", 37.0, 2, BrowseCategory.Magic),
            card("Birds of Paradise", "Revised", "NM", 22.0, 5, BrowseCategory.Magic),
            card("Thoughtseize", "Lorwyn", "NM", 25.0, 4, BrowseCategory.Magic),
            card("Doubling Season", "Ravnica", "NM", 78.0, 1, BrowseCategory.Magic),
            card("Sol Ring", "Alpha", "NM", 650.0, 1, BrowseCategory.Magic),
            card("Mana Crypt", "Eternal Masters", "NM", 185.0, 2, BrowseCategory.Magic),
            card("Mox Diamond", "Stronghold", "PLD", 680.0, 1, BrowseCategory.Magic),
            //
            card("Michael Jordan RC", "Fleer '86", "NM", 2_200.0, 1, BrowseCategory.Sports),
            card("Patrick Mahomes Prizm", "2017 Prizm", "NM", 425.0, 2, BrowseCategory.Sports),
            card("Wayne Gretzky RC", "1979 O-Pee-Chee", "VG", 3500.0, 1, BrowseCategory.Sports),
            card("Tom Brady RC", "2000 Bowman Chrome", "NM", 9000.0, 1, BrowseCategory.Sports),
            card("LeBron James RC", "2003 Topps Chrome", "NM", 4700.0, 1, BrowseCategory.Sports),
            card("Ken Griffey Jr. RC", "1989 Upper Deck", "NM", 800.0, 1, BrowseCategory.Sports),
            card("Mickey Mantle", "1952 Topps", "GOOD", 127500.0, 1, BrowseCategory.Sports),
            card("Babe Ruth", "1933 Goudey", "FAIR", 98000.0, 1, BrowseCategory.Sports),
            card("Cristiano Ronaldo RC", "2002 Panini", "NM", 1200.0, 2, BrowseCategory.Sports),
            card("Lionel Messi RC", "2004 Panini", "NM", 2100.0, 1, BrowseCategory.Sports),
            card("Steph Curry RC", "2009 Topps", "NM", 3400.0, 1, BrowseCategory.Sports),
            card("Giannis Antetokounmpo RC", "2013 Prizm", "NM", 2500.0, 1, BrowseCategory.Sports),
            card("Zion Williamson RC", "2019 Prizm", "NM", 280.0, 3, BrowseCategory.Sports),
            card("Shohei Ohtani RC", "2018 Topps", "NM", 155.0, 4, BrowseCategory.Sports),
            card("Aaron Judge RC", "2017 Topps", "NM", 42.0, 5, BrowseCategory.Sports),
            card("Derek Jeter RC", "1993 SP", "NM", 670.0, 1, BrowseCategory.Sports),
            card("Honus Wagner", "1909 T206", "GOOD", 2500000.0, 1, BrowseCategory.Sports),
            card("Wilt Chamberlain RC", "1961 Fleer", "EX", 70000.0, 1, BrowseCategory.Sports),
            card("Joe Montana RC", "1981 Topps", "NM", 450.0, 2, BrowseCategory.Sports),
            card("Mike Trout RC", "2011 Topps Update", "NM", 1850.0, 1, BrowseCategory.Sports),
            //
            card("Charizard Base Set", "Base Set", "EX", 535.0, 1, BrowseCategory.Pokemon),
            card("Misdreavus", "Neo Revelation", "NM", 3.75, 10, BrowseCategory.Pokemon),
            card("Crobat VMAX", "Darkness Ablaze", "NM", 14.5, 4, BrowseCategory.Pokemon),
            card("Gardevoir EX", "Steam Siege", "LP", 9.25, 5, BrowseCategory.Pokemon),
            card("Dark Gengar", "Neo Destiny", "NM", 44.0, 2, BrowseCategory.Pokemon),
            card("Mew", "XY: Evolutions", "NM", 7.5, 3, BrowseCategory.Pokemon),
            card("Ho-oh GX", "Burning Shadows", "NM", 13.0, 7, BrowseCategory.Pokemon),
            card("Tapu Lele GX", "Guardians Rising", "LP", 22.0, 2, BrowseCategory.Pokemon),
            card("Salamence", "Dragon Majesty", "NM", 11.0, 6, BrowseCategory.Pokemon),
            card("Regigigas", "Platinum", "NM", 4.5, 2, BrowseCategory.Pokemon),
            card("Professor Sycamore", "Steam Siege", "NM", 5.2, 8, BrowseCategory.Pokemon),
            card("Machamp", "Fossil", "MP", 8.9, 4, BrowseCategory.Pokemon),
            card("Urza's Tower", "Antiquities", "LP", 39.0, 3, BrowseCategory.Magic),
            card("Crucible of Worlds", "Fifth Dawn", "NM", 60.0, 2, BrowseCategory.Magic),
            card("Karn Liberated", "New Phyrexia", "NM", 98.4, 2, BrowseCategory.Magic),
            card("Fetchland Polluted Delta", "Onslaught", "SP", 80.0, 2, BrowseCategory.Magic),
            card("Aether Vial", "Darksteel", "NM", 45.2, 3, BrowseCategory.Magic),
            card("Vedalken Shackles", "Fifth Dawn", "NM", 33.0, 4, BrowseCategory.Magic),
            card("Olivia Voldaren", "Innistrad", "NM", 16.5, 5, BrowseCategory.Magic),
            card("Batterskull", "New Phyrexia", "NM", 28.0, 3, BrowseCategory.Magic)
 
        )

    private fun card(
        name: String,
        setName: String,
        condition: String,
        priceDollars: Double,
        quantity: Int,
        category: BrowseCategory,
    ): BrowseCardEntity = detailCard(
        name = name,
        setName = setName,
        condition = condition,
        priceDollars = priceDollars,
        quantity = quantity,
        category = category,
    )

    private fun detailCard(
        name: String,
        setName: String,
        condition: String,
        priceDollars: Double,
        quantity: Int,
        category: BrowseCategory,
        gameName: String = category.toGameName(),
        rarityLabel: String = "Common",
        editionLabel: String = "Normal Edition",
        imageUrl: String? = null,
        abilitiesText: String = "",
        flavorText: String = "",
        marketPriceDollars: Double? = null,
        buylistPriceDollars: Double? = null,
        lpPriceDollars: Double? = null,
        mpPriceDollars: Double? = null,
        hpPriceDollars: Double? = null,
        dPriceDollars: Double? = null,
    ): BrowseCardEntity {
        val nmCents = dollarsToCents(priceDollars)
        return BrowseCardEntity(
            name = name,
            setName = setName,
            condition = condition,
            priceCents = nmCents,
            quantity = quantity,
            category = category.name,
            gameName = gameName,
            rarityLabel = rarityLabel,
            editionLabel = editionLabel,
            imageUrl = imageUrl,
            abilitiesText = abilitiesText,
            flavorText = flavorText,
            marketPriceCents = marketPriceDollars?.let(::dollarsToCents) ?: 0,
            buylistPriceCents = buylistPriceDollars?.let(::dollarsToCents) ?: 0,
            lpPriceCents = lpPriceDollars?.let(::dollarsToCents) ?: 0,
            mpPriceCents = mpPriceDollars?.let(::dollarsToCents) ?: 0,
            hpPriceCents = hpPriceDollars?.let(::dollarsToCents) ?: 0,
            dPriceCents = dPriceDollars?.let(::dollarsToCents) ?: 0,
        )
    }

    private fun dollarsToCents(dollars: Double): Long = (dollars * 100).toLong()

    private fun BrowseCategory.toGameName(): String =
        when (this) {
            BrowseCategory.Pokemon -> "Pokémon"
            BrowseCategory.Magic -> "Magic: The Gathering"
            BrowseCategory.Sports -> "Sports Cards"
            BrowseCategory.All -> "Collectibles"
        }
}
