package com.devindie.cmptemplate.data.source.local.browse

import com.devindie.cmptemplate.domain.model.browse.BrowseCategory
import com.devindie.cmptemplate.domain.model.carddetail.CardCondition
import com.devindie.cmptemplate.domain.model.carddetail.CardDetail
import com.devindie.cmptemplate.domain.model.carddetail.ConditionPricing

private const val MARKET_PRICE_SCALE_NUMERATOR = 96L
private const val BUYLIST_PRICE_SCALE_NUMERATOR = 58L
private const val LP_PRICE_SCALE_NUMERATOR = 86L
private const val MP_PRICE_SCALE_NUMERATOR = 69L
private const val HP_PRICE_SCALE_NUMERATOR = 38L
private const val DAMAGED_PRICE_SCALE_NUMERATOR = 19L

internal fun BrowseCardEntity.toCardDetail(): CardDetail {
    val listingCondition = CardCondition.fromCode(condition) ?: CardCondition.NearMint
    val nmCents = priceCents
    val tierCents = resolveTierPriceCents(nmCents)

    return CardDetail(
        id = id,
        name = name,
        gameName = resolveGameName(),
        setName = setName,
        rarityLabel = rarityLabel.ifBlank { "Common" },
        editionLabel = editionLabel.ifBlank { "Normal Edition" },
        imageUrl = imageUrl,
        listingCondition = listingCondition,
        conditionBadgeLabel = CardCondition.fromCode(condition)?.label ?: condition,
        abilitiesText = abilitiesText,
        flavorText = flavorText,
        conditionPricing = tierCents.toConditionPricing(),
        marketPriceDisplay =
        formatPriceCents(storedOrScaled(marketPriceCents, nmCents, MARKET_PRICE_SCALE_NUMERATOR)),
        buylistPriceDisplay =
        formatPriceCents(storedOrScaled(buylistPriceCents, nmCents, BUYLIST_PRICE_SCALE_NUMERATOR)),
    )
}

private fun BrowseCardEntity.resolveTierPriceCents(nmCents: Long): TierPriceCents = TierPriceCents(
    nearMint = nmCents,
    lightlyPlayed = storedOrScaled(lpPriceCents, nmCents, LP_PRICE_SCALE_NUMERATOR),
    moderatelyPlayed = storedOrScaled(mpPriceCents, nmCents, MP_PRICE_SCALE_NUMERATOR),
    heavilyPlayed = storedOrScaled(hpPriceCents, nmCents, HP_PRICE_SCALE_NUMERATOR),
    damaged = storedOrScaled(dPriceCents, nmCents, DAMAGED_PRICE_SCALE_NUMERATOR),
)

private fun BrowseCardEntity.resolveGameName(): String = gameName.ifBlank {
    BrowseCategory.entries
        .firstOrNull { it.name == category }
        ?.toGameName()
        ?: category
}

private fun storedOrScaled(storedCents: Long, baseCents: Long, scaleNumerator: Long): Long =
    storedCents.takeIf { it > 0 } ?: scaledPriceCents(baseCents, scaleNumerator)

private data class TierPriceCents(
    val nearMint: Long,
    val lightlyPlayed: Long,
    val moderatelyPlayed: Long,
    val heavilyPlayed: Long,
    val damaged: Long,
)

private fun TierPriceCents.toConditionPricing(): List<ConditionPricing> = CardCondition.selectorOrder.map { tier ->
    ConditionPricing(
        condition = tier,
        priceDisplay = formatPriceCents(centsFor(tier)),
    )
}

private fun TierPriceCents.centsFor(tier: CardCondition): Long = when (tier) {
    CardCondition.NearMint -> nearMint
    CardCondition.LightlyPlayed -> lightlyPlayed
    CardCondition.ModeratelyPlayed -> moderatelyPlayed
    CardCondition.HeavilyPlayed -> heavilyPlayed
    CardCondition.Damaged -> damaged
}

private fun BrowseCategory.toGameName(): String = when (this) {
    BrowseCategory.Pokemon -> "Pokémon"
    BrowseCategory.Magic -> "Magic: The Gathering"
    BrowseCategory.Sports -> "Sports Cards"
    BrowseCategory.All -> "Collectibles"
}
