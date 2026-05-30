package com.devindie.cmptemplate.domain.model.carddetail

/** Price for a single condition tier on the card detail screen. */
data class ConditionPricing(
    val condition: CardCondition,
    val priceDisplay: String,
)
