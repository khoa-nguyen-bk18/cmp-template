package com.devindie.cmptemplate.data.browse

import com.devindie.cmptemplate.domain.model.browse.BrowseCategory
import com.devindie.cmptemplate.domain.model.carddetail.CardCondition
import com.devindie.cmptemplate.domain.model.carddetail.CardDetail
import com.devindie.cmptemplate.domain.model.carddetail.ConditionPricing

internal fun BrowseCardEntity.toCardDetail(): CardDetail {
    val listingCondition = CardCondition.fromCode(condition) ?: CardCondition.NearMint
    val nmCents = priceCents
    val marketCents =
        marketPriceCents.takeIf { it > 0 }
            ?: scaledPriceCents(nmCents, 96)
    val buylistCents =
        buylistPriceCents.takeIf { it > 0 }
            ?: scaledPriceCents(nmCents, 58)
    val lpCents = lpPriceCents.takeIf { it > 0 } ?: scaledPriceCents(nmCents, 86)
    val mpCents = mpPriceCents.takeIf { it > 0 } ?: scaledPriceCents(nmCents, 69)
    val hpCents = hpPriceCents.takeIf { it > 0 } ?: scaledPriceCents(nmCents, 38)
    val dCents = dPriceCents.takeIf { it > 0 } ?: scaledPriceCents(nmCents, 19)

    val resolvedGameName =
        gameName.ifBlank {
            BrowseCategory.entries
                .firstOrNull { it.name == category }
                ?.toGameName()
                ?: category
        }

    return CardDetail(
        id = id,
        name = name,
        gameName = resolvedGameName,
        setName = setName,
        rarityLabel = rarityLabel.ifBlank { "Common" },
        editionLabel = editionLabel.ifBlank { "Normal Edition" },
        imageUrl = imageUrl,
        listingCondition = listingCondition,
        conditionBadgeLabel = CardCondition.fromCode(condition)?.label ?: condition,
        abilitiesText = abilitiesText,
        flavorText = flavorText,
        conditionPricing =
        CardCondition.selectorOrder.map { tier ->
            val cents =
                when (tier) {
                    CardCondition.NearMint -> nmCents
                    CardCondition.LightlyPlayed -> lpCents
                    CardCondition.ModeratelyPlayed -> mpCents
                    CardCondition.HeavilyPlayed -> hpCents
                    CardCondition.Damaged -> dCents
                }
            ConditionPricing(
                condition = tier,
                priceDisplay = formatPriceCents(cents),
            )
        },
        marketPriceDisplay = formatPriceCents(marketCents),
        buylistPriceDisplay = formatPriceCents(buylistCents),
    )
}

private fun BrowseCategory.toGameName(): String = when (this) {
    BrowseCategory.Pokemon -> "Pokémon"
    BrowseCategory.Magic -> "Magic: The Gathering"
    BrowseCategory.Sports -> "Sports Cards"
    BrowseCategory.All -> "Collectibles"
}
