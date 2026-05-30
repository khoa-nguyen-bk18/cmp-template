package com.devindie.vaulty.domain.model.index

/**
 * Search and filter parameters for [com.devindie.vaulty.domain.usecase.index.SearchVaultUseCase].
 *
 * Free-text [text] is matched via FTS when non-blank. Structured fields are applied in SQL or
 * post-filter; see [hasStructuredFilters].
 */
data class VaultSearchQuery(
    val text: String = "",
    val extensions: Set<String> = emptySet(),
    val tags: Set<String> = emptySet(),
    val onlyMarkdown: Boolean = false,
    val hasBrokenLinks: Boolean = false,
    /** @deprecated Prefer [modifiedFromEpochMs] / [modifiedToEpochMs]. */
    val modifiedAfterEpochMs: Long? = null,
    val modifiedFromEpochMs: Long? = null,
    val modifiedToEpochMs: Long? = null,
    val indexedFromEpochMs: Long? = null,
    val indexedToEpochMs: Long? = null,
    val linkTargetPath: String? = null,
    val minBacklinks: Int? = null,
    val maxBacklinks: Int? = null,
    val orphansOnly: Boolean = false,
    val mimeCategories: Set<String> = emptySet(),
    val requireAttachment: Boolean = false,
    val sort: VaultSearchSort = VaultSearchSort.Default,
    val limit: Int = 50,
) {
    fun effectiveModifiedFromEpochMs(): Long? = modifiedFromEpochMs ?: modifiedAfterEpochMs

    fun hasStructuredFilters(): Boolean = tags.isNotEmpty() ||
        effectiveModifiedFromEpochMs() != null ||
        modifiedToEpochMs != null ||
        indexedFromEpochMs != null ||
        indexedToEpochMs != null ||
        extensions.isNotEmpty() ||
        mimeCategories.isNotEmpty() ||
        requireAttachment ||
        orphansOnly ||
        minBacklinks != null ||
        maxBacklinks != null ||
        hasBrokenLinks ||
        linkTargetPath != null ||
        sort != VaultSearchSort.Default
}
