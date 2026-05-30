package com.devindie.vaulty.domain.model.index

/**
 * High-level index state for a vault, observed via [VaultIndexRepository.observeIndexStatus].
 */
enum class VaultIndexStatus {
    NotIndexed,
    Indexing,
    Ready,
    Failed,
}
