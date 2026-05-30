package com.devindie.vaulty.domain.model.index

/** File count grouped by extension (e.g. `md`, `pdf`). */
data class ExtensionCount(val extension: String, val count: Int)

/** Tag frequency from indexed front matter or inline tags. */
data class TagCount(val tag: String, val count: Int)

/**
 * Aggregated vault metrics for the dashboard screen.
 *
 * Loaded by [com.devindie.vaulty.domain.usecase.index.ObserveVaultDashboardStatsUseCase].
 */
data class VaultDashboardStats(
    val totalFiles: Int,
    val markdownFiles: Int,
    val totalSizeBytes: Long,
    val lastIndexedAtEpochMs: Long?,
    val extensionBreakdown: List<ExtensionCount>,
    val topTags: List<TagCount>,
    val brokenLinkCount: Int,
    val orphanNoteCount: Int,
)
