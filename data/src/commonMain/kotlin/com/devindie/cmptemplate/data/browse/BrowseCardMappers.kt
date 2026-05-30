package com.devindie.cmptemplate.data.browse

import com.devindie.cmptemplate.domain.model.browse.BrowseCategory
import com.devindie.cmptemplate.domain.model.browse.CollectibleCard
internal fun BrowseCardEntity.toDomain(): CollectibleCard =
    CollectibleCard(
        id = id,
        name = name,
        setName = setName,
        condition = condition,
        priceDisplay = formatPrice(priceCents),
        quantity = quantity,
        category = BrowseCategory.entries.firstOrNull { it.name == category } ?: BrowseCategory.All,
    )

private fun formatPrice(priceCents: Long): String {
    val dollars = priceCents / 100
    val cents = priceCents % 100
    return if (cents == 0L) {
        "$$dollars"
    } else {
        "$$dollars.${cents.toString().padStart(2, '0')}"
    }
}
