package com.devindie.vaulty.domain.model.index

/**
 * Progress snapshot while [VaultIndexStatus.Indexing] is active.
 *
 * @param phase [VaultIndexPhase.Collecting] uses [processed] as files discovered so far and
 *   [total] `0` for an indeterminate bar; [VaultIndexPhase.Indexing] uses [processed]/[total]
 *   for the per-file indexing loop.
 */
data class VaultIndexProgress(
    val phase: VaultIndexPhase,
    val processed: Int,
    val total: Int,
    val currentPath: String,
    val startedAtEpochMs: Long,
)
