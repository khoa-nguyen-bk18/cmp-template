package com.devindie.vaulty.domain.model

/**
 * Result of a shallow folder scan (counts and size, not full-text index).
 *
 * Produced by [com.devindie.vaulty.domain.repository.VaultFolderRepository.scanSelectedFolder].
 */
data class VaultFolderSummary(
    val folderName: String,
    val fileCount: Int,
    val totalSizeBytes: Long,
    val scannedAtEpochMs: Long,
)
