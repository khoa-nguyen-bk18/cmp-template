package com.devindie.vaulty.domain.model.index

/**
 * Derives UI highlight terms from a raw search query string.
 *
 * Skips DSL tokens (containing `:`) and very short terms; used when preparing
 * [SearchResultDisplay] and for live query highlighting in [com.devindie.vaulty.screens.search.SearchScreen].
 */
object SearchQueryHighlight {
    private val whitespace = Regex("""\s+""")

    fun termsFromQuery(query: String): List<String> = query
        .trim()
        .split(whitespace)
        .filter { it.isNotBlank() && !it.contains(':') && it.length >= 2 }
}
