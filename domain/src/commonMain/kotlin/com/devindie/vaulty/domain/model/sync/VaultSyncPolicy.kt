package com.devindie.vaulty.domain.model.sync

/** Debounce and rate limits for automatic vault indexing. */
object VaultSyncPolicy {
    const val DEBOUNCE_MS: Long = 1_500L
    const val MIN_INTERVAL_MS: Long = 30_000L
}
