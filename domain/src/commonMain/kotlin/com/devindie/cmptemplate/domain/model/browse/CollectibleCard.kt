package com.devindie.cmptemplate.domain.model.browse

/**
 * Inventory card shown on the Stitch "Browse" / Product Listing tab.
 */
data class CollectibleCard(
    val id: Long,
    val name: String,
    val setName: String,
    val condition: String,
    val priceDisplay: String,
    val quantity: Int,
    val category: BrowseCategory,
)
