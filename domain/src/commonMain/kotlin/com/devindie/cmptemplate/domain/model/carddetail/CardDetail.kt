package com.devindie.cmptemplate.domain.model.carddetail

/**
 * Full card detail payload for the Stitch "Card Details" screen.
 *
 * @see com.devindie.cmptemplate.domain.repository.CardDetailRepository
 */
data class CardDetail(
    val id: Long,
    val name: String,
    val gameName: String,
    val setName: String,
    val rarityLabel: String,
    val editionLabel: String,
    val imageUrl: String?,
    val listingCondition: CardCondition,
    val conditionBadgeLabel: String,
    val abilitiesText: String,
    val flavorText: String,
    val conditionPricing: List<ConditionPricing>,
    val marketPriceDisplay: String,
    val buylistPriceDisplay: String,
)
