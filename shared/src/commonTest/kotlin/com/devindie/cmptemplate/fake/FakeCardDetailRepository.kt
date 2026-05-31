package com.devindie.cmptemplate.fake

import com.devindie.cmptemplate.domain.model.carddetail.CardCondition
import com.devindie.cmptemplate.domain.model.carddetail.CardDetail
import com.devindie.cmptemplate.domain.model.carddetail.ConditionPricing
import com.devindie.cmptemplate.domain.repository.CardDetailRepository

class FakeCardDetailRepository : CardDetailRepository {
    var getCardDetailResult: Result<CardDetail>? = null

    override suspend fun getCardDetail(cardId: Long): Result<CardDetail> =
        getCardDetailResult ?: Result.failure(IllegalStateException("No stubbed result"))
}

fun sampleCardDetail(id: Long = 1L): CardDetail = CardDetail(
    id = id,
    name = "Charizard ex",
    gameName = "Pokémon",
    setName = "Obsidian Flames",
    rarityLabel = "Rare",
    editionLabel = "Normal Edition",
    imageUrl = null,
    listingCondition = CardCondition.NearMint,
    conditionBadgeLabel = CardCondition.NearMint.label,
    abilitiesText = "Ability text",
    flavorText = "Flavor text",
    conditionPricing =
    CardCondition.selectorOrder.map { condition ->
        ConditionPricing(
            condition = condition,
            priceDisplay =
            when (condition) {
                CardCondition.NearMint -> "$189.99"
                CardCondition.LightlyPlayed -> "$163.99"
                CardCondition.ModeratelyPlayed -> "$131.09"
                CardCondition.HeavilyPlayed -> "$72.19"
                CardCondition.Damaged -> "$36.09"
            },
        )
    },
    marketPriceDisplay = "$182.39",
    buylistPriceDisplay = "$110.19",
)
