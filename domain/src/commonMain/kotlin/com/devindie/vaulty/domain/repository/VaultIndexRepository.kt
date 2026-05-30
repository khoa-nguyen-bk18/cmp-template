package com.devindie.vaulty.domain.repository

import com.devindie.vaulty.domain.model.index.VaultDashboardStats
import com.devindie.vaulty.domain.model.index.VaultFileRef
import com.devindie.vaulty.domain.model.index.VaultIndexProgress
import com.devindie.vaulty.domain.model.index.VaultIndexStatus
import com.devindie.vaulty.domain.model.index.VaultLink
import com.devindie.vaulty.domain.model.index.VaultSearchQuery
import com.devindie.vaulty.domain.model.index.VaultSearchResult
import kotlinx.coroutines.flow.Flow

/**
 * Contract for Room-backed vault indexing, search, and link graph queries.
 *
 * **Upstream:** Index and observe use cases in [com.devindie.vaulty.domain.usecase.index].
 * **Downstream:** [com.devindie.vaulty.data.vault.index.VaultIndexRepositoryImpl] →
 * [com.devindie.vaulty.data.vault.index.VaultFolderIndexer].
 *
 * @see com.devindie.vaulty.data.vault.index.VaultIndexRepositoryImpl
 * @see com.devindie.vaulty.data.vault.index.VaultFolderIndexer
 */
interface VaultIndexRepository {
    /** Index lifecycle for a vault identified by [storageKey]. */
    fun observeIndexStatus(storageKey: String): Flow<VaultIndexStatus>

    /** In-flight indexing progress; `null` when idle. */
    fun observeIndexProgress(): Flow<VaultIndexProgress?>

    /**
     * Indexes the currently selected vault folder.
     *
     * @param fullRebuild When `true`, clears existing index rows before re-scanning all files.
     */
    suspend fun indexSelectedVault(fullRebuild: Boolean): Result<Unit>

    /** Full-text and filter search over indexed content. */
    suspend fun search(query: VaultSearchQuery): Result<List<VaultSearchResult>>

    /** Aggregated counts for the dashboard (extensions, tags, broken links, orphans). */
    suspend fun getDashboardStats(): Result<VaultDashboardStats>

    /** Files that link *to* the file identified by [fileId]. */
    suspend fun getBacklinks(fileId: Long): Result<List<VaultFileRef>>

    /** Outgoing links from [fileId] (wiki, markdown, embed). */
    suspend fun getOutgoingLinks(fileId: Long): Result<List<VaultLink>>
}
