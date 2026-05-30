package com.devindie.vaulty.data.vault.index

import com.devindie.vaulty.domain.model.index.VaultSearchQuery
import com.devindie.vaulty.domain.model.index.VaultSearchSort
import kotlin.test.Test
import kotlin.test.assertEquals

class SearchFilteredArgsTest {
    @Test
    fun searchFilteredArgs_mapsQueryFiltersToDaoSentinels() {
        val args =
            VaultSearchQueryFilters.searchFilteredArgs(
                query =
                VaultSearchQuery(
                    tags = setOf("meeting"),
                    modifiedFromEpochMs = 100L,
                    modifiedToEpochMs = 200L,
                    orphansOnly = true,
                    minBacklinks = 2,
                    requireAttachment = true,
                    sort = VaultSearchSort.ModifiedDesc,
                ),
                vaultStorageKey = "vault-key",
                limit = 25,
            )

        assertEquals("vault-key", args.vaultStorageKey)
        assertEquals(100L, args.modifiedFromMs)
        assertEquals(200L, args.modifiedToMs)
        assertEquals(VaultSearchQueryFilters.NO_LONG, args.indexedFromMs)
        assertEquals(VaultSearchQueryFilters.NO_INT, args.maxBacklinks)
        assertEquals(listOf("meeting"), args.tagNames)
        assertEquals(1, args.tagCount)
        assertEquals(25, args.limit)
        assertEquals(true, args.requireAttachment)
    }
}
