package com.devindie.vaulty.domain.model.index

/**
 * Single FTS hit with snippet and relevance rank.
 *
 * [snippet] may contain [VaultSearchHighlightMarkers] around terms matched in the body.
 */
data class VaultSearchResult(
    val file: VaultFileRef,
    val snippet: String,
    /** Truncated body text used when FTS [snippet] is empty (external-content FTS4). */
    val contentBodyExcerpt: String = "",
    val rank: Double,
    val modifiedAtEpochMs: Long,
    val mimeCategory: String,
    val sizeBytes: Long,
)
