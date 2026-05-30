package com.devindie.vaulty.data.vault.sync

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class VaultFolderSnapshotStoreEncodingTest {
    @Test
    fun encodeAndDecode_roundTrip() {
        val snapshot =
            VaultFolderMetadataSnapshot.fromEntries(
                listOf(
                    com.devindie.vaulty.data.vault.index.VaultFileEntry(
                        relativePath = "a.md",
                        name = "a.md",
                        extension = "md",
                        sizeBytes = 42,
                        modifiedAtEpochMs = 100,
                        isDirectory = false,
                    ),
                ),
            )

        val decoded = decodeVaultFolderMetadataSnapshot(snapshot.encode())

        assertNotNull(decoded)
        assertEquals(snapshot, decoded)
    }
}
