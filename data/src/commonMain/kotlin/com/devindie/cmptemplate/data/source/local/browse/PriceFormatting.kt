package com.devindie.cmptemplate.data.source.local.browse

internal fun formatPriceCents(priceCents: Long): String {
    val dollars = priceCents / 100
    val cents = priceCents % 100
    return if (cents == 0L) {
        "$$dollars"
    } else {
        "$$dollars.${cents.toString().padStart(2, '0')}"
    }
}

internal fun scaledPriceCents(baseCents: Long, numerator: Long, denominator: Long = 100): Long =
    baseCents * numerator / denominator
