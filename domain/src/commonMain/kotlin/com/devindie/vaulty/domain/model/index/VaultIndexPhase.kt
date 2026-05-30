package com.devindie.vaulty.domain.model.index

/**
 * Stage of an in-flight index run surfaced through [VaultIndexProgress].
 */
enum class VaultIndexPhase {
    /** Walking the vault folder tree and gathering file metadata. */
    Collecting,

    /** Reading, parsing, and persisting files into Room. */
    Indexing,
}
