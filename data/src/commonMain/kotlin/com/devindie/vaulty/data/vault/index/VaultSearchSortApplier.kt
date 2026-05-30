package com.devindie.vaulty.data.vault.index

import com.devindie.vaulty.domain.model.index.VaultSearchResult
import com.devindie.vaulty.domain.model.index.VaultSearchSort

internal fun List<VaultSearchResult>.sortedByVaultSearchSort(sort: VaultSearchSort): List<VaultSearchResult> =
    when (sort) {
        VaultSearchSort.Default -> this
        VaultSearchSort.ModifiedDesc -> sortedByDescending { it.modifiedAtEpochMs }
        VaultSearchSort.ModifiedAsc -> sortedBy { it.modifiedAtEpochMs }
        VaultSearchSort.NameAsc -> sortedBy { it.file.name.lowercase() }
    }
