package com.devindie.vaulty.domain.model.index

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VaultSearchRankerTest {
    @Test
    fun rank_prefersFilenameMatchOverPathOnly() {
        val results =
            listOf(
                result(id = 1L, name = "notes/other.md", path = "notes/other.md"),
                result(id = 2L, name = "budget.md", path = "finance/budget.md"),
            )

        val ranked = VaultSearchRanker.rank("budget", results)

        assertEquals(2L, ranked.first().file.id)
        assertTrue(ranked.first().rank > ranked.last().rank)
    }

    @Test
    fun rank_boostsNearTypoInFilename() {
        val results =
            listOf(
                result(id = 1L, name = "budjet.md", path = "finance/budjet.md"),
                result(id = 2L, name = "totally-unrelated.md", path = "x/totally-unrelated.md"),
            )

        val ranked = VaultSearchRanker.rank("budget", results)

        assertEquals(1L, ranked.first().file.id)
    }

    @Test
    fun levenshteinDistance_matchesSingleEdit() {
        assertEquals(1, VaultSearchRanker.levenshteinDistance("budget", "budjet"))
    }
}

private fun result(id: Long, name: String, path: String): VaultSearchResult = VaultSearchResult(
    file =
    VaultFileRef(
        id = id,
        relativePath = path,
        name = name,
        extension = "md",
        isMarkdown = true,
    ),
    snippet = "",
    rank = 0.0,
    modifiedAtEpochMs = 0L,
    mimeCategory = "markdown",
    sizeBytes = 0L,
)
