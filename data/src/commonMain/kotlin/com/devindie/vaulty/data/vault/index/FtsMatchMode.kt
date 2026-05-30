package com.devindie.vaulty.data.vault.index

import com.devindie.vaulty.data.vault.index.dao.SearchResultRow

internal enum class FtsMatchMode {
    And,
    Or,
}

internal const val FTS_OR_FALLBACK_THRESHOLD = 5
internal const val FTS_SEARCH_CANDIDATE_LIMIT = 50
private const val MIN_PREFIX_TOKEN_LENGTH = 2

internal fun buildFtsMatchQuery(raw: String, mode: FtsMatchMode = FtsMatchMode.And): String {
    val ftsTokens =
        raw
            .trim()
            .split(Regex("""\s+"""))
            .map { escapeFtsToken(it) }
            .filter { it.isNotBlank() }
            .map { formatFtsToken(it) }
    if (ftsTokens.isEmpty()) return "\"\""
    val joiner =
        when (mode) {
            FtsMatchMode.And -> " "
            FtsMatchMode.Or -> " OR "
        }
    return ftsTokens.joinToString(joiner)
}

private fun formatFtsToken(token: String): String {
    val needsQuotes = token.any { it.isWhitespace() } || token.contains('"')
    return if (token.length >= MIN_PREFIX_TOKEN_LENGTH) {
        if (needsQuotes) "\"$token\"*" else "$token*"
    } else {
        if (needsQuotes) "\"$token\"" else token
    }
}

private fun escapeFtsToken(token: String): String = token
    .replace("\"", "\"\"")
    .replace(Regex("""[*?:^]"""), "")
    .trim()

internal fun mergeSearchRows(strict: List<SearchResultRow>, relaxed: List<SearchResultRow>): List<SearchResultRow> {
    val seen = strict.map { it.id }.toMutableSet()
    val merged = strict.toMutableList()
    for (row in relaxed) {
        if (seen.add(row.id)) {
            merged.add(row)
        }
    }
    return merged
}
