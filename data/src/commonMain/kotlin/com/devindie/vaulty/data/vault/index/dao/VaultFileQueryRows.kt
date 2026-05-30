package com.devindie.vaulty.data.vault.index.dao

import com.devindie.vaulty.domain.model.index.VaultFileIndexSnapshot
/** Lightweight row for incremental index change detection (no content columns). */
data class VaultFileSnapshotRow(
    val id: Long,
    val relativePath: String,
    val sizeBytes: Long,
    val modifiedAtEpochMs: Long,
)

/** Minimal columns for link resolution after indexing (avoids loading full [VaultFileEntity]). */
data class VaultFileLinkSourceRow(
    val id: Long,
    val relativePath: String,
    val isMarkdown: Boolean,
    val contentBody: String,
)

internal fun VaultFileSnapshotRow.toIndexSnapshot(): VaultFileIndexSnapshot = VaultFileIndexSnapshot(
    fileId = id,
    relativePath = relativePath,
    sizeBytes = sizeBytes,
    modifiedAtEpochMs = modifiedAtEpochMs,
)

data class ExtensionCountRow(val extension: String, val count: Int)

data class TagCountRow(val tag: String, val count: Int)

data class SearchResultRow(
    val id: Long,
    val relativePath: String,
    val name: String,
    val extension: String,
    val modifiedAtEpochMs: Long,
    val mimeCategory: String,
    val sizeBytes: Long,
    val snippet: String,
    val contentBodyExcerpt: String,
    val rankScore: Double,
)
