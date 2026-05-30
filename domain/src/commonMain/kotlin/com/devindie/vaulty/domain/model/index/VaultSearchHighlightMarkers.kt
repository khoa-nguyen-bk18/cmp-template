package com.devindie.vaulty.domain.model.index

/**
 * Delimiters wrapped around matched terms in FTS [VaultSearchResult.snippet] text.
 *
 * Must match the open/close tags passed to SQLite `snippet()` in the data layer.
 */
object VaultSearchHighlightMarkers {
    const val OPEN = "\uE000"
    const val CLOSE = "\uE001"
}
