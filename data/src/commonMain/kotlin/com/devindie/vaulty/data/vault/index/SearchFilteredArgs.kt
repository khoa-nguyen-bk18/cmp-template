package com.devindie.vaulty.data.vault.index

import com.devindie.vaulty.data.vault.index.dao.VaultFileSearchDao

/** Bind arguments for [VaultFileSearchDao.searchFiltered] (keeps call sites under Sonar parameter limits). */
internal data class SearchFilteredArgs(
    val vaultStorageKey: String,
    val onlyMarkdown: Boolean,
    val modifiedFromMs: Long,
    val modifiedToMs: Long,
    val indexedFromMs: Long,
    val indexedToMs: Long,
    val orphansOnly: Boolean,
    val minBacklinks: Int,
    val maxBacklinks: Int,
    val requireAttachment: Boolean,
    val tagNames: List<String>,
    val tagCount: Int,
    val limit: Int,
)

internal suspend fun VaultFileSearchDao.searchFiltered(
    args: SearchFilteredArgs,
): List<com.devindie.vaulty.data.vault.index.dao.SearchResultRow> = searchFiltered(
    vaultStorageKey = args.vaultStorageKey,
    onlyMarkdown = args.onlyMarkdown,
    modifiedFromMs = args.modifiedFromMs,
    modifiedToMs = args.modifiedToMs,
    indexedFromMs = args.indexedFromMs,
    indexedToMs = args.indexedToMs,
    orphansOnly = args.orphansOnly,
    minBacklinks = args.minBacklinks,
    maxBacklinks = args.maxBacklinks,
    requireAttachment = args.requireAttachment,
    tagNames = args.tagNames,
    tagCount = args.tagCount,
    limit = args.limit,
)
