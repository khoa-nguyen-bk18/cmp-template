package com.devindie.cmptemplate.domain.model.browse

/**
 * Inventory card shown on the Stitch "Browse" / Product Listing tab.
 *
 * @see com.devindie.cmptemplate.domain.repository.BrowseCardRepository
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
