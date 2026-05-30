package com.devindie.vaulty.data.vault.index

import com.devindie.vaulty.domain.model.index.VaultFileRef
import com.devindie.vaulty.domain.model.index.VaultSearchQuery
import com.devindie.vaulty.domain.model.index.VaultSearchResult
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VaultSearchQueryFiltersTest {
    @Test
    fun extensionFilter_rejectsNonMatching() {
        val result = sampleResult(extension = "md")
        val query = VaultSearchQuery(extensions = setOf("pdf"))
        assertFalse(VaultSearchQueryFilters.matchesPostSqlFilters(result, query))
    }

    @Test
    fun modifiedRange_acceptsInRange() {
        val result = sampleResult(modifiedAt = 100L)
        val query =
            VaultSearchQuery(
                modifiedFromEpochMs = 50L,
                modifiedToEpochMs = 150L,
            )
        assertTrue(VaultSearchQueryFilters.matchesPostSqlFilters(result, query))
    }

    private fun sampleResult(extension: String = "md", modifiedAt: Long = 0L): VaultSearchResult = VaultSearchResult(
        file =
        VaultFileRef(
            id = 1L,
            relativePath = "note.$extension",
            name = "note.$extension",
            extension = extension,
            isMarkdown = extension == "md",
        ),
        snippet = "",
        rank = 0.0,
        modifiedAtEpochMs = modifiedAt,
        mimeCategory = "markdown",
        sizeBytes = 0L,
    )
}
