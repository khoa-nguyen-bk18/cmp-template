package com.devindie.vaulty.domain.model.index

/**
 * Search hit prepared for list UI: raw [VaultSearchResult] plus precomputed match preview.
 *
 * Built by [com.devindie.vaulty.domain.usecase.index.PrepareSearchResultDisplayUseCase]
 * so composition does not scan [VaultSearchResult.contentBodyExcerpt] on scroll.
 */
data class SearchResultDisplay(
    val result: VaultSearchResult,
    val matchLine: String?,
    val matchSource: SearchSnippetLineExtractor.MatchSource,
)
