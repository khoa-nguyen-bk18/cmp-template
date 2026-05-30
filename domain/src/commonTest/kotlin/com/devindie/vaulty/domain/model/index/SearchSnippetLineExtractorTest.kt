package com.devindie.vaulty.domain.model.index

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SearchSnippetLineExtractorTest {
    @Test
    fun extractMatchLine_returnsSingleLineSnippet() {
        val snippet = "Intro ${VaultSearchHighlightMarkers.OPEN}budget${VaultSearchHighlightMarkers.CLOSE} notes"
        assertEquals(
            snippet,
            SearchSnippetLineExtractor.extractMatchLine(snippet, listOf("budget")),
        )
    }

    @Test
    fun extractMatchLine_picksLineContainingMarkers() {
        val snippet =
            """
            First line without match
            Plan the ${VaultSearchHighlightMarkers.OPEN}budget${VaultSearchHighlightMarkers.CLOSE} for Q2
            Third line
            """.trimIndent()

        assertEquals(
            "Plan the ${VaultSearchHighlightMarkers.OPEN}budget${VaultSearchHighlightMarkers.CLOSE} for Q2",
            SearchSnippetLineExtractor.extractMatchLine(snippet, listOf("budget")),
        )
    }

    @Test
    fun extractMatchLine_fallsBackToContentBodyWhenSnippetEmpty() {
        val content =
            """
            # Title
            Track the budget for this quarter.
            """.trimIndent()

        assertEquals(
            "Track the budget for this quarter.",
            SearchSnippetLineExtractor.extractMatchLine(
                snippet = "",
                highlightTerms = listOf("budget"),
                contentBody = content,
            ),
        )
    }

    @Test
    fun extractMatchLine_prefersMarkedSnippetOverBody() {
        val snippet =
            "${VaultSearchHighlightMarkers.OPEN}budget${VaultSearchHighlightMarkers.CLOSE} in snippet"
        val content = "budget line in full body"

        assertEquals(
            snippet,
            SearchSnippetLineExtractor.extractMatchLine(
                snippet = snippet,
                highlightTerms = listOf("budget"),
                contentBody = content,
            ),
        )
    }

    @Test
    fun extractMatchLine_blankSnippetAndBody_returnsNull() {
        assertNull(
            SearchSnippetLineExtractor.extractMatchLine(
                snippet = "   ",
                highlightTerms = listOf("a"),
                contentBody = "",
            ),
        )
    }
}
