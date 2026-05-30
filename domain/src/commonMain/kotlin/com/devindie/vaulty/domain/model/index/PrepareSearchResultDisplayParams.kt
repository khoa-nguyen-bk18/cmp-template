package com.devindie.vaulty.domain.model.index

/** Input for [com.devindie.vaulty.domain.usecase.index.PrepareSearchResultDisplayUseCase]. */
data class PrepareSearchResultDisplayParams(val highlightTerms: List<String>, val results: List<VaultSearchResult>)
