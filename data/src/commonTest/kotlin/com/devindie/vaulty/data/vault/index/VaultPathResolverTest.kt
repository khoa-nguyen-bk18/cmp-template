package com.devindie.vaulty.data.vault.index

import kotlin.test.Test
import kotlin.test.assertEquals
/** Unit tests for relative path normalization and link resolution in [VaultPathResolver]. */
class VaultPathResolverTest {
    @Test
    fun resolvesRelativeMarkdownLinks() {
        val resolved =
            VaultPathResolver.resolveLinkTarget(
                sourceRelativePath = "notes/a.md",
                targetPath = "../b",
            )
        assertEquals("b", resolved)
    }

    @Test
    fun addsMarkdownExtensionCandidates() {
        val candidates = VaultPathResolver.withMarkdownExtension("note")
        assertEquals(listOf("note", "note.md", "note.markdown"), candidates)
    }
}
