package com.devindie.vaulty.domain.model.index

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
/** Unit tests for incremental index skip logic in [VaultFileChangeDetector]. */
class VaultFileChangeDetectorTest {
    @Test
    fun needsReindex_whenNoStoredSnapshot() {
        assertTrue(
            VaultFileChangeDetector.needsReindex(
                stored = null,
                currentSizeBytes = 100L,
                currentModifiedAtEpochMs = 1L,
            ),
        )
    }

    @Test
    fun skipsReindex_whenMetadataUnchanged() {
        assertFalse(
            VaultFileChangeDetector.needsReindex(
                stored =
                VaultFileIndexSnapshot(
                    fileId = 1L,
                    relativePath = "note.md",
                    sizeBytes = 100L,
                    modifiedAtEpochMs = 42L,
                ),
                currentSizeBytes = 100L,
                currentModifiedAtEpochMs = 42L,
            ),
        )
    }

    @Test
    fun needsReindex_whenSizeChanged() {
        assertTrue(
            VaultFileChangeDetector.needsReindex(
                stored =
                VaultFileIndexSnapshot(
                    fileId = 1L,
                    relativePath = "note.md",
                    sizeBytes = 100L,
                    modifiedAtEpochMs = 42L,
                ),
                currentSizeBytes = 101L,
                currentModifiedAtEpochMs = 42L,
            ),
        )
    }

    @Test
    fun needsReindex_whenModifiedAtChanged() {
        assertTrue(
            VaultFileChangeDetector.needsReindex(
                stored =
                VaultFileIndexSnapshot(
                    fileId = 1L,
                    relativePath = "note.md",
                    sizeBytes = 100L,
                    modifiedAtEpochMs = 42L,
                ),
                currentSizeBytes = 100L,
                currentModifiedAtEpochMs = 43L,
            ),
        )
    }
}
