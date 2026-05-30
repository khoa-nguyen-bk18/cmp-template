package com.devindie.vaulty.data.vault.index

import com.devindie.vaulty.data.vault.index.dao.SearchResultRow
import com.devindie.vaulty.data.vault.index.dao.VaultFileSearchDao
import com.devindie.vaulty.domain.model.index.VaultSearchQuery
import com.devindie.vaulty.domain.model.index.VaultSearchRanker
import com.devindie.vaulty.domain.model.index.VaultSearchResult

internal class VaultIndexSearchCoordinator {
    suspend fun searchVault(
        storageKey: String,
        query: VaultSearchQuery,
        searchDao: VaultFileSearchDao,
    ): List<VaultSearchResult> {
        val trimmed = query.text.trim()
        val hasText = trimmed.isNotBlank()
        if (!hasText && !query.hasStructuredFilters()) return emptyList()

        val candidateLimit = maxOf(query.limit, FTS_SEARCH_CANDIDATE_LIMIT)
        val tagIds = resolveTagIds(searchDao, storageKey, query)
        val rows =
            if (hasText) {
                searchFtsRows(searchDao, storageKey, query, trimmed, candidateLimit, tagIds)
            } else {
                searchDao.searchFiltered(
                    VaultSearchQueryFilters.searchFilteredArgs(query, storageKey, candidateLimit),
                )
            }

        return finalizeSearchResults(rows, query, trimmed, hasText)
    }

    private suspend fun resolveTagIds(
        searchDao: VaultFileSearchDao,
        storageKey: String,
        query: VaultSearchQuery,
    ): Set<Long>? = if (query.tags.isEmpty()) {
        null
    } else {
        searchDao
            .getFileIdsMatchingAllTags(
                vaultStorageKey = storageKey,
                tagNames = query.tags.toList(),
                requiredTagCount = query.tags.size,
            )
            .toSet()
    }

    private suspend fun searchFtsRows(
        searchDao: VaultFileSearchDao,
        storageKey: String,
        query: VaultSearchQuery,
        trimmed: String,
        candidateLimit: Int,
        tagIds: Set<Long>?,
    ): List<SearchResultRow> {
        val strictRows =
            searchDao.searchFts(
                vaultStorageKey = storageKey,
                matchQuery = buildFtsMatchQuery(trimmed, FtsMatchMode.And),
                onlyMarkdown = query.onlyMarkdown,
                limit = candidateLimit,
            )
        val mergedRows =
            if (
                strictRows.size < FTS_OR_FALLBACK_THRESHOLD &&
                VaultSearchRanker.queryTerms(trimmed).size > 1
            ) {
                val relaxedRows =
                    searchDao.searchFts(
                        vaultStorageKey = storageKey,
                        matchQuery = buildFtsMatchQuery(trimmed, FtsMatchMode.Or),
                        onlyMarkdown = query.onlyMarkdown,
                        limit = candidateLimit,
                    )
                mergeSearchRows(strictRows, relaxedRows)
            } else {
                strictRows
            }
        return mergedRows.filterRows(tagIds)
    }

    private fun finalizeSearchResults(
        rows: List<SearchResultRow>,
        query: VaultSearchQuery,
        trimmed: String,
        hasText: Boolean,
    ): List<VaultSearchResult> = rows
        .map { it.toDomain() }
        .filter { result -> VaultSearchQueryFilters.matchesPostSqlFilters(result, query) }
        .let { results ->
            if (hasText) {
                VaultSearchRanker.rank(trimmed, results)
            } else {
                results
            }
        }
        .sortedByVaultSearchSort(query.sort)
        .take(query.limit)

    private fun List<SearchResultRow>.filterRows(tagIds: Set<Long>?): List<SearchResultRow> = if (tagIds == null) {
        this
    } else {
        filter { it.id in tagIds }
    }
}
