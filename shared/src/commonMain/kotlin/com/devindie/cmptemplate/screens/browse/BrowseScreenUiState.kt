package com.devindie.cmptemplate.screens.browse

import com.devindie.cmptemplate.domain.model.browse.BrowseCategory
import com.devindie.cmptemplate.domain.model.browse.CollectibleCard

data class BrowseScreenUiState(
    val searchQuery: String = "",
    val selectedCategory: BrowseCategory = BrowseCategory.All,
    val cards: List<CollectibleCard> = emptyList(),
    val isLoading: Boolean = true,
)
