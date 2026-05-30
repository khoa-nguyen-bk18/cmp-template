package com.devindie.vaulty.domain.model.index

/** Outcome of parsing a vault search DSL string. */
data class VaultSearchDslParseResult(
    val query: VaultSearchQuery,
    val issues: List<VaultSearchDslIssue> = emptyList(),
    val unsupportedOperators: Set<String> = emptySet(),
) {
    val isSupported: Boolean get() = unsupportedOperators.isEmpty()

    val hasStructuredFilters: Boolean get() = query.hasStructuredFilters()
}

data class VaultSearchDslIssue(val token: String, val message: String)
