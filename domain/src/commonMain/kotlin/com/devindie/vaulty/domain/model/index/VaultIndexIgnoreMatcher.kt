package com.devindie.vaulty.domain.model.index

/**
 * Gitignore-style matcher for vault-relative paths during indexing.
 *
 * Supports comments, blank lines, wildcards (`*`, `**`), root anchoring (`/`), and
 * directory-only patterns (trailing `/`). Negation (`!`) is not supported yet.
 *
 * Paths use forward slashes relative to the vault root (e.g. `notes/daily.md`).
 */
class VaultIndexIgnoreMatcher private constructor(private val patterns: List<IgnorePattern>) {
    fun isIgnored(relativePath: String, isDirectory: Boolean): Boolean {
        val normalized = normalizeRelativePath(relativePath)
        return when {
            normalized.isEmpty() -> false
            matchesDirectly(normalized, isDirectory) -> true
            !isDirectory && hasIgnoredParentPrefix(normalized) -> true
            else -> false
        }
    }

    private fun hasIgnoredParentPrefix(normalizedPath: String): Boolean {
        val segments = normalizedPath.split('/')
        return (1 until segments.size).any { prefixLength ->
            val parent = segments.take(prefixLength).joinToString("/")
            shouldPruneDirectory(parent)
        }
    }

    fun shouldPruneDirectory(relativePath: String): Boolean {
        val normalized = normalizeRelativePath(relativePath)
        if (normalized.isEmpty()) return false
        return patterns.any { it.matchesDirectoryTree(normalized) }
    }

    private fun matchesDirectly(normalizedPath: String, isDirectory: Boolean): Boolean =
        patterns.any { it.matches(normalizedPath, isDirectory) }

    companion object {
        val EMPTY: VaultIndexIgnoreMatcher = VaultIndexIgnoreMatcher(emptyList())

        fun parse(rulesText: String): VaultIndexIgnoreMatcher {
            val patterns =
                rulesText
                    .lineSequence()
                    .map { it.trim() }
                    .filter { it.isNotEmpty() && !it.startsWith("#") }
                    .mapNotNull { IgnorePattern.parse(it) }
                    .toList()
            return VaultIndexIgnoreMatcher(patterns)
        }

        internal fun normalizeRelativePath(path: String): String = path.replace('\\', '/').trim('/')
    }
}

private data class IgnorePattern(
    val anchored: Boolean,
    val directoryOnly: Boolean,
    val hasSlash: Boolean,
    val segments: List<String>,
) {
    fun matches(normalizedPath: String, isDirectory: Boolean): Boolean {
        if (directoryOnly && !isDirectory) return false
        return matchesPath(normalizedPath)
    }

    fun matchesDirectoryTree(normalizedPath: String): Boolean {
        if (!matchesPath(normalizedPath)) return false
        return directoryOnly || hasSlash || anchored || segments.any { it.contains('*') }
    }

    private fun matchesPath(normalizedPath: String): Boolean {
        val pathSegments = normalizedPath.split('/')
        val fileName = pathSegments.lastOrNull() ?: normalizedPath
        return when {
            !hasSlash -> matchesFileNameOnly(pathSegments, fileName)
            anchored -> matchPathWithDoubleStar(pathSegments, segments)
            else -> matchesAnyPathSuffix(pathSegments)
        }
    }

    private fun matchesFileNameOnly(pathSegments: List<String>, fileName: String): Boolean {
        val patternSegment = segments.singleOrNull() ?: return false
        return when {
            anchored -> matchPathWithDoubleStar(pathSegments, segments)
            matchGlob(fileName, patternSegment) -> true
            else -> pathSegments.any { segment -> matchGlob(segment, patternSegment) }
        }
    }

    private fun matchesAnyPathSuffix(pathSegments: List<String>): Boolean =
        matchPathWithDoubleStar(pathSegments, segments) ||
            pathSegments.indices.any { start ->
                matchPathWithDoubleStar(pathSegments.drop(start), segments)
            }

    companion object {
        fun parse(line: String): IgnorePattern? {
            var pattern = line.trim().replace('\\', '/')
            val directoryOnly = pattern.endsWith("/")
            if (directoryOnly) {
                pattern = pattern.dropLast(1)
            }
            if (pattern.isEmpty()) {
                return null
            }

            val anchored = pattern.startsWith("/")
            if (anchored) {
                pattern = pattern.drop(1)
            }
            val segments = pattern.split('/').filter { it.isNotEmpty() }
            return when {
                pattern.isEmpty() || segments.isEmpty() -> null
                else ->
                    IgnorePattern(
                        anchored = anchored,
                        directoryOnly = directoryOnly,
                        hasSlash = pattern.contains('/'),
                        segments = segments,
                    )
            }
        }
    }
}
