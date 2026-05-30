package com.devindie.vaulty.data.vault.sync

/**
 * Persists the last observed vault metadata snapshot between background sync runs.
 *
 * **Implemented by:** [AndroidVaultFolderSnapshotStore], [IosVaultFolderSnapshotStore].
 */
interface VaultFolderSnapshotStore {
    suspend fun load(storageKey: String): VaultFolderMetadataSnapshot?

    suspend fun save(storageKey: String, snapshot: VaultFolderMetadataSnapshot)

    suspend fun clear(storageKey: String)
}

internal fun VaultFolderMetadataSnapshot.encode(): String = files.entries
    .sortedBy { it.key }
    .joinToString(separator = "\n") { (path, meta) ->
        "$path|${meta.sizeBytes}|${meta.modifiedAtEpochMs}"
    }

private const val SNAPSHOT_FIELD_COUNT = 3

internal fun decodeVaultFolderMetadataSnapshot(encoded: String): VaultFolderMetadataSnapshot? {
    if (encoded.isBlank()) {
        return null
    }
    val lines = encoded.lineSequence().filter { it.isNotBlank() }.toList()
    val parsed = lines.map { line -> parseSnapshotLine(line) }
    return if (parsed.any { it == null }) {
        null
    } else {
        val files = linkedMapOf<String, VaultFolderMetadataSnapshot.FileMeta>()
        parsed.filterNotNull().forEach { (path, meta) -> files[path] = meta }
        VaultFolderMetadataSnapshot(files)
    }
}

private fun parseSnapshotLine(line: String): Pair<String, VaultFolderMetadataSnapshot.FileMeta>? {
    val parts = line.split('|')
    val size = parts.getOrNull(1)?.toLongOrNull()
    val modified = parts.getOrNull(2)?.toLongOrNull()
    return if (parts.size == SNAPSHOT_FIELD_COUNT && size != null && modified != null) {
        parts[0] to VaultFolderMetadataSnapshot.FileMeta(sizeBytes = size, modifiedAtEpochMs = modified)
    } else {
        null
    }
}
