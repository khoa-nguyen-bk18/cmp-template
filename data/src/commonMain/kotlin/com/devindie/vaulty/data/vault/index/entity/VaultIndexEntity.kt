package com.devindie.vaulty.data.vault.index.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Per-vault index metadata and status string (`indexing`, `ready`, `failed`). */
@Entity(tableName = "vault_index")
data class VaultIndexEntity(
    @PrimaryKey val storageKey: String,
    val displayName: String,
    val indexedAtEpochMs: Long,
    val schemaVersion: Int,
    val status: String,
)
