package com.devindie.vaulty.domain.usecase.index

import com.devindie.vaulty.domain.model.index.PrepareSearchResultDisplayParams
import com.devindie.vaulty.domain.model.index.SearchSnippetLineExtractor
import com.devindie.vaulty.domain.model.index.VaultFileRef
import com.devindie.vaulty.domain.model.index.VaultSearchHighlightMarkers
import com.devindie.vaulty.domain.model.index.VaultSearchResult
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PrepareSearchResultDisplayUseCaseTest {
    private val useCase = PrepareSearchResultDisplayUseCase()

    @Test
    fun invoke_precomputesMatchLineAndSource() = runTest {
        val snippet =
            "Plan the ${VaultSearchHighlightMarkers.OPEN}budget${VaultSearchHighlightMarkers.CLOSE} for Q2"
        val result =
            VaultSearchResult(
                file =
                VaultFileRef(
                    id = 1L,
                    relativePath = "note.md",
                    name = "note.md",
                    extension = "md",
                    isMarkdown = true,
                ),
                snippet = snippet,
                rank = 1.0,
                modifiedAtEpochMs = 0L,
                mimeCategory = "markdown",
                sizeBytes = 0L,
            )

        val display =
            useCase(
                PrepareSearchResultDisplayParams(
                    highlightTerms = listOf("budget"),
                    results = listOf(result),
                ),
            ).single()

        assertEquals(snippet, display.matchLine)
        assertEquals(SearchSnippetLineExtractor.MatchSource.Content, display.matchSource)
        assertEquals(result, display.result)
    }

    @Test
    fun invoke_blankSnippetAndBody_leavesMatchLineNull() = runTest {
        val result =
            VaultSearchResult(
                file =
                VaultFileRef(
                    id = 2L,
                    relativePath = "x.txt",
                    name = "x.txt",
                    extension = "txt",
                    isMarkdown = false,
                ),
                snippet = "  ",
                rank = 0.0,
                modifiedAtEpochMs = 0L,
                mimeCategory = "text",
                sizeBytes = 0L,
            )

        val display =
            useCase(
                query = "ab",
                results = listOf(result),
            ).single()

        assertNull(display.matchLine)
    }
}
