package com.devindie.vaulty.data.vault.index

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class VaultLinkResolutionTest {
    @Test
    fun linkEntitiesFromPending_resolvesTargetFileId() {
        val pending =
            listOf(
                PendingVaultLink(
                    sourceFileId = 1L,
                    sourceRelativePath = "note.md",
                    link =
                    ExtractedLink(
                        targetPath = "other.md",
                        linkKind = "wiki",
                        anchor = null,
                        label = null,
                    ),
                ),
            )
        val pathToId = mapOf("other.md" to 2L)

        val entities = linkEntitiesFromPending(pending, pathToId)

        assertEquals(1, entities.size)
        assertEquals(1L, entities.first().sourceFileId)
        assertEquals(2L, entities.first().resolvedTargetFileId)
        assertEquals("other.md", entities.first().targetPath)
    }

    @Test
    fun linkEntitiesFromPending_marksBrokenLinkWhenTargetMissing() {
        val pending =
            listOf(
                PendingVaultLink(
                    sourceFileId = 1L,
                    sourceRelativePath = "note.md",
                    link =
                    ExtractedLink(
                        targetPath = "missing.md",
                        linkKind = "wiki",
                        anchor = null,
                        label = null,
                    ),
                ),
            )

        val entities = linkEntitiesFromPending(pending, emptyMap())

        assertNull(entities.first().resolvedTargetFileId)
    }
}
