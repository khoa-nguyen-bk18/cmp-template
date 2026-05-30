package com.devindie.vaulty.domain.model.index

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VaultIndexIgnoreMatcherTest {
    @Test
    fun emptyRules_ignoreNothing() {
        val matcher = VaultIndexIgnoreMatcher.EMPTY
        assertFalse(matcher.isIgnored("notes/a.md", isDirectory = false))
        assertFalse(matcher.shouldPruneDirectory("notes"))
    }

    @Test
    fun commentsAndBlankLines_ignored() {
        val matcher =
            VaultIndexIgnoreMatcher.parse(
                """
                # comment

                *.png
                """.trimIndent(),
            )
        assertTrue(matcher.isIgnored("img/logo.png", isDirectory = false))
    }

    @Test
    fun wildcardExtension_matchesAnyDepth() {
        val matcher = VaultIndexIgnoreMatcher.parse("*.png")
        assertTrue(matcher.isIgnored("a/b/c.png", isDirectory = false))
        assertFalse(matcher.isIgnored("a/b/c.jpg", isDirectory = false))
        assertFalse(matcher.shouldPruneDirectory("a/b"))
    }

    @Test
    fun directoryPattern_prunesAndIgnoresDescendants() {
        val matcher = VaultIndexIgnoreMatcher.parse(".trash/")
        assertTrue(matcher.shouldPruneDirectory(".trash"))
        assertTrue(matcher.isIgnored(".trash/note.md", isDirectory = false))
        assertTrue(matcher.shouldPruneDirectory("notes/.trash"))
    }

    @Test
    fun anchoredRoot_onlyMatchesFromRoot() {
        val matcher = VaultIndexIgnoreMatcher.parse("/build")
        assertTrue(matcher.isIgnored("build", isDirectory = true))
        assertTrue(matcher.shouldPruneDirectory("build"))
        assertFalse(matcher.isIgnored("src/build/out", isDirectory = false))
    }

    @Test
    fun pathWithSlash_matchesRelativePath() {
        val matcher = VaultIndexIgnoreMatcher.parse("notes/private.md")
        assertTrue(matcher.isIgnored("notes/private.md", isDirectory = false))
        assertFalse(matcher.isIgnored("other/private.md", isDirectory = false))
    }

    @Test
    fun doubleStar_matchesNestedDirectories() {
        val matcher = VaultIndexIgnoreMatcher.parse("**/node_modules")
        assertTrue(matcher.shouldPruneDirectory("node_modules"))
        assertTrue(matcher.shouldPruneDirectory("packages/app/node_modules"))
        assertFalse(matcher.isIgnored("node_modules.txt", isDirectory = false))
    }

    @Test
    fun directoryOnly_doesNotIgnoreFilesWithSamePrefix() {
        val matcher = VaultIndexIgnoreMatcher.parse("temp/")
        assertFalse(matcher.isIgnored("temp.txt", isDirectory = false))
        assertTrue(matcher.isIgnored("temp", isDirectory = true))
    }

    @Test
    fun normalizeBackslashes() {
        val matcher = VaultIndexIgnoreMatcher.parse("notes\\secret\\")
        assertTrue(matcher.shouldPruneDirectory("notes/secret"))
    }
}
