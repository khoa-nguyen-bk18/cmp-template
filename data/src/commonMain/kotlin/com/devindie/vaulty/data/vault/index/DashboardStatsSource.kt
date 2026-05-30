package com.devindie.vaulty.data.vault.index

import com.devindie.vaulty.data.vault.index.dao.ExtensionCountRow
import com.devindie.vaulty.data.vault.index.dao.TagCountRow
import com.devindie.vaulty.domain.model.index.ExtensionCount
import com.devindie.vaulty.domain.model.index.TagCount
import com.devindie.vaulty.domain.model.index.VaultDashboardStats

internal data class DashboardStatsSource(
    val lastIndexedAt: Long?,
    val extensionRows: List<ExtensionCountRow>,
    val tagRows: List<TagCountRow>,
    val totalFiles: Int,
    val markdownFiles: Int,
    val totalSize: Long,
    val brokenLinks: Int,
    val orphanNotes: Int,
)

internal fun buildDashboardStats(source: DashboardStatsSource): VaultDashboardStats = VaultDashboardStats(
    totalFiles = source.totalFiles,
    markdownFiles = source.markdownFiles,
    totalSizeBytes = source.totalSize,
    lastIndexedAtEpochMs = source.lastIndexedAt,
    extensionBreakdown = source.extensionRows.map { ExtensionCount(it.extension, it.count) },
    topTags = source.tagRows.map { TagCount(it.tag, it.count) },
    brokenLinkCount = source.brokenLinks,
    orphanNoteCount = source.orphanNotes,
)

internal fun isMarkdownExtension(extension: String): Boolean {
    val normalized = extension.lowercase()
    return normalized == "md" || normalized == "markdown"
}

internal fun isTextExtension(extension: String): Boolean =
    isMarkdownExtension(extension) || extension in TEXT_EXTENSIONS

private val TEXT_EXTENSIONS =
    setOf(
        "txt",
        "text",
        "json",
        "yaml",
        "yml",
        "xml",
        "html",
        "htm",
        "csv",
        "kt",
        "kts",
        "java",
        "gradle",
    )

internal fun mimeCategoryFor(extension: String): String = when {
    isMarkdownExtension(extension) -> "markdown"
    extension in setOf("png", "jpg", "jpeg", "gif", "webp", "svg") -> "image"
    extension in setOf("mp3", "wav", "m4a", "flac") -> "audio"
    extension in setOf("mp4", "mov", "mkv", "webm") -> "video"
    isTextExtension(extension) -> "text"
    else -> "other"
}
