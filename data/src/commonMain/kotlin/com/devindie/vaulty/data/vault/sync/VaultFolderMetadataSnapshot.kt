package com.devindie.vaulty.data.vault.sync

import com.devindie.vaulty.data.vault.index.VaultFileEntry

/** Lightweight file metadata map used to detect vault tree changes between polls. */
data class VaultFolderMetadataSnapshot(val files: Map<String, FileMeta>) {
    data class FileMeta(val sizeBytes: Long, val modifiedAtEpochMs: Long)

    companion object {
        fun fromEntries(entries: List<VaultFileEntry>): VaultFolderMetadataSnapshot = VaultFolderMetadataSnapshot(
            entries
                .filter { !it.isDirectory }
                .associate { entry ->
                    entry.relativePath to
                        FileMeta(
                            sizeBytes = entry.sizeBytes,
                            modifiedAtEpochMs = entry.modifiedAtEpochMs,
                        )
                },
        )
    }
}

/** Returns true when [previous] differs from this snapshot (or [previous] is null on first poll). */
fun VaultFolderMetadataSnapshot.hasChangesFrom(previous: VaultFolderMetadataSnapshot?): Boolean =
    previous != null && previous != this
