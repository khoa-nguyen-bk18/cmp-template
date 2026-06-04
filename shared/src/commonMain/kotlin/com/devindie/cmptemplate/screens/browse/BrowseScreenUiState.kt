package com.devindie.cmptemplate.screens.browse

import com.devindie.cmptemplate.domain.model.browse.BrowseCategory

data class BrowseScreenUiState(
    val searchQuery: String = "",
    val selectedCategory: BrowseCategory = BrowseCategory.All,
)
