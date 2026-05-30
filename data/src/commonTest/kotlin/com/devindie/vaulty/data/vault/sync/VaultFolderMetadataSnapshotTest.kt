package com.devindie.vaulty.data.vault.sync

import com.devindie.vaulty.data.vault.index.VaultFileEntry
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VaultFolderMetadataSnapshotTest {
    @Test
    fun hasChangesFrom_falseOnFirstPoll() {
        val snapshot =
            VaultFolderMetadataSnapshot.fromEntries(
                listOf(
                    file("a.md", size = 10, modified = 100),
                ),
            )

        assertFalse(snapshot.hasChangesFrom(previous = null))
    }

    @Test
    fun hasChangesFrom_trueWhenFileAdded() {
        val previous =
            VaultFolderMetadataSnapshot.fromEntries(
                listOf(file("a.md", size = 10, modified = 100)),
            )
        val current =
            VaultFolderMetadataSnapshot.fromEntries(
                listOf(
                    file("a.md", size = 10, modified = 100),
                    file("b.md", size = 5, modified = 200),
                ),
            )

        assertTrue(current.hasChangesFrom(previous))
    }

    @Test
    fun hasChangesFrom_trueWhenMtimeChanges() {
        val previous =
            VaultFolderMetadataSnapshot.fromEntries(
                listOf(file("a.md", size = 10, modified = 100)),
            )
        val current =
            VaultFolderMetadataSnapshot.fromEntries(
                listOf(file("a.md", size = 10, modified = 101)),
            )

        assertTrue(current.hasChangesFrom(previous))
    }

    @Test
    fun hasChangesFrom_falseWhenUnchanged() {
        val entries = listOf(file("a.md", size = 10, modified = 100))
        val previous = VaultFolderMetadataSnapshot.fromEntries(entries)
        val current = VaultFolderMetadataSnapshot.fromEntries(entries)

        assertFalse(current.hasChangesFrom(previous))
    }

    private fun file(path: String, size: Long, modified: Long) = VaultFileEntry(
        relativePath = path,
        name = path.substringAfterLast('/'),
        extension = "md",
        sizeBytes = size,
        modifiedAtEpochMs = modified,
        isDirectory = false,
    )
}
