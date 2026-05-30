package com.devindie.vaulty.data.vault.index

/** Normalizes vault-relative paths and resolves wiki/markdown link targets for the link graph. */
object VaultPathResolver {
    fun normalizeRelativePath(path: String): String = path.replace('\\', '/').trim('/')

    fun resolveLinkTarget(sourceRelativePath: String, targetPath: String): String {
        val normalizedTarget = normalizeRelativePath(targetPath)
        return when {
            normalizedTarget.isEmpty() -> normalizedTarget
            normalizedTarget.startsWith("/") -> normalizeRelativePath(normalizedTarget.drop(1))
            else -> {
                val sourceDir =
                    sourceRelativePath
                        .replace('\\', '/')
                        .substringBeforeLast('/', "")
                val combined =
                    if (sourceDir.isEmpty()) {
                        normalizedTarget
                    } else {
                        "$sourceDir/$normalizedTarget"
                    }
                normalizeSegments(combined)
            }
        }
    }

    private fun normalizeSegments(path: String): String {
        val parts = path.split('/').filter { it.isNotEmpty() && it != "." }
        val stack = mutableListOf<String>()
        for (part in parts) {
            if (part == "..") {
                if (stack.isNotEmpty()) stack.removeAt(stack.lastIndex)
            } else {
                stack.add(part)
            }
        }
        return stack.joinToString("/")
    }

    fun withMarkdownExtension(path: String): List<String> {
        if (path.endsWith(".md", ignoreCase = true) || path.endsWith(".markdown", ignoreCase = true)) {
            return listOf(path)
        }
        return listOf(path, "$path.md", "$path.markdown")
    }
}
