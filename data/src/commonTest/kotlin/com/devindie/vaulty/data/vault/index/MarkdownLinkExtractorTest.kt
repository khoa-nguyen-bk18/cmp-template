package com.devindie.vaulty.data.vault.index

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
/** Unit tests for wiki links, front matter, and tags in [MarkdownLinkExtractor]. */
class MarkdownLinkExtractorTest {
    @Test
    fun extractsWikiLinksAndFrontmatterTags() {
        val content =
            """
            ---
            title: Note
            tags: alpha, beta
            ---
            # Heading
            See [[Other Note]] and [[Target|Alias]].
            #inline-tag
            """.trimIndent()

        val result = MarkdownLinkExtractor.parse(content, isMarkdown = true)

        assertTrue(result.links.any { it.targetPath == "Other Note" && it.linkKind == "wiki" })
        assertTrue(result.links.any { it.targetPath == "Target" && it.label == "Alias" })
        assertTrue(result.properties.any { it.namespace == "tag" && it.value == "alpha" })
        assertTrue(result.properties.any { it.namespace == "tag" && it.value == "inline-tag" })
        assertTrue(result.headings.contains("Heading"))
    }

    @Test
    fun extractsMarkdownLinksIgnoringExternalUrls() {
        val content = "Link [Docs](guide.md) and [Web](https://example.com)."
        val result = MarkdownLinkExtractor.parse(content, isMarkdown = true)

        assertEquals(1, result.links.size)
        assertEquals("guide.md", result.links.first().targetPath)
        assertEquals("markdown", result.links.first().linkKind)
    }
}
