package com.devindie.cmptemplate.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class BrowseCardDto(
    val id:Long,
    val name: String,
    val setName: String,
    val condition: String,
    val priceCents: Long,
    val quantity: Int,
    val category: String,
    val gameName: String = "",
    val rarityLabel: String = "",
    val editionLabel: String = "",
    val imageUrl: String? = null,
    val abilitiesText: String = "",
    val flavorText: String = "",
    val marketPriceCents: Long = 0,
    val buylistPriceCents: Long = 0,
    val lpPriceCents: Long = 0,
    val mpPriceCents: Long = 0,
    val hpPriceCents: Long = 0,
    val dPriceCents: Long = 0,
)

@Serializable
data class BrowseCatalogPageDto(
    val cards: List<BrowseCardDto>,
    val pagination: BrowsePaginationDto,
)

@Serializable
data class BrowsePaginationDto(
    val page: Int,
    val pageSize: Int,
    val hasMore: Boolean,
)
