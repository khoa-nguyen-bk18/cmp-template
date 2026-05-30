package com.devindie.vaulty.domain.usecase.index

import com.devindie.vaulty.domain.model.index.PrepareSearchResultDisplayParams
import com.devindie.vaulty.domain.model.index.SearchQueryHighlight
import com.devindie.vaulty.domain.model.index.SearchResultDisplay
import com.devindie.vaulty.domain.model.index.SearchSnippetLineExtractor
import com.devindie.vaulty.domain.model.index.VaultSearchResult
import com.devindie.vaulty.domain.usecase.UseCase

/**
 * Precomputes per-hit match lines and source labels for the search results list.
 *
 * **Flow:** [com.devindie.vaulty.screens.search.SearchViewModel] → this (off main via caller)
 * after [SearchVaultUseCase]. Pure CPU; no dispatchers here.
 *
 * @see SearchSnippetLineExtractor
 * @see SearchResultDisplay
 */
class PrepareSearchResultDisplayUseCase : UseCase<PrepareSearchResultDisplayParams, List<SearchResultDisplay>> {
    override suspend fun invoke(parameters: PrepareSearchResultDisplayParams): List<SearchResultDisplay> {
        val terms = parameters.highlightTerms
        return parameters.results.map { result -> result.toDisplay(terms) }
    }

    /** Convenience when terms are derived from the query that produced [results]. */
    suspend operator fun invoke(query: String, results: List<VaultSearchResult>): List<SearchResultDisplay> = invoke(
        PrepareSearchResultDisplayParams(
            highlightTerms = SearchQueryHighlight.termsFromQuery(query),
            results = results,
        ),
    )
}

private fun VaultSearchResult.toDisplay(highlightTerms: List<String>): SearchResultDisplay {
    val snippet = this.snippet
    val body = contentBodyExcerpt
    return SearchResultDisplay(
        result = this,
        matchLine =
        SearchSnippetLineExtractor.extractMatchLine(
            snippet = snippet,
            highlightTerms = highlightTerms,
            contentBody = body,
        ),
        matchSource =
        SearchSnippetLineExtractor.matchSourceLabelKey(
            snippet = snippet,
            contentBody = body,
            highlightTerms = highlightTerms,
        ),
    )
}
