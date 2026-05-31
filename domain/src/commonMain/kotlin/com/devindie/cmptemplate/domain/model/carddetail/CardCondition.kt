package com.devindie.cmptemplate.domain.model.carddetail

/**
 * Inventory condition tiers shown on the Stitch card detail segmented control.
 */
enum class CardCondition(val code: String, val label: String) {
    NearMint(code = "NM", label = "Near Mint"),
    LightlyPlayed(code = "LP", label = "Lightly Played"),
    ModeratelyPlayed(code = "MP", label = "Moderately Played"),
    HeavilyPlayed(code = "HP", label = "Heavily Played"),
    Damaged(code = "D", label = "Damaged"),
    ;

    companion object {
        val selectorOrder: List<CardCondition> = entries

        fun fromCode(code: String): CardCondition? = entries.firstOrNull { it.code.equals(code, ignoreCase = true) }
    }
}
