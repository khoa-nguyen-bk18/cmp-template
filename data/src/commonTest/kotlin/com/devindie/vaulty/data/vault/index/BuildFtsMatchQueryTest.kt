package com.devindie.vaulty.data.vault.index

import kotlin.test.Test
import kotlin.test.assertEquals

class BuildFtsMatchQueryTest {
    @Test
    fun singleToken_usesUnquotedPrefixWildcard() {
        assertEquals("hello*", buildFtsMatchQuery("hello"))
    }

    @Test
    fun multipleTokens_andJoinedWithPrefixWildcards() {
        assertEquals("hello* world*", buildFtsMatchQuery("hello world"))
    }

    @Test
    fun multipleTokens_orMode_joinsWithOr() {
        assertEquals("hello* OR world*", buildFtsMatchQuery("hello world", FtsMatchMode.Or))
    }

    @Test
    fun escapesDoubleQuotesInToken() {
        assertEquals("say* \"\"\"hi\"\"\"*", buildFtsMatchQuery("say \"hi\""))
    }

    @Test
    fun stripsFtsSpecialCharactersFromTokens() {
        assertEquals("hello*", buildFtsMatchQuery("hel*lo?"))
    }

    @Test
    fun blankInput_returnsEmptyMatchSentinel() {
        assertEquals("\"\"", buildFtsMatchQuery("   "))
    }

    @Test
    fun mergeSearchRows_prefersStrictMatchesAndDedupes() {
        val strict =
            listOf(
                searchRow(id = 1L, name = "a.md"),
                searchRow(id = 2L, name = "b.md"),
            )
        val relaxed =
            listOf(
                searchRow(id = 2L, name = "b.md"),
                searchRow(id = 3L, name = "c.md"),
            )

        val merged = mergeSearchRows(strict, relaxed)

        assertEquals(listOf(1L, 2L, 3L), merged.map { it.id })
    }
}

private fun searchRow(id: Long, name: String) = com.devindie.vaulty.data.vault.index.dao.SearchResultRow(
    id = id,
    relativePath = name,
    name = name,
    extension = "md",
    modifiedAtEpochMs = 0L,
    mimeCategory = "markdown",
    sizeBytes = 0L,
    snippet = "",
    contentBodyExcerpt = "",
    rankScore = 0.0,
)
