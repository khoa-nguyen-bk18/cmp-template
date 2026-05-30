package com.devindie.vaulty.data.vault.index

data class ExtractedLink(val targetPath: String, val linkKind: String, val anchor: String?, val label: String?)

data class ExtractedProperty(val namespace: String, val key: String, val value: String)

data class MarkdownParseResult(
    val body: String,
    val properties: List<ExtractedProperty>,
    val links: List<ExtractedLink>,
    val headings: String,
)

/** Parses markdown body, front matter, wiki links, tags, and headings for indexing. */
object MarkdownLinkExtractor {
    private val wikiLinkRegex = Regex("""\[\[([^|\]#]+)(?:\|([^\]]+))?\]\]""")
    private val embedLinkRegex = Regex("""!\[\[([^|\]#]+)(?:\|([^\]]+))?\]\]""")
    private val markdownLinkRegex = Regex("""\[[^\]]*]\(([^)]+)\)""")
    private val headingRegex = Regex("""^#{1,6}\s+(.+)$""", RegexOption.MULTILINE)
    private val tagRegex = Regex("""#([\w-]+)""")

    fun parse(content: String, isMarkdown: Boolean): MarkdownParseResult {
        if (!isMarkdown) {
            return MarkdownParseResult(
                body = content,
                properties = emptyList(),
                links = emptyList(),
                headings = "",
            )
        }

        val (frontmatter, body) = splitFrontmatter(content)
        val properties = parseFrontmatter(frontmatter).toMutableList()
        extractInlineTags(body, properties)

        val links = mutableListOf<ExtractedLink>()
        embedLinkRegex.findAll(body).forEach { match ->
            links.add(
                ExtractedLink(
                    targetPath = match.groupValues[1].trim(),
                    linkKind = "embed",
                    anchor = null,
                    label = match.groupValues.getOrNull(2)?.trim(),
                ),
            )
        }
        wikiLinkRegex.findAll(body).forEach { match ->
            links.add(
                ExtractedLink(
                    targetPath = match.groupValues[1].trim(),
                    linkKind = "wiki",
                    anchor = null,
                    label = match.groupValues.getOrNull(2)?.trim(),
                ),
            )
        }
        markdownLinkRegex.findAll(body).forEach { match ->
            val raw = match.groupValues[1].trim()
            if (raw.startsWith("http://") || raw.startsWith("https://") || raw.startsWith("mailto:")) {
                return@forEach
            }
            val (path, anchor) = splitAnchor(raw)
            links.add(
                ExtractedLink(
                    targetPath = path,
                    linkKind = "markdown",
                    anchor = anchor,
                    label = null,
                ),
            )
        }

        val headings =
            headingRegex
                .findAll(body)
                .map { it.groupValues[1].trim() }
                .joinToString("\n")

        return MarkdownParseResult(
            body = body,
            properties = properties,
            links = links,
            headings = headings,
        )
    }

    private fun splitFrontmatter(content: String): Pair<String?, String> {
        if (!content.startsWith("---")) return null to content
        val end = content.indexOf("\n---", startIndex = 3)
        return when {
            end < 0 -> null to content
            else -> {
                val fm = content.substring(3, end).trim()
                val body = content.substring(end + 4).trimStart()
                fm to body
            }
        }
    }

    private fun parseFrontmatter(frontmatter: String?): List<ExtractedProperty> {
        if (frontmatter.isNullOrBlank()) return emptyList()
        val result = mutableListOf<ExtractedProperty>()
        var currentKey: String? = null
        val listValues = mutableListOf<String>()

        fun flushList() {
            val key = currentKey ?: return
            listValues.forEach { value ->
                result.add(ExtractedProperty(namespace = "frontmatter", key = key, value = value))
            }
            listValues.clear()
        }

        frontmatter.lineSequence().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.isEmpty()) return@forEach
            if (trimmed.startsWith("- ") && currentKey != null) {
                listValues.add(trimmed.removePrefix("- ").trim().trim('"'))
                return@forEach
            }
            flushList()
            val colon = trimmed.indexOf(':')
            if (colon <= 0) return@forEach
            currentKey = trimmed.substring(0, colon).trim()
            val value = trimmed.substring(colon + 1).trim().trim('"')
            if (value.isEmpty()) return@forEach
            if (currentKey == "tags") {
                value.split(',').map { it.trim() }.filter { it.isNotEmpty() }.forEach { tag ->
                    result.add(ExtractedProperty(namespace = "tag", key = "tag", value = tag))
                }
            } else {
                result.add(ExtractedProperty(namespace = "frontmatter", key = currentKey, value = value))
            }
        }
        flushList()
        return result
    }

    private fun extractInlineTags(body: String, properties: MutableList<ExtractedProperty>) {
        tagRegex.findAll(body).forEach { match ->
            properties.add(
                ExtractedProperty(namespace = "tag", key = "tag", value = match.groupValues[1]),
            )
        }
    }

    private fun splitAnchor(raw: String): Pair<String, String?> {
        val hash = raw.indexOf('#')
        if (hash < 0) return raw to null
        return raw.substring(0, hash) to raw.substring(hash + 1).ifBlank { null }
    }
}
