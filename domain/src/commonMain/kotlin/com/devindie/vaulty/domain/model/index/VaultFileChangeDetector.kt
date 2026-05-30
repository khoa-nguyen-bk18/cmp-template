package com.devindie.vaulty.domain.model.index

/**
 * Decides whether a vault file needs a full re-index based on filesystem metadata.
 *
 * Content is not read when size and mtime are unchanged. Used by
 * [com.devindie.vaulty.data.vault.index.VaultFolderIndexer] during incremental runs.
 */
object VaultFileChangeDetector {
    fun needsReindex(
        storedSizeBytes: Long,
        storedModifiedAtEpochMs: Long,
        currentSizeBytes: Long,
        currentModifiedAtEpochMs: Long,
    ): Boolean = storedSizeBytes != currentSizeBytes ||
        storedModifiedAtEpochMs != currentModifiedAtEpochMs

    fun needsReindex(
        stored: VaultFileIndexSnapshot?,
        currentSizeBytes: Long,
        currentModifiedAtEpochMs: Long,
    ): Boolean {
        if (stored == null) return true
        return needsReindex(
            storedSizeBytes = stored.sizeBytes,
            storedModifiedAtEpochMs = stored.modifiedAtEpochMs,
            currentSizeBytes = currentSizeBytes,
            currentModifiedAtEpochMs = currentModifiedAtEpochMs,
        )
    }
}

/** Stored metadata used to detect incremental index skips. */
data class VaultFileIndexSnapshot(
    val fileId: Long,
    val relativePath: String,
    val sizeBytes: Long,
    val modifiedAtEpochMs: Long,
)
