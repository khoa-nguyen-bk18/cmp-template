package com.devindie.vaulty.domain.model.index

/**
 * Lightweight reference to an indexed file (search hits, backlinks).
 *
 * @property id Stable Room primary key for the file row.
 * @property relativePath Path within the vault root.
 */
data class VaultFileRef(
    val id: Long,
    val relativePath: String,
    val name: String,
    val extension: String,
    val isMarkdown: Boolean,
)
