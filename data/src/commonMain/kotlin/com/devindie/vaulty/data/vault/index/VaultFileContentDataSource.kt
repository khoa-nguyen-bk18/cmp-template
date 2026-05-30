package com.devindie.vaulty.data.vault.index

import com.devindie.vaulty.domain.model.index.VaultIndexIgnoreMatcher

/** File metadata collected from the vault tree before per-file content reads. */
data class VaultFileEntry(
    val relativePath: String,
    val name: String,
    val extension: String,
    val sizeBytes: Long,
    val modifiedAtEpochMs: Long,
    val isDirectory: Boolean,
)

/**
 * Platform port to enumerate vault files and read text for indexing.
 *
 * **Upstream:** [VaultFolderIndexer], [VaultIndexRepositoryImpl].
 */
interface VaultFileContentDataSource {
    /**
     * @param onProgress Called during tree traversal with files discovered so far and the
     *   current relative path (directory or file being visited).
     */
    suspend fun collectFiles(
        storageKey: String,
        ignoreMatcher: VaultIndexIgnoreMatcher = VaultIndexIgnoreMatcher.EMPTY,
        onProgress: (filesDiscovered: Int, currentPath: String) -> Unit = { _, _ -> },
    ): Result<List<VaultFileEntry>>

    suspend fun readTextContent(storageKey: String, relativePath: String, maxBytes: Int): Result<String>
}
