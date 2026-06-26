package com.devindie.cmptemplate.domain.model.carddetail

import com.devindie.cmptemplate.domain.fake.FakeCardDetailRepository

fun sampleCardDetail(id: Long = 1L, listingCondition: CardCondition = CardCondition.NearMint): CardDetail = CardDetail(
    id = id,
    name = "Charizard ex",
    gameName = "Pokémon",
    setName = "Obsidian Flames",
    rarityLabel = "Rare",
    editionLabel = "Normal Edition",
    imageUrl = null,
    listingCondition = listingCondition,
    conditionBadgeLabel = listingCondition.label,
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

fun FakeCardDetailRepository.stubSuccess(cardId: Long = 1L, detail: CardDetail = sampleCardDetail(id = cardId)) {
    getCardDetailResult = Result.success(detail)
}

fun FakeCardDetailRepository.stubFailure(message: String = "Card not found") {
    getCardDetailResult = Result.failure(IllegalStateException(message))
}
