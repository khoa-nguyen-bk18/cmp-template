package com.devindie.vaulty.data.vault.index

import com.devindie.vaulty.domain.model.index.VaultSearchQuery
import com.devindie.vaulty.domain.model.index.VaultSearchResult

internal object VaultSearchQueryFilters {
    fun searchFilteredArgs(query: VaultSearchQuery, vaultStorageKey: String, limit: Int): SearchFilteredArgs =
        SearchFilteredArgs(
            vaultStorageKey = vaultStorageKey,
            onlyMarkdown = query.onlyMarkdown,
            modifiedFromMs = query.effectiveModifiedFromEpochMs() ?: NO_LONG,
            modifiedToMs = query.modifiedToEpochMs ?: NO_LONG,
            indexedFromMs = query.indexedFromEpochMs ?: NO_LONG,
            indexedToMs = query.indexedToEpochMs ?: NO_LONG,
            orphansOnly = query.orphansOnly,
            minBacklinks = query.minBacklinks ?: NO_INT,
            maxBacklinks = query.maxBacklinks ?: NO_INT,
            requireAttachment = query.requireAttachment,
            tagNames = query.tags.toList().ifEmpty { listOf("") },
            tagCount = query.tags.size,
            limit = limit,
        )

    internal const val NO_LONG = -1L
    internal const val NO_INT = -1

    fun matchesPostSqlFilters(result: VaultSearchResult, query: VaultSearchQuery): Boolean {
        val tagsOk = matchesTagFilters(result, query)
        val extensionsOk = query.extensions.isEmpty() || result.file.extension in query.extensions
        val mimeOk = query.mimeCategories.isEmpty() || result.mimeCategory in query.mimeCategories
        val modifiedFromOk =
            query.effectiveModifiedFromEpochMs()?.let { from ->
                result.modifiedAtEpochMs >= from
            } ?: true
        val modifiedToOk =
            query.modifiedToEpochMs?.let { to ->
                result.modifiedAtEpochMs < to
            } ?: true
        return tagsOk && extensionsOk && mimeOk && modifiedFromOk && modifiedToOk
    }

    private fun matchesTagFilters(result: VaultSearchResult, query: VaultSearchQuery): Boolean {
        if (query.tags.isEmpty()) return true
        val haystack = result.file.relativePath.lowercase()
        val tagMatch =
            query.tags.all { tag ->
                haystack.contains(tag.lowercase()) ||
                    result.snippet.lowercase().contains("#${tag.lowercase()}")
            }
        return tagMatch || query.text.isNotBlank()
    }
}
