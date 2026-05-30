package com.devindie.vaulty.domain.model.index

/**
 * Picks the best single line to show from an FTS [VaultSearchResult.snippet] excerpt,
 * with a fallback scan of indexed [VaultSearchResult.contentBodyExcerpt].
 *
 * FTS4 `content=` tables often return an empty column-specific `snippet()`; loading
 * body text from `vault_file.contentBody` restores matched lines in the UI.
 */
object SearchSnippetLineExtractor {
    fun extractMatchLine(snippet: String, highlightTerms: List<String>, contentBody: String = ""): String? {
        val terms = normalizedTerms(highlightTerms)
        val trimmedSnippet = snippet.trim()
        val fromMarkedSnippet =
            if (VaultSearchHighlightMarkers.OPEN in trimmedSnippet) {
                extractFromSnippet(trimmedSnippet, terms)
            } else {
                null
            }
        return fromMarkedSnippet
            ?: extractLineFromContent(contentBody, terms)
            ?: trimmedSnippet.takeIf { it.isNotBlank() }?.let { extractFromSnippet(it, terms) }
    }

    fun matchSourceLabelKey(snippet: String, contentBody: String, highlightTerms: List<String>): MatchSource {
        val terms = normalizedTerms(highlightTerms)
        val trimmedSnippet = snippet.trim()
        return when {
            trimmedSnippet.lines().any { it.trimStart().startsWith("#") } -> MatchSource.Heading
            trimmedSnippet.isNotBlank() && lineContainsAnyTerm(trimmedSnippet, terms) -> MatchSource.Content
            contentBody.isNotBlank() && extractLineFromContent(contentBody, terms) != null -> MatchSource.Content
            else -> MatchSource.None
        }
    }

    enum class MatchSource {
        None,
        Content,
        Heading,
    }

    private fun extractFromSnippet(snippet: String, terms: List<String>): String? {
        val lines = snippet.lines().map { it.trim() }.filter { it.isNotEmpty() }
        return when {
            lines.isEmpty() -> null
            lines.size == 1 -> lines.single()
            else ->
                lines.firstOrNull { VaultSearchHighlightMarkers.OPEN in it }
                    ?: terms.takeIf { it.isNotEmpty() }
                        ?.let { activeTerms ->
                            lines.firstOrNull { line -> lineContainsAnyTerm(line, activeTerms) }
                        }
                    ?: lines.maxByOrNull { line -> markerCount(line) }
                    ?: lines.first()
        }
    }

    private fun extractLineFromContent(contentBody: String, terms: List<String>): String? {
        if (contentBody.isBlank() || terms.isEmpty()) return null
        return contentBody
            .lineSequence()
            .map { it.trim() }
            .firstOrNull { it.isNotEmpty() && lineContainsAnyTerm(it, terms) }
    }

    private fun normalizedTerms(highlightTerms: List<String>): List<String> = highlightTerms
        .map { it.trim() }
        .filter { it.length >= 2 }
        .map { it.lowercase() }
        .distinct()

    private fun lineContainsAnyTerm(line: String, terms: List<String>): Boolean {
        if (terms.isEmpty()) return false
        val lower = line.lowercase()
        return terms.any { lower.contains(it) }
    }

    private fun markerCount(line: String): Int {
        var count = 0
        var index = 0
        while (true) {
            val found = line.indexOf(VaultSearchHighlightMarkers.OPEN, index)
            if (found < 0) break
            count++
            index = found + VaultSearchHighlightMarkers.OPEN.length
        }
        return count
    }
}
