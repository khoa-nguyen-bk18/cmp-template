package com.devindie.cmptemplate.data.source.remote.browse

import com.devindie.cmptemplate.data.source.local.browse.BrowseCardEntity
import com.devindie.cmptemplate.data.network.dto.BrowseCardDto

internal fun BrowseCardEntity.toDto(): BrowseCardDto = BrowseCardDto(
    name = name,
    setName = setName,
    condition = condition,
    priceCents = priceCents,
    quantity = quantity,
    category = category,
    gameName = gameName,
    rarityLabel = rarityLabel,
    editionLabel = editionLabel,
    imageUrl = imageUrl,
    abilitiesText = abilitiesText,
    flavorText = flavorText,
    marketPriceCents = marketPriceCents,
    buylistPriceCents = buylistPriceCents,
    lpPriceCents = lpPriceCents,
    mpPriceCents = mpPriceCents,
    hpPriceCents = hpPriceCents,
    dPriceCents = dPriceCents,
)

internal fun BrowseCardDto.toEntity(): BrowseCardEntity = BrowseCardEntity(
    name = name,
    setName = setName,
    condition = condition,
    priceCents = priceCents,
    quantity = quantity,
    category = category,
    gameName = gameName,
    rarityLabel = rarityLabel,
    editionLabel = editionLabel,
    imageUrl = imageUrl,
    abilitiesText = abilitiesText,
    flavorText = flavorText,
    marketPriceCents = marketPriceCents,
    buylistPriceCents = buylistPriceCents,
    lpPriceCents = lpPriceCents,
    mpPriceCents = mpPriceCents,
    hpPriceCents = hpPriceCents,
    dPriceCents = dPriceCents,
)
