package com.devindie.vaulty.domain.model.index

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VaultSearchDslParserTest {
    private val clock =
        object : VaultSearchDslClock {
            // 2026-05-23 12:00 UTC
            override fun nowEpochMs(): Long = 1_748_995_200_000L

            override fun zoneOffsetMinutes(): Int = 0
        }

    private val parser = VaultSearchDslParser(clock)

    @Test
    fun modifiedYesterday_setsRangeAndSort() {
        val result = parser.parse("modified:yesterday sort:modified_desc")
        assertTrue(result.isSupported)
        assertEquals(VaultSearchSort.ModifiedDesc, result.query.sort)
        assertEquals(1_748_908_800_000L, result.query.modifiedFromEpochMs)
        assertEquals(1_748_995_200_000L, result.query.modifiedToEpochMs)
    }

    @Test
    fun tagWithFreeText_splitsFields() {
        val result = parser.parse("tag:meeting John")
        assertTrue(result.isSupported)
        assertEquals(setOf("meeting"), result.query.tags)
        assertEquals("John", result.query.text)
    }

    @Test
    fun backlinksZero_setsOrphanFilter() {
        val result = parser.parse("backlinks:0")
        assertTrue(result.isSupported)
        assertTrue(result.query.orphansOnly)
        assertEquals(0, result.query.maxBacklinks)
        assertTrue(result.query.onlyMarkdown)
    }

    @Test
    fun unknownOperatorToken_treatedAsFreeText() {
        val result = parser.parse("foo:bar")
        assertTrue(result.isSupported)
        assertEquals("foo:bar", result.query.text)
    }

    @Test
    fun unsupportedTaskOperator_reported() {
        val result = parser.parse("task:open")
        assertFalse(result.isSupported)
        assertEquals(setOf("task"), result.unsupportedOperators)
    }
}
