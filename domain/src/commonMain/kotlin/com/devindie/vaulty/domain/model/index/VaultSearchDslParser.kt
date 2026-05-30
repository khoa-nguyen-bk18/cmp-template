package com.devindie.vaulty.domain.model.index

/**
 * Parses vault search mini-DSL (`modified:yesterday tag:meeting budget`) into [VaultSearchQuery].
 */
class VaultSearchDslParser(private val clock: VaultSearchDslClock) {
    fun parse(input: String): VaultSearchDslParseResult {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) {
            return VaultSearchDslParseResult(query = VaultSearchQuery())
        }

        val state = DslParseState()
        for (token in tokenize(trimmed)) {
            state.applyToken(token, clock)
        }

        return state.toResult()
    }

    private class DslParseState {
        var extensions = emptySet<String>()
        var tags = emptySet<String>()
        var onlyMarkdown = false
        var modifiedFrom: Long? = null
        var modifiedTo: Long? = null
        var indexedFrom: Long? = null
        var indexedTo: Long? = null
        var minBacklinks: Int? = null
        var maxBacklinks: Int? = null
        var orphansOnly = false
        var mimeCategories = emptySet<String>()
        var requireAttachment = false
        var sort = VaultSearchSort.Default
        val issues = mutableListOf<VaultSearchDslIssue>()
        val unsupported = mutableSetOf<String>()
        val freeText = mutableListOf<String>()

        fun applyToken(token: String, clock: VaultSearchDslClock) {
            val colon = token.indexOf(':')
            if (colon <= 0) {
                freeText.add(token)
                return
            }
            val key = token.substring(0, colon).lowercase()
            val value = token.substring(colon + 1).trim().removeSurrounding("\"")

            when (key) {
                "modified" -> applyDateRange(token, value, clock, DateField.Modified)
                "created" -> applyDateRange(token, value, clock, DateField.Created)
                "tag" -> applyTag(value)
                "type" -> applyType(value)
                "has" -> applyHas(value)
                "backlinks" -> applyBacklinks(token, value)
                "sort" -> applySort(token, value)
                "task", "daily", "favorite", "pinned", "opened", "duplicate", "related", "source" ->
                    unsupported.add(key)
                else -> freeText.add(token)
            }
        }

        fun toResult(): VaultSearchDslParseResult {
            val text = freeText.joinToString(" ").trim()
            val query =
                VaultSearchQuery(
                    text = text,
                    extensions = extensions,
                    tags = tags,
                    onlyMarkdown = onlyMarkdown,
                    modifiedFromEpochMs = modifiedFrom,
                    modifiedToEpochMs = modifiedTo,
                    indexedFromEpochMs = indexedFrom,
                    indexedToEpochMs = indexedTo,
                    minBacklinks = minBacklinks,
                    maxBacklinks = maxBacklinks,
                    orphansOnly = orphansOnly,
                    mimeCategories = mimeCategories,
                    requireAttachment = requireAttachment,
                    sort = sort,
                )
            return VaultSearchDslParseResult(
                query = query,
                issues = issues,
                unsupportedOperators = unsupported,
            )
        }

        private enum class DateField { Modified, Created }

        private fun applyDateRange(token: String, value: String, clock: VaultSearchDslClock, field: DateField) {
            val range = VaultSearchDslDateRanges.resolve(value, clock)
            if (range == null) {
                val label = if (field == DateField.Modified) "modified" else "created"
                issues.add(VaultSearchDslIssue(token, "Unknown $label date: $value"))
                freeText.add(token)
                return
            }
            when (field) {
                DateField.Modified -> {
                    modifiedFrom = range.fromEpochMs
                    modifiedTo = range.toEpochMs
                }
                DateField.Created -> {
                    indexedFrom = range.fromEpochMs
                    indexedTo = range.toEpochMs
                }
            }
        }

        private fun applyTag(value: String) {
            val tag = value.removePrefix("#")
            if (tag.isNotEmpty()) tags = tags + tag
        }

        private fun applyType(value: String) {
            val (ext, mime) = typeMapping(value)
            extensions = extensions + ext
            mimeCategories = mimeCategories + mime
        }

        private fun applyHas(value: String) {
            when (value.lowercase()) {
                "image" -> {
                    extensions = extensions + imageExtensions
                    mimeCategories = mimeCategories + "image"
                }
                "audio" -> {
                    extensions = extensions + audioExtensions
                    mimeCategories = mimeCategories + "audio"
                }
                "attachment" -> requireAttachment = true
                else -> {
                    val (ext, mime) = typeMapping(value)
                    extensions = extensions + ext
                    mimeCategories = mimeCategories + mime
                }
            }
        }

        private fun applyBacklinks(token: String, value: String) {
            when (value.lowercase()) {
                "0" -> applyOrphanConstraints()
                "current" -> unsupported.add("backlinks")
                else -> {
                    val count = value.toIntOrNull()
                    when {
                        count == null -> {
                            issues.add(VaultSearchDslIssue(token, "Invalid backlinks value"))
                            freeText.add(token)
                        }
                        count == 0 -> applyOrphanConstraints()
                        else -> minBacklinks = count
                    }
                }
            }
        }

        private fun applyOrphanConstraints() {
            orphansOnly = true
            onlyMarkdown = true
            maxBacklinks = 0
        }

        private fun applySort(token: String, value: String) {
            sort =
                when (value.lowercase()) {
                    "modified_desc" -> VaultSearchSort.ModifiedDesc
                    "modified_asc" -> VaultSearchSort.ModifiedAsc
                    "name_asc" -> VaultSearchSort.NameAsc
                    else -> {
                        issues.add(VaultSearchDslIssue(token, "Unknown sort: $value"))
                        VaultSearchSort.Default
                    }
                }
        }

        private fun typeMapping(value: String): Pair<Set<String>, Set<String>> = when (value.lowercase()) {
            "pdf" -> setOf("pdf") to setOf("other")
            "image" -> imageExtensions to setOf("image")
            "audio" -> audioExtensions to setOf("audio")
            else -> setOf(value.lowercase()) to emptySet()
        }
    }

    internal companion object {
        val imageExtensions = setOf("png", "jpg", "jpeg", "gif", "webp", "svg")
        val audioExtensions = setOf("mp3", "wav", "m4a", "flac")
        val attachmentMimeCategories = setOf("image", "audio", "video", "other")

        fun tokenize(input: String): List<String> {
            val tokens = mutableListOf<String>()
            val current = StringBuilder()
            var inQuotes = false
            var i = 0
            while (i < input.length) {
                val c = input[i]
                when {
                    c == '"' -> {
                        inQuotes = !inQuotes
                        current.append(c)
                    }
                    c.isWhitespace() && !inQuotes -> {
                        if (current.isNotEmpty()) {
                            tokens.add(current.toString())
                            current.clear()
                        }
                    }
                    else -> current.append(c)
                }
                i++
            }
            if (current.isNotEmpty()) tokens.add(current.toString())
            return tokens
        }
    }
}
