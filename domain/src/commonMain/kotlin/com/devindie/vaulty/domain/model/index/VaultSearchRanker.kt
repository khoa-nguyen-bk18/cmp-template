package com.devindie.vaulty.domain.model.index

/**
 * Re-ranks FTS search hits using query-term overlap, field boosts, and light typo tolerance.
 *
 * Used after SQLite returns candidate rows so ranking stays platform-agnostic and testable.
 */
object VaultSearchRanker {
    private const val NAME_PREFIX_BOOST = 100.0
    private const val NAME_CONTAINS_BOOST = 80.0
    private const val PATH_CONTAINS_BOOST = 40.0
    private const val SNIPPET_CONTAINS_BOOST = 25.0
    private const val NAME_FUZZY_BOOST = 30.0
    private const val PATH_FUZZY_BOOST = 15.0
    private const val RECENCY_SCALE = 1e-15
    private const val MIN_FUZZY_TERM_LENGTH = 3
    private const val SHORT_TERM_MAX_LENGTH = 4
    private const val MEDIUM_TERM_MAX_LENGTH = 7
    private const val SHORT_TERM_EDIT_DISTANCE = 1
    private const val MEDIUM_TERM_EDIT_DISTANCE = 2
    private const val LONG_TERM_EDIT_DISTANCE = 3

    fun rank(query: String, results: List<VaultSearchResult>): List<VaultSearchResult> {
        val terms = queryTerms(query)
        if (terms.isEmpty()) return results

        return results
            .map { result -> result to score(result, terms) }
            .sortedByDescending { (_, score) -> score }
            .map { (result, score) -> result.copy(rank = score) }
    }

    fun queryTerms(query: String): List<String> = query
        .trim()
        .split(Regex("""\s+"""))
        .map { it.trim() }
        .filter { it.length >= 2 }

    private fun score(result: VaultSearchResult, terms: List<String>): Double {
        val name = result.file.name.lowercase()
        val path = result.file.relativePath.lowercase()
        val snippet = stripHighlightMarkers(result.snippet).lowercase()

        var total = 0.0
        for (term in terms) {
            val needle = term.lowercase()
            total +=
                when {
                    name.startsWith(needle) -> NAME_PREFIX_BOOST
                    name.contains(needle) -> NAME_CONTAINS_BOOST
                    path.contains(needle) -> PATH_CONTAINS_BOOST
                    snippet.contains(needle) -> SNIPPET_CONTAINS_BOOST
                    else -> 0.0
                }
            if (needle.length >= MIN_FUZZY_TERM_LENGTH) {
                total += fuzzyBonus(name, needle) * NAME_FUZZY_BOOST
                total += fuzzyBonus(path, needle) * PATH_FUZZY_BOOST
            }
        }
        total += result.modifiedAtEpochMs * RECENCY_SCALE
        return total
    }

    private fun stripHighlightMarkers(text: String): String = text
        .replace(VaultSearchHighlightMarkers.OPEN, "")
        .replace(VaultSearchHighlightMarkers.CLOSE, "")

    private fun fuzzyBonus(text: String, term: String): Double {
        if (text.contains(term)) return 0.0
        val maxDistance = allowedEditDistance(term.length)
        return text
            .split(Regex("""\W+"""))
            .filter { it.isNotEmpty() }
            .maxOfOrNull { word ->
                if (levenshteinDistance(word, term) <= maxDistance) 1.0 else 0.0
            } ?: 0.0
    }

    internal fun allowedEditDistance(termLength: Int): Int = when {
        termLength <= SHORT_TERM_MAX_LENGTH -> SHORT_TERM_EDIT_DISTANCE
        termLength <= MEDIUM_TERM_MAX_LENGTH -> MEDIUM_TERM_EDIT_DISTANCE
        else -> LONG_TERM_EDIT_DISTANCE
    }

    internal fun levenshteinDistance(left: String, right: String): Int {
        val trivialDistance =
            when {
                left == right -> 0
                left.isEmpty() -> right.length
                right.isEmpty() -> left.length
                else -> null
            }
        if (trivialDistance != null) return trivialDistance

        val previous = IntArray(right.length + 1) { it }
        val current = IntArray(right.length + 1)

        for (i in left.indices) {
            current[0] = i + 1
            for (j in right.indices) {
                val substitutionCost = if (left[i] == right[j]) 0 else 1
                current[j + 1] =
                    minOf(
                        current[j] + 1,
                        previous[j + 1] + 1,
                        previous[j] + substitutionCost,
                    )
            }
            previous.indices.forEach { index -> previous[index] = current[index] }
        }
        return previous[right.length]
    }
}
